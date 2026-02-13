# 🏗️ Billing Service – Constructores Vanguardistas

Backend RESTful desarrollado con **Spring Boot 3.5.5** y **Java 21** para la gestión estructurada de presupuestos, partidas, insumos y estructuras financieras dentro del sector construcción.

Este servicio forma parte del backend principal de la plataforma de presupuestos de **Constructores Vanguardistas**.

---

## 🚀 Stack Tecnológico

- Java 21
- Spring Boot 3.5.5
- Spring Security
- JWT (RS256 con llaves RSA)
- PostgreSQL
- Flyway (Migraciones de base de datos)
- Spring Data JPA / Hibernate
- MapStruct
- Lombok
- Swagger / OpenAPI (springdoc)
- Actuator
- Maven

---

## 📐 Arquitectura

El proyecto sigue una arquitectura modular organizada por capas:

```
com.cvanguardistas.billing_service
│
├── bootstrap        → Inicialización / Seeders
├── controller       → Controladores REST
├── dto              → Objetos de transferencia de datos
├── service          → Lógica de negocio
├── repository       → Acceso a datos (Spring Data JPA)
├── security         → Configuración JWT y filtros
└── config           → Configuración general
```

### Principios aplicados

- Separación clara de responsabilidades  
- Uso de DTOs para desacoplar dominio y transporte  
- Mapeo automático con MapStruct  
- Configuración externalizada basada en variables de entorno  
- Seguridad basada en JWT con llaves RSA externas  
- Migraciones versionadas con Flyway  
- Hibernate en modo `validate` (no genera tablas automáticamente)

---

## 🔐 Seguridad

- Autenticación mediante JWT (RS256)  
- Access Token + Refresh Token  
- Llaves RSA externas (no almacenadas en el repositorio)  
- Configuración CORS parametrizable  
- Header de correlación para trazabilidad (`X-Correlation-Id`)  
- Endpoints de Actuator restringidos  

---

## 🗄️ Base de Datos

Motor utilizado: **PostgreSQL**

Migraciones gestionadas con Flyway en:

```
src/main/resources/db.migration
```

Configuración relevante:

```
spring.jpa.hibernate.ddl-auto=validate
```

El esquema debe existir y estar alineado con las entidades.

---

## ⚙️ Configuración

El proyecto utiliza variables de entorno para evitar exponer credenciales sensibles.

### Variables necesarias

```env
# ==== DATABASE ====
DB_URL=jdbc:postgresql://localhost:5432/constructoresvanguardistas
DB_USER=postgres
DB_PASSWORD=your_password

# ==== JWT ====
JWT_ALG=RS256
JWT_ISS=https://cvanguardistas.auth
JWT_AUD=api-billing
JWT_ACCESS_TTL_MINUTES=15
JWT_REFRESH_TTL_DAYS=14
JWT_PRIVATE_KEY_PATH=/ruta/a/jwt-private.pem
JWT_PUBLIC_KEY_PATH=/ruta/a/jwt-public.pem

# ==== CORS ====
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:5173

# ==== TRACING ====
CORRELATION_HEADER=X-Correlation-Id
```

⚠️ Las llaves `.pem` no deben subirse al repositorio.

---

## ▶️ Ejecución del Proyecto

### 1️⃣ Clonar repositorio

```bash
git clone <repo-url>
cd billing-service
```

### 2️⃣ Configurar variables de entorno

Crear archivo `.env` o exportar variables manualmente.

### 3️⃣ Ejecutar aplicación

```bash
./mvnw spring-boot:run
```

O compilar:

```bash
./mvnw clean install
```

---

## 📘 Documentación API

Swagger UI disponible en:

```
http://localhost:8080/swagger-ui.html
```

---

## ❤️ Actuator

Endpoints expuestos:

```
/actuator/health
/actuator/info
```

---

## 📌 Dominio del Sistema

El servicio permite administrar:

- Presupuestos  
- Partidas  
- Subpresupuestos  
- Insumos  
- Plantillas  
- Programación  
- Reportes  
- Auditoría  

Está diseñado para estructurar y controlar costos dentro de proyectos de construcción.

---

## 🏢 Contexto

Backend desarrollado para la plataforma de presupuestos de **Constructores Vanguardistas**.

---

## 📜 Licencia

Uso interno empresarial.
