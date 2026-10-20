package com.taiobas.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:taioba-test;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
@Transactional
class EstoqueApiTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    long id() {return jdbc.queryForObject("SELECT MIN(id) FROM produto",Long.class);}
    @Test void exigeLoginParaConsultar() throws Exception {mvc.perform(get("/api/produtos")).andExpect(status().isUnauthorized());}
    @Test void loginCorreto() throws Exception {mvc.perform(formLogin().user("admin").password("admin123")).andExpect(status().isOk());}
    @Test void loginIncorreto() throws Exception {mvc.perform(formLogin().user("admin").password("errada")).andExpect(status().isUnauthorized());}
    @Test @WithMockUser(roles="AUDITOR") void auditorConsulta() throws Exception {mvc.perform(get("/api/produtos")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nome").isNotEmpty());}
    @Test @WithMockUser(roles="AUDITOR") void auditorNaoMovimenta() throws Exception {
        mvc.perform(post("/api/movimentacoes").with(csrf()).contentType("application/json").content("{\"produtoId\":"+id()+",\"tipo\":\"ENTRADA\",\"quantidade\":1,\"observacao\":\"teste\"}")).andExpect(status().isForbidden());
    }
    @Test @WithMockUser(roles="OPERADOR") void operadorNaoCadastraProdutos() throws Exception {
        mvc.perform(post("/api/produtos").with(csrf()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }
    @Test @WithMockUser(roles="ADMIN") void exigeCsrf() throws Exception {
        mvc.perform(post("/api/produtos").contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }
    @Test @WithMockUser(roles="ADMIN") void validaProduto() throws Exception {
        mvc.perform(post("/api/produtos").with(csrf()).contentType("application/json").content("{\"nome\":\"\",\"preco\":-1}")).andExpect(status().isBadRequest());
    }
    @Test @WithMockUser(username="operador",roles="OPERADOR") void entradaAtualizaSaldoEHistorico() throws Exception {
        long id=id(); int saldo=jdbc.queryForObject("SELECT quantidade FROM produto WHERE id=?",Integer.class,id);
        int registros=jdbc.queryForObject("SELECT COUNT(*) FROM movimentacao",Integer.class);
        mvc.perform(post("/api/movimentacoes").with(csrf()).contentType("application/json").content("{\"produtoId\":"+id+",\"tipo\":\"ENTRADA\",\"quantidade\":5,\"observacao\":\"teste\"}")).andExpect(status().isOk());
        assertEquals(saldo+5,jdbc.queryForObject("SELECT quantidade FROM produto WHERE id=?",Integer.class,id));
        assertEquals(registros+1,jdbc.queryForObject("SELECT COUNT(*) FROM movimentacao",Integer.class));
        assertEquals("operador",jdbc.queryForObject("SELECT usuario FROM movimentacao ORDER BY id DESC LIMIT 1",String.class));
    }
    @Test @WithMockUser(roles="ADMIN") void saidaInvalidaNaoAlteraBanco() throws Exception {
        long id=id(); int saldo=jdbc.queryForObject("SELECT quantidade FROM produto WHERE id=?",Integer.class,id);
        int registros=jdbc.queryForObject("SELECT COUNT(*) FROM movimentacao",Integer.class);
        mvc.perform(post("/api/movimentacoes").with(csrf()).contentType("application/json").content("{\"produtoId\":"+id+",\"tipo\":\"SAIDA\",\"quantidade\":2147483647,\"observacao\":\"teste\"}")).andExpect(status().isBadRequest());
        assertEquals(saldo,jdbc.queryForObject("SELECT quantidade FROM produto WHERE id=?",Integer.class,id));
        assertEquals(registros,jdbc.queryForObject("SELECT COUNT(*) FROM movimentacao",Integer.class));
    }
    @Test @WithMockUser(roles="ADMIN") void preservaProdutoComSaldo() throws Exception {
        mvc.perform(delete("/api/produtos/"+id()).with(csrf())).andExpect(status().isBadRequest());
    }
    @Test @WithMockUser(roles="ADMIN") void produtoInexistenteRetorna404() throws Exception {
        mvc.perform(delete("/api/produtos/999999").with(csrf())).andExpect(status().isNotFound());
    }
    @Test @WithMockUser(roles="ADMIN") void fornecedorVinculadoNaoPodeSerExcluido() throws Exception {
        long fornecedor=jdbc.queryForObject("SELECT fornecedor_id FROM produto WHERE id=?",Long.class,id());
        mvc.perform(delete("/api/fornecedores/"+fornecedor).with(csrf())).andExpect(status().isConflict());
    }
}
