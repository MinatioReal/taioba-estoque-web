package com.taiobas.web.api;

import com.taiobas.web.domain.*;
import com.taiobas.web.repository.ProdutoRepository;
import com.taiobas.web.service.EstoqueService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api")
public class EstoqueApi {
    private final ProdutoRepository produtos;
    private final EstoqueService estoque;
    private final JdbcTemplate jdbc;
    public EstoqueApi(ProdutoRepository produtos,EstoqueService estoque,JdbcTemplate jdbc) {
        this.produtos=produtos;this.estoque=estoque;this.jdbc=jdbc;
    }
    public record ProdutoInput(@NotBlank @Size(max=100) String nome, @NotNull @Size(max=500) String descricao,
        @NotBlank @Size(max=50) String categoria, @NotNull @DecimalMin("0.00") @DecimalMax("9999999999.99") @Digits(integer=10,fraction=2) BigDecimal preco,
        @Min(0) int minimo, @Positive long fornecedorId) {
        Produto produto() { return new Produto(0,nome.trim(),descricao.trim(),categoria.trim(),preco,0,minimo,fornecedorId,""); }
    }
    public record MovimentoInput(@Positive long produtoId,@Pattern(regexp="ENTRADA|SAIDA") @NotNull String tipo,
        @Positive int quantidade,@NotNull @Size(max=250) String observacao) {}
    public record FornecedorInput(@NotBlank @Size(max=100) String nome,
        @Pattern(regexp="[0-9]{2}\\.[0-9]{3}\\.[0-9]{3}/[0-9]{4}-[0-9]{2}") @NotNull String cnpj,
        @Email @NotBlank @Size(max=120) String email,@NotBlank @Size(max=25) String telefone) {}

    @GetMapping("/sessao") public Map<String,Object> sessao(Authentication auth,CsrfToken csrf) {
        return Map.of("autenticado",auth!=null,"usuario",auth==null?"":auth.getName(),
            "perfil",auth==null?"":auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_",""),
            "csrf",csrf.getToken());
    }
    @GetMapping("/produtos") public List<Produto> produtos() { return produtos.listar(); }
    @PostMapping("/produtos") public Map<String,Long> criar(@Valid @RequestBody ProdutoInput p) { return Map.of("id",estoque.salvar(null,p.produto())); }
    @PutMapping("/produtos/{id}") public void editar(@PathVariable long id,@Valid @RequestBody ProdutoInput p) { estoque.salvar(id,p.produto()); }
    @DeleteMapping("/produtos/{id}") public void excluir(@PathVariable long id) { estoque.excluir(id); }
    @PostMapping("/movimentacoes") public void movimentar(@Valid @RequestBody MovimentoInput m,Authentication auth) {
        estoque.movimentar(m.produtoId(),m.tipo(),m.quantidade(),m.observacao(),auth.getName());
    }
    @GetMapping("/movimentacoes") public List<Map<String,Object>> historico() {
        return jdbc.query("SELECT m.*,p.nome FROM movimentacao m JOIN produto p ON p.id=m.produto_id ORDER BY m.id DESC",(r,n)->Map.<String,Object>of(
            "id",r.getLong("id"),"produto",r.getString("nome"),"tipo",r.getString("tipo"),"quantidade",r.getInt("quantidade"),
            "observacao",r.getString("observacao"),"usuario",r.getString("usuario"),"data",r.getObject("data_hora",java.time.OffsetDateTime.class).toString()));
    }
    @GetMapping("/fornecedores") public List<Fornecedor> fornecedores() {
        return jdbc.query("SELECT * FROM fornecedor ORDER BY nome",(r,n)->new Fornecedor(r.getLong("id"),r.getString("nome"),r.getString("cnpj"),r.getString("email"),r.getString("telefone")));
    }
    @PostMapping("/fornecedores") public void criarFornecedor(@Valid @RequestBody FornecedorInput f) {
        jdbc.update("INSERT INTO fornecedor(nome,cnpj,email,telefone) VALUES(?,?,?,?)",f.nome().trim(),f.cnpj(),f.email(),f.telefone());
    }
    @PutMapping("/fornecedores/{id}") public void editarFornecedor(@PathVariable long id,@Valid @RequestBody FornecedorInput f) {
        if(jdbc.update("UPDATE fornecedor SET nome=?,cnpj=?,email=?,telefone=? WHERE id=?",f.nome().trim(),f.cnpj(),f.email(),f.telefone(),id)==0)
            throw new org.springframework.dao.EmptyResultDataAccessException(1);
    }
    @DeleteMapping("/fornecedores/{id}") public void excluirFornecedor(@PathVariable long id) {
        if(jdbc.update("DELETE FROM fornecedor WHERE id=?",id)==0) throw new org.springframework.dao.EmptyResultDataAccessException(1);
    }
}
