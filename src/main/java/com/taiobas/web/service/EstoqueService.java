package com.taiobas.web.service;

import com.taiobas.web.domain.Produto;
import com.taiobas.web.domain.RegrasEstoque;
import com.taiobas.web.repository.ProdutoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstoqueService {
    private final ProdutoRepository produtos;
    private final JdbcTemplate jdbc;
    public EstoqueService(ProdutoRepository produtos, JdbcTemplate jdbc) { this.produtos=produtos; this.jdbc=jdbc; }

    @Transactional
    public long salvar(Long id, Produto produto) {
        if(id != null) produtos.buscar(id,true);
        return produtos.salvar(id,produto);
    }

    @Transactional
    public void movimentar(long id,String tipo,int quantidade,String observacao,String usuario) {
        Produto produto=produtos.buscar(id,true);
        int saldo=RegrasEstoque.calcularSaldo(produto.quantidade(),tipo,quantidade);
        produtos.atualizarSaldo(id,saldo);
        jdbc.update("INSERT INTO movimentacao(produto_id,tipo,quantidade,observacao,usuario) VALUES(?,?,?,?,?)",
            id,tipo,quantidade,observacao,usuario);
    }

    @Transactional
    public void excluir(long id) {
        Produto p=produtos.buscar(id,true);
        if(p.quantidade()!=0) throw new IllegalArgumentException("Zere o estoque antes de excluir o produto.");
        if(jdbc.queryForObject("SELECT COUNT(*) FROM movimentacao WHERE produto_id=?",Integer.class,id)>0)
            throw new IllegalArgumentException("Este produto possui histórico e deve ser preservado.");
        produtos.excluir(id);
    }
}
