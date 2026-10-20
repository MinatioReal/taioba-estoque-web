package com.taiobas.web.repository;

import com.taiobas.web.domain.Produto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.Statement;
import java.util.List;

@Repository
public class JdbcProdutoRepository implements ProdutoRepository {
    private final JdbcTemplate jdbc;
    private static final String SELECT = "SELECT p.*, f.nome AS fornecedor FROM produto p JOIN fornecedor f ON f.id=p.fornecedor_id";
    private static final RowMapper<Produto> MAPPER = (r, n) -> new Produto(r.getLong("id"),r.getString("nome"),
        r.getString("descricao"),r.getString("categoria"),r.getBigDecimal("preco"),r.getInt("quantidade"),
        r.getInt("minimo"),r.getLong("fornecedor_id"),r.getString("fornecedor"));

    public JdbcProdutoRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<Produto> listar() { return jdbc.query(SELECT + " ORDER BY p.id DESC", MAPPER); }
    public Produto buscar(long id, boolean bloquear) {
        return jdbc.queryForObject(SELECT + " WHERE p.id=?" + (bloquear ? " FOR UPDATE" : ""), MAPPER, id);
    }
    public long salvar(Long id, Produto p) {
        if (id != null) {
            jdbc.update("UPDATE produto SET nome=?,descricao=?,categoria=?,preco=?,minimo=?,fornecedor_id=? WHERE id=?",
                p.nome(),p.descricao(),p.categoria(),p.preco(),p.minimo(),p.fornecedorId(),id);
            return id;
        }
        var keys = new GeneratedKeyHolder();
        jdbc.update(c -> {
            var s=c.prepareStatement("INSERT INTO produto(nome,descricao,categoria,preco,quantidade,minimo,fornecedor_id) VALUES(?,?,?,?,0,?,?)", Statement.RETURN_GENERATED_KEYS);
            s.setString(1,p.nome()); s.setString(2,p.descricao()); s.setString(3,p.categoria());
            s.setBigDecimal(4,p.preco()); s.setInt(5,p.minimo()); s.setLong(6,p.fornecedorId()); return s;
        },keys);
        return keys.getKey().longValue();
    }
    public void atualizarSaldo(long id,int saldo) { jdbc.update("UPDATE produto SET quantidade=? WHERE id=?",saldo,id); }
    public void excluir(long id) { jdbc.update("DELETE FROM produto WHERE id=?",id); }
}
