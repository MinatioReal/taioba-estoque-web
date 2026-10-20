# Testes e execução

Data: 20/10/2026 · Taioba Web

## Execução local

O pacote contém o JAR com as dependências e os scripts de inicialização. Requer Java 17 ou superior. O servidor atende em `http://localhost:8085`, e os dados são armazenados no arquivo `data/taioba.mv.db`.

## Testes Java

Comando: `mvn verify`. Resultado: 22 testes aprovados, sem falhas, erros ou testes ignorados.

- `RegrasEstoqueTest`: 9 testes de saldo, saída insuficiente, entradas inválidas, overflow, cálculo monetário e estoque mínimo.
- `EstoqueApiTest`: 13 testes de autenticação, autorização, CSRF, validação, movimentação e integridade dos dados.

Os testes usam H2 em memória, isolado do banco local. Os relatórios são gerados em `target/surefire-reports`; os resumos estão em `docs/evidencias`.

## Navegador

Microsoft Edge, em modo headless, com larguras de 1440 e 390 pixels. Foram aprovadas 15 verificações, sem erros de JavaScript. Resultados em `docs/evidencias/navegador.json`.

Fluxos verificados: login, catálogo, busca, estado vazio, filtros, cadastro de fornecedor e produto, entrada, saída inválida, saída válida, edição sem alteração de saldo, histórico, layout móvel sem rolagem horizontal e permissões do auditor.

As imagens anexas mostram as telas de acesso, catálogo e movimentações.

## Persistência

O JAR foi iniciado em diretório temporário, com H2 em arquivo. Após cadastrar um fornecedor pela API, o processo foi encerrado e iniciado novamente. O registro permaneceu disponível: dois fornecedores antes do cadastro e três depois do reinício. Resultado em `docs/evidencias/persistencia.json`.

## Problemas corrigidos

1. **Porta ocupada:** a aplicação não iniciou quando a porta 8080 já estava em uso. O padrão passou para 8085, com alternativa pela variável `PORT` ou argumento `--server.port`.
2. **Gravação no encerramento abrupto:** o atraso de escrita do H2 podia perder o último cadastro confirmado. A configuração `WRITE_DELAY=0` foi aplicada, e o teste de persistência passou após o ajuste.

Os registros e as verificações estão em `BUGTRACKING.pdf` e `docs/evidencias/bugtracking-arquivado.json`. As referências às issues nesse arquivo são históricas.

## Limitações

As contas são fixas para demonstração. Não há gestão de usuários, pagamentos ou comércio eletrônico. Permanecem pendentes o teste dedicado de concorrência e a revisão completa de acessibilidade por teclado. As movimentações usam transação e bloqueio do produto.
