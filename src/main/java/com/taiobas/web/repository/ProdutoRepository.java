package com.taiobas.web.repository;

import com.taiobas.web.domain.Produto;
import java.util.List;

public interface ProdutoRepository {
    List<Produto> listar();
    Produto buscar(long id, boolean bloquear);
    long salvar(Long id, Produto produto);
    void atualizarSaldo(long id, int saldo);
    void excluir(long id);
}
