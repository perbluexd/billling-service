// src/main/java/com/cvanguardistas/billing_service/service/impl/SPFormulaServiceImpl.java
package com.cvanguardistas.billing_service.service.impl;

import com.cvanguardistas.billing_service.entities.SPFormula;
import com.cvanguardistas.billing_service.exception.DomainException;
import com.cvanguardistas.billing_service.repository.SPFormulaRepository;
import com.cvanguardistas.billing_service.service.SPFormulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SPFormulaServiceImpl implements SPFormulaService {

    private static final int SCALE_MONTO = 2;

    private final SPFormulaRepository repo;

    @Override
    @Transactional
    public Map<String, BigDecimal> evaluarPie(Long subPresupuestoId, Map<String, BigDecimal> variablesBase) {
        List<SPFormula> formulas = repo.findBySubPresupuestoIdOrderByOrdenAsc(subPresupuestoId);
        Map<String, BigDecimal> ctx = new HashMap<>(variablesBase == null ? Map.of() : variablesBase);

        for (SPFormula f : formulas) {
            BigDecimal valor = evalExpresionSeguro(f.getExpresion(), ctx);
            valor = valor.setScale(SCALE_MONTO, RoundingMode.HALF_UP);
            f.setValor(valor);
            repo.save(f);
            ctx.put(f.getVariable(), valor);
        }
        return ctx;
    }

    // ===== Evaluador seguro: números decimales, variables [A-Z_]+, operadores + - * / y paréntesis =====
    private static final Pattern TOKEN = Pattern.compile("\\s*([A-Z_]+|\\d+(?:\\.\\d+)?|[+\\-*/()])\\s*");

    private BigDecimal evalExpresionSeguro(String expr, Map<String, BigDecimal> ctx) {
        List<String> rpn = toRpn(expr);
        Deque<BigDecimal> st = new ArrayDeque<>();
        for (String t : rpn) {
            if (isNumber(t)) {
                st.push(new BigDecimal(t));
            } else if (isVar(t)) {
                BigDecimal v = ctx.get(t);
                if (v == null) throw new DomainException("Variable no definida: " + t);
                st.push(v);
            } else {
                BigDecimal b = st.pop(), a = st.pop(), r;
                switch (t) {
                    case "+": r = a.add(b); break;
                    case "-": r = a.subtract(b); break;
                    case "*": r = a.multiply(b); break;
                    case "/": r = a.divide(b, 6, RoundingMode.HALF_UP); break;
                    default: throw new DomainException("Operador no soportado: " + t);
                }
                st.push(r);
            }
        }
        if (st.size() != 1) throw new DomainException("Expresión mal formada: " + expr);
        return st.pop();
    }

    private List<String> toRpn(String expr) {
        List<String> out = new ArrayList<>();
        Deque<String> ops = new ArrayDeque<>();
        Matcher m = TOKEN.matcher(expr);
        int i = 0;
        while (m.find()) {
            if (m.start() != i) throw new DomainException("Token inválido en: " + expr);
            String t = m.group(1);
            i = m.end();

            if (isNumber(t) || isVar(t)) out.add(t);
            else if ("(".equals(t)) ops.push(t);
            else if (")".equals(t)) {
                while (!ops.isEmpty() && !"(".equals(ops.peek())) out.add(ops.pop());
                if (ops.isEmpty()) throw new DomainException("Paréntesis no balanceados");
                ops.pop();
            } else { // operador
                while (!ops.isEmpty() && prec(ops.peek()) >= prec(t)) out.add(ops.pop());
                ops.push(t);
            }
        }
        if (i != expr.length()) throw new DomainException("Expresión inválida: " + expr);
        while (!ops.isEmpty()) {
            String op = ops.pop();
            if ("(".equals(op)) throw new DomainException("Paréntesis no balanceados");
            out.add(op);
        }
        return out;
    }

    private boolean isNumber(String t) { return t.chars().allMatch(c -> "0123456789.".indexOf(c) >= 0) && t.charAt(0) != '.'; }
    private boolean isVar(String t) { return t.matches("[A-Z_]+"); }
    private int prec(String op) { return (op.equals("+") || op.equals("-")) ? 1 : (op.equals("*") || op.equals("/")) ? 2 : 0; }
}
