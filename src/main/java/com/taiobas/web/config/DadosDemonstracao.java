package com.taiobas.web.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Component
public class DadosDemonstracao implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    public DadosDemonstracao(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    @Override @Transactional public void run(ApplicationArguments args) {
        if(jdbc.queryForObject("SELECT COUNT(*) FROM fornecedor",Integer.class)>0) return;
        jdbc.update("INSERT INTO fornecedor(nome,cnpj,email,telefone) VALUES(?,?,?,?)","Tech Distribuidora","12.345.678/0001-90","contato@tech.example","(11) 98765-4321");
        jdbc.update("INSERT INTO fornecedor(nome,cnpj,email,telefone) VALUES(?,?,?,?)","Digital Parts","98.765.432/0001-10","vendas@digital.example","(11) 91234-5678");
        long fornecedor=jdbc.queryForObject("SELECT MIN(id) FROM fornecedor",Long.class);
        Object[][] itens={
            {"Mouse Gamer RGB","Precisão e conforto para cada movimento.","Periféricos","89.90",50,10},
            {"Teclado Mecânico","Switches táteis e iluminação para seu setup.","Periféricos","249.90",8,10},
            {"Monitor LED 24″","Mais espaço para trabalhar e criar.","Monitores","799.90",15,5},
            {"Headset Gamer","Som envolvente com microfone ajustável.","Áudio","199.90",25,10},
            {"Mousepad XL","Uma base ampla para suas ideias.","Acessórios","49.90",100,20},
            {"SSD 480 GB","Agilidade para os arquivos do dia a dia.","Componentes","299.90",4,5},
            {"Webcam Full HD","Clareza para conectar pessoas.","Periféricos","159.90",40,10},
            {"Memória RAM 8 GB","Fôlego extra para sua rotina.","Componentes","189.90",0,5}};
        for(Object[] p:itens) {
            jdbc.update("INSERT INTO produto(nome,descricao,categoria,preco,quantidade,minimo,fornecedor_id) VALUES(?,?,?,?,?,?,?)",p[0],p[1],p[2],new BigDecimal((String)p[3]),p[4],p[5],fornecedor);
            if((int)p[4]>0) jdbc.update("INSERT INTO movimentacao(produto_id,tipo,quantidade,observacao,usuario) VALUES((SELECT id FROM produto WHERE nome=?),'ENTRADA',?,'Saldo inicial de demonstração','sistema')",p[0],p[4]);
        }
    }
}
