package com.taiobas.web.domain;

import java.math.BigDecimal;
import java.util.Collection;

/** Regras puras: independem de Swing, HTTP, sessão e banco de dados. */
public final class RegrasEstoque {
    private RegrasEstoque() {}

    public static int calcularSaldo(int saldo, String tipo, int quantidade) {
        if (saldo < 0 || quantidade <= 0) throw new IllegalArgumentException("Informe uma quantidade maior que zero.");
        return switch (tipo) {
            case "ENTRADA" -> Math.addExact(saldo, quantidade);
            case "SAIDA" -> {
                if (quantidade > saldo) throw new IllegalArgumentException("Estoque insuficiente para essa saída.");
                yield saldo - quantidade;
            }
            default -> throw new IllegalArgumentException("Tipo de movimentação inválido.");
        };
    }

    public static BigDecimal valorTotal(Collection<Produto> produtos) {
        return produtos.stream().map(p -> p.preco().multiply(BigDecimal.valueOf(p.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static boolean estoqueBaixo(Produto produto) { return produto.quantidade() <= produto.minimo(); }
}
