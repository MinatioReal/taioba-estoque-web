package com.taiobas.web.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RegrasEstoqueTest {
    @Test void entradaSomaSaldo() { assertEquals(15,RegrasEstoque.calcularSaldo(10,"ENTRADA",5)); }
    @Test void saidaSubtraiSaldo() { assertEquals(5,RegrasEstoque.calcularSaldo(10,"SAIDA",5)); }
    @Test void saidaPodeZerar() { assertEquals(0,RegrasEstoque.calcularSaldo(10,"SAIDA",10)); }
    @Test void recusaSaldoInsuficiente() { assertThrows(IllegalArgumentException.class,()->RegrasEstoque.calcularSaldo(3,"SAIDA",4)); }
    @Test void recusaQuantidadeInvalida() {
        for(int n:new int[]{0,-1}) assertThrows(IllegalArgumentException.class,()->RegrasEstoque.calcularSaldo(3,"ENTRADA",n));
    }
    @Test void recusaTipoInvalido() { assertThrows(IllegalArgumentException.class,()->RegrasEstoque.calcularSaldo(3,"VENDA",1)); }
    @Test void detectaOverflow() { assertThrows(ArithmeticException.class,()->RegrasEstoque.calcularSaldo(Integer.MAX_VALUE,"ENTRADA",1)); }
    @Test void calculaDinheiroSemErroDePontoFlutuante() {
        Produto p=new Produto(1,"Produto","","Teste",new BigDecimal("0.10"),3,3,1,"");
        assertEquals(new BigDecimal("0.30"),RegrasEstoque.valorTotal(List.of(p)));
        assertTrue(RegrasEstoque.estoqueBaixo(p));
    }
    @Test void estoqueVazioTemValorZero() { assertEquals(BigDecimal.ZERO,RegrasEstoque.valorTotal(List.of())); }
}
