# Requisitos — Taioba Web

20/10/2026

## Objetivo e origem

Evoluir o STOCKSTaiobistas de Java Swing para uma aplicação web local de controle de estoque. Preservar os conceitos de produto, fornecedor, movimentação, saldo e permissões do desktop. A identidade visual usa a taioba como referência; a apresentação do catálogo é inspirada na organização de marketplaces.

## Requisitos funcionais

| ID | Requisito | Critério de aceite |
|---|---|---|
| RF01 | Autenticar usuário e encerrar sessão | Credenciais corretas dão acesso; logout encerra a sessão |
| RF02 | Restringir operações por perfil | Administrador gerencia; operador movimenta; auditor consulta |
| RF03 | Cadastrar e editar produtos | Nome, descrição, categoria, preço, mínimo e fornecedor persistidos |
| RF04 | Cadastrar e editar fornecedores | Nome, CNPJ, e-mail e telefone persistidos; CNPJ único |
| RF05 | Registrar entrada | Quantidade positiva soma saldo e cria histórico com usuário e horário |
| RF06 | Registrar saída | Quantidade positiva subtrai saldo; saldo negativo recusado |
| RF07 | Consultar produtos | Busca por nome/descrição/fornecedor, categoria, situação e fornecedor |
| RF08 | Exibir resumo | Total de produtos, unidades, valor e produtos para reposição |
| RF09 | Consultar histórico | Produto, tipo, quantidade, data, responsável e observação |
| RF10 | Preservar integridade na exclusão | Produtos com saldo/histórico e fornecedores vinculados não são apagados |

## Requisitos não funcionais

- RNF01: executar localmente com Java 17+, sem serviços externos ou CDN.
- RNF02: manter os dados em banco H2 em arquivo via JDBC.
- RNF03: abrir no NetBeans como projeto Maven.
- RNF04: interface responsiva, rótulos nos campos, navegação por teclado e feedback de erros.
- RNF05: regras verificadas no servidor, transação de estoque, sessão individual e proteção CSRF.
- RNF06: usar BigDecimal nas regras financeiras e DECIMAL no banco.

## Limites desta versão

Não contempla vendas, checkout, pagamentos, importação automática do MySQL, cadastro de usuários nem relatórios fiscais. Três contas de demonstração são fixas. O CNPJ é validado pelo formato e pela unicidade. O acesso é limitado à própria máquina.
