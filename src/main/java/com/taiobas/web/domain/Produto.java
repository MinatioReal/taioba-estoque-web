package com.taiobas.web.domain;

import java.math.BigDecimal;

public record Produto(long id, String nome, String descricao, String categoria, BigDecimal preco,
                      int quantidade, int minimo, long fornecedorId, String fornecedor) {}
