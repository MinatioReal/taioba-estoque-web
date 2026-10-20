# Plano básico de testes

Data: 20/10/2026 · 

Ambiente: Java 17, Spring Boot 3.5, H2; navegador Microsoft Edge. JUnit utiliza H2 em memória, isolado do banco local. Para os manuais, usar uma instalação de teste e contas de demonstração. Não usar dados pessoais reais.

| ID | Requisito / cenário | Procedimento | Resultado esperado | Verificação |
|---|---|---|---|---|
| CT01 | Login válido/inválido | Entrar como admin e tentar senha errada | Acesso apenas com senha correta | JUnit e navegador para válido |
| CT02 | Permissões | Consultar como auditor e tentar cadastrar/movimentar pela API | Consulta permitida; escrita 403 | JUnit e navegador |
| CT03 | Produto novo | Cadastrar com fornecedor e preço válido | Produto salvo com saldo zero | Navegador |
| CT04 | Campos inválidos | Enviar nome vazio e preço negativo | Servidor responde 400 | JUnit |
| CT05 | Entrada | Movimentar +5 | Saldo soma 5, histórico contém usuário | JUnit e navegador |
| CT06 | Saída válida | Movimentar -2 com saldo 5 | Saldo 3 e histórico criado | JUnit da regra e navegador |
| CT07 | Saída inválida | Tentar sair mais que saldo | Erro; saldo e histórico inalterados | JUnit e navegador |
| CT08 | Quantidade inválida | Enviar zero/negativo | Operação recusada | JUnit da regra |
| CT09 | Cálculo monetário | Preço 0,10 e quantidade 3 | Total exatamente 0,30 | JUnit |
| CT10 | Filtros | Buscar teclado; buscar inexistente; filtrar zerados | Lista correta e estado vazio | Navegador |
| CT11 | Fornecedor | Cadastrar nome, CNPJ formatado, e-mail e telefone | Fornecedor aparece na lista | Navegador |
| CT12 | Exclusão protegida | Excluir fornecedor vinculado/produto com saldo | Conflito ou erro, dados preservados | JUnit |
| CT13 | Responsividade | Abrir catálogo em 390px e 1440px | Conteúdo legível, sem overflow horizontal | Navegador |
| CT14 | Persistência | Registrar entrada, parar e iniciar o JAR | Dados e saldo permanecem | Teste HTTP com reinício, registrar evidência |
| CT15 | Edição | Alterar preço de produto movimentado | Preço muda, saldo permanece | Navegador |
| CT16 | Segurança CSRF | POST autenticado sem token | Resposta 403 | JUnit |
| CT17 | Concorrência | Duas saídas simultâneas competindo pelo mesmo saldo | Nunca haver saldo negativo; apenas saídas possíveis são aceitas | Pendente de execução dedicada |
| CT18 | Teclado/acessibilidade | Navegar login, catálogo e modais sem mouse | Foco visível, rótulos corretos e modal operável | Revisão manual pendente |

## Critério de aceite e evidências

Os testes pendentes devem ser executados antes de ampliar o uso da aplicação. Relatórios JUnit ficam em `target/surefire-reports`; resultados e capturas do navegador ficam em `docs/evidencias`. Registrar falhas no GitHub Issues, com reprodução, resultado esperado, resultado observado e correção, antes de encerrar o registro.
