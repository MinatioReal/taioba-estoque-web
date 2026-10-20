# Separação de responsabilidades e refatoração

Origem: STOCKSTaiobistas desktop, pasta `atv1/StocksTaiobistas`. Destino: projeto Maven Java Web `taioba-web`.

## Princípios aplicados

| Princípio | Classes afetadas | Justificativa |
|---|---|---|
| Responsabilidade única (SRP) | `RegrasEstoque`, `EstoqueService`, `JdbcProdutoRepository`, `ErrosApi`, `SecurityConfig` | Cálculo, transação, persistência, tradução de erros HTTP e segurança possuem responsabilidades próprias |
| Inversão de dependência (DIP) | `ProdutoRepository`, `JdbcProdutoRepository`, `EstoqueService` | O serviço depende de um contrato de persistência de produto, fornecido por construtor |
| Aberto/fechado (OCP), no ponto de extensão | `ProdutoRepository` | Outra implementação de persistência pode substituir a JDBC sem alterar as regras puras de estoque |

Não se afirma aplicação de todos os princípios SOLID. O serviço de estoque ainda usa JdbcTemplate para registrar histórico, e os endpoints de fornecedores concentram operações pequenas de CRUD: são limitações assumidas da implementação básica.

## Refatorações e mudanças de comportamento

| Origem | Destino | Alteração e motivo |
|---|---|---|
| `TelaConsultaEstoque.carregarEstoque`, `atualizarResumoFiltrado` | `RegrasEstoque.valorTotal` e resumo do catálogo | Regra financeira isolada e testável; BigDecimal substitui double no domínio |
| `EstoqueController.registrarEntrada/registrarSaida` | `RegrasEstoque.calcularSaldo` e `EstoqueService.movimentar` | Validação e cálculo separados da interface; transação com bloqueio protege operações concorrentes |
| `TelaProdutos.salvarProduto` | DTO `ProdutoInput`, serviço e repositório | Validação HTTP no servidor; edição não altera diretamente o saldo; novo produto começa zerado |
| `ProdutoDAO` | `ProdutoRepository` e `JdbcProdutoRepository` | Contrato, injeção pelo construtor e JdbcTemplate eliminam repetição de fechamento manual |
| `SessaoUsuario` Singleton | Spring Security e sessão HTTP | Usuário deixa de ser global e passa a ser individual por sessão |
| `UsuarioDAO.autenticar` | `SecurityConfig` | Contas locais de demonstração com BCrypt e autorização no servidor |
| `ConexaoMySQL` com credenciais fixas | configuração do DataSource H2 | Elimina instalação externa de banco na execução local; persistência em arquivo |
| Exclusão com cascata de histórico | `EstoqueService.excluir` e chaves estrangeiras restritivas | Preserva histórico de movimentação |

Padrões usados: **Repository** para acesso a produtos; **Service Layer** para coordenação transacional; **Dependency Injection** via construtores. O domínio não importa Swing, HTTP, JDBC ou Spring.
