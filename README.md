# Taioba — Controle de estoque

Aplicação web local para cadastrar produtos e fornecedores e acompanhar entradas, saídas e saldos. Utiliza Java 17, Spring Boot, Spring MVC REST, JDBC e banco H2 em arquivo.

## Executar

1. Instale **Java 17 ou superior**.
2. Extraia o ZIP completo em uma pasta com permissão de escrita.
3. No Windows, execute `INICIAR.bat`. No Linux/macOS, execute `sh iniciar.sh`.
4. Aguarde `Started TaiobaApplication` e abra **http://localhost:8085**.
5. Para encerrar, pressione `Ctrl+C` no terminal do servidor.

O pacote executável inclui o JAR com suas dependências. Não exige MySQL, NetBeans, Maven ou internet para funcionar.

| Perfil | Usuário | Senha | Permissões |
|---|---|---|---|
| Administrador | admin | admin123 | Cadastros, exclusões, consultas e movimentações |
| Operador | operador | operador123 | Consultas e movimentações |
| Auditor | auditor | auditor123 | Consultas |

As contas são fixas, com senhas codificadas em BCrypt. A sessão é individual por navegador, e as permissões são verificadas também no servidor.

## Como usar

- **Meu estoque:** busque produtos e filtre por categoria, fornecedor ou situação. O resumo mostra produtos cadastrados, unidades, valor total e itens para reposição.
- **Produtos:** cadastre nome, descrição, categoria, preço, estoque mínimo e fornecedor. Novos produtos começam com saldo zero; registre uma entrada para inserir o saldo inicial.
- **Fornecedores:** cadastre nome, CNPJ, e-mail e telefone. O CNPJ deve ser único e seguir o formato `12.345.678/0001-90`; os dígitos verificadores não são validados.
- **Movimentações:** registre entrada ou saída com quantidade positiva e observação opcional. O histórico registra usuário, data e horário. Saídas acima do saldo disponível são recusadas.
- **Exclusões:** produtos com saldo ou histórico e fornecedores vinculados a produtos são preservados.

Editar o cadastro de um produto não altera sua quantidade. O saldo só muda por movimentações.

## Banco e configuração

O banco é criado automaticamente em `data/taioba.mv.db`, na pasta de execução. Os scripts de inicialização usam a própria pasta do projeto. Os dados permanecem após reiniciar. Na primeira execução, são incluídos registros fictícios.

Não execute duas instâncias usando o mesmo banco. Para fazer backup, encerre a aplicação e copie a pasta `data`. Para restaurar, substitua essa pasta pelo backup com a aplicação parada.

O servidor aceita conexões apenas da própria máquina. Se a porta 8085 estiver ocupada, configure outra antes de executar:

```bat
set PORT=8086
INICIAR.bat
```

Nesse caso, acesse `http://localhost:8086`. As configurações ficam em `src/main/resources/application.properties`.

## Compilar e testar

No NetBeans, use **Arquivo → Abrir projeto**, selecione a pasta que contém `pom.xml` e execute **Limpar e Construir**. Depois, use o script de inicialização.

Com Maven no terminal:

```sh
mvn clean verify
java -jar target/taioba-web-1.0.0.jar
```

A primeira compilação exige internet para baixar dependências. `verify` executa os testes JUnit com banco em memória, isolado dos dados locais, e gera o JAR em `target`.

## Estrutura do código

| Pasta | Responsabilidade |
|---|---|
| `domain` | Modelos e regras de saldo e cálculo monetário |
| `repository` | Contrato e persistência JDBC de produtos |
| `service` | Operações de estoque com transação e bloqueio do produto |
| `api` | Endpoints HTTP, validação e mensagens de erro |
| `config` | Segurança, permissões e dados iniciais |
| `src/main/resources/static` | Interface HTML, CSS, JavaScript e marca |
| `src/test/java` | Testes de regras e integração |

A interface usa arquivos locais, sem bibliotecas, imagens ou fontes por CDN. Valores monetários utilizam `BigDecimal` no domínio e `DECIMAL` no banco.

## Documentação

- [Requisitos do sistema](docs/REQUISITOS.pdf).
- [Arquitetura e refatoração](docs/REFATORACAO.pdf).
- [Plano de testes](docs/PLANO_DE_TESTES.pdf).
- [Wireframes](docs/wireframes.html).
- [Resultados dos testes](docs/EVIDENCIAS.pdf).
- [Registro de problemas e correções](docs/BUGTRACKING.pdf).
