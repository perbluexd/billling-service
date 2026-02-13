package com.cvanguardistas.billing_service.security;

import com.cvanguardistas.billing_service.entities.Usuario;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties props;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtService(JwtProperties props, KeyLoader loader) {
        this.props = props;
        this.privateKey = loader.loadPrivateKey(props.privateKeyPem(), props.privateKeyPath());
        this.publicKey = loader.loadPublicKey(props.publicKeyPem(), props.publicKeyPath());
    }

    /* ==========================
       Emisión de tokens
       ========================== */

    public String createAccessToken(Usuario u, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.accessTtlMinutes() * 60L);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(props.issuer())
                .audience(props.audience())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .jwtID(UUID.randomUUID().toString())
                .subject(String.valueOf(u.getId()))
                .claim("email", u.getEmail())
                .claim("email_verified", Boolean.TRUE.equals(u.getEmailVerificado()))
                .claim("roles", roles)
                .build();

        return sign(claims);
    }

    public String createRefreshToken(Usuario u) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.refreshTtlDays() * 24L * 3600L);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(props.issuer())
                .audience(props.audience())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .jwtID(UUID.randomUUID().toString())
                .subject(String.valueOf(u.getId()))
                .claim("typ", "refresh")
                .build();

        return sign(claims);
    }

    private String sign(JWTClaimsSet claims) {
        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .build();
            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(new RSASSASigner(privateKey));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("JWT sign error: " + e.getMessage(), e);
        }
    }

    /* ==========================
       Validación / parsing
       ========================== */

    /** Validación original: firma + expiración. Devuelve claims si es válido. */
    public JWTClaimsSet parseAndValidate(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!(publicKey instanceof RSAPublicKey rp)) {
                throw new IllegalStateException("JWT publicKey no es RSA; se esperaba RSAPublicKey para RS256");
            }
            JWSVerifier verifier = new RSASSAVerifier(rp);

            if (!jwt.verify(verifier)) {
                throw new JOSEException("Signature invalid");
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date now = new Date();

            if (claims.getExpirationTime() == null || now.after(claims.getExpirationTime())) {
                throw new JOSEException("Token expired");
            }

            return claims;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT: " + e.getMessage(), e);
        }
    }

    /**
     * Overload reforzado: además de firma/exp, valida:
     * - typ (si se espera, p.ej. "refresh")
     * - issuer (iss) y audience (aud)
     */
    public JWTClaimsSet parseAndValidate(String token, String expectedTyp) {
        JWTClaimsSet claims = parseAndValidate(token); // valida firma + exp primero

        // typ (cuando lo exijas, ej. "refresh")
        if (expectedTyp != null) {
            String typ = asString(claims.getClaim("typ"));
            if (!Objects.equals(typ, expectedTyp)) {
                throw new IllegalArgumentException("Invalid JWT: unexpected typ");
            }
        }

        // iss
        String iss = claims.getIssuer();
        if (iss == null || !iss.equals(props.issuer())) {
            throw new IllegalArgumentException("Invalid JWT: bad issuer");
        }

        // aud (contiene tu audience configurada)
        List<String> aud = claims.getAudience();
        if (aud == null || !aud.contains(props.audience())) {
            throw new IllegalArgumentException("Invalid JWT: bad audience");
        }

        return claims;
    }

    private static String asString(Object o) {
        return (o == null) ? null : String.valueOf(o);
    }
}
