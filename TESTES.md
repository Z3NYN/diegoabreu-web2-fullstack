# Verificações executadas

Data: **09/10/2026**, execução local em Windows, com JDK Microsoft **21.0.12.1**, Maven **3.9.11**, Node.js **24.19.0** e npm **10.9.2**. Spring Boot **4.1.0**, conforme Aula 01. Foram testadas as fontes finais, incluindo DTOs, CORS e o ajuste pontual de dependência.

## Resultado

| Verificação | Resultado real |
| --- | --- |
| Maven `clean verify` | **BUILD SUCCESS**; compilação Java 21 e JAR executável gerado |
| Testes de integração do backend | **10 executados; 0 falhas; 0 erros; 0 ignorados** |
| `npm ci` e instalação limpa final `npm ci --offline` | Concluídos com o lock entregue |
| `npm run build` | TypeScript e Vite **8.2.2** concluídos |
| `npm run lint` | oxlint concluído, sem avisos ou erros |
| `npm audit` após patch | **0 vulnerabilidades reportadas** naquela execução |
| Aplicação pelo navegador Chrome | CRUD completo de **Usuario, Permissao e Produto**, aprovado |
| Integração real | React em 5173 → Axios → Spring em 8080 → H2 em arquivo, aprovada |
| Persistência após reiniciar o JAR | Produto cadastrado recuperado do mesmo arquivo H2 após reinício |

## O que os dez testes do backend verificam

A classe `src/test/java/br/ueg/trindade/braullyweb2fullstack/CrudIntegrationTest.java` usa Spring Boot, MockMvc, os filtros de Security e H2 de teste em memória. Não usa repositórios simulados.

- Um fluxo completo por entidade: POST, GET da lista, GET por ID, PUT, DELETE e consulta após exclusão.
- Usuario: senha recebida e persistida no POST; ausente no JSON de cadastro, listagem, consulta e atualização; preservada no PUT mesmo quando outra senha é enviada ou quando ela é omitida.
- Produto: preço negativo e nome vazio retornam 400; atualização inválida preserva o registro anterior; preço zero é aceito.
- Três casos parametrizados: GET, PUT e DELETE com ID inexistente retornam 404 para cada entidade.
- Dois casos parametrizados: preflight PUT e consulta permitidos para `localhost:5173` e `127.0.0.1:5173`, sem autenticação.
- Rest Repositories: repositórios não exportam endpoints alternativos que contornem os services.

Repita estes testes, na raiz do projeto, com:

```sh
mvn clean verify
```

## O que foi verificado no navegador

O JAR gerado foi executado com Java 21, simultaneamente ao servidor de desenvolvimento Vite. Um navegador Chrome temporário executou o fluxo abaixo para **cada** entidade, usando a interface e as requisições reais:

1. Preencher formulário e clicar em Salvar; confirmar POST 201.
2. Conferir o item na listagem e recarregar a página; confirmar que continua listado a partir da API.
3. Clicar em Editar; confirmar GET por ID e formulário preenchido.
4. Alterar os campos e salvar; confirmar PUT 200 e item atualizado na lista.
5. Clicar em Excluir; confirmar DELETE 204 e ausência do item, também após recarregar.

Foi conferido que a senha não aparece nos JSONs e que o formulário de edição de Usuario não possui campo de senha nem a envia no PUT. Produto foi editado para preço zero. Nenhum erro JavaScript foi capturado e as operações não receberam bloqueio de CORS ou Security.

Em seguida, foi cadastrado um Produto para teste de persistência, o processo do backend foi encerrado, o mesmo JAR foi reiniciado no mesmo diretório e o Produto foi consultado pelo mesmo ID. O arquivo efetivo foi `database.db.mv.db`, correspondente à URL das aulas. Todos os registros temporários foram removidos e os servidores de teste encerrados.

Para repetir manualmente, siga os comandos de execução do README e realize os cinco passos nas três páginas. Para verificar persistência, cadastre um Produto, encerre normalmente o backend, inicie novamente na mesma pasta e confira a listagem.

## Limites e ocorrências

- Uma execução no ambiente restrito ficou bloqueada na comunicação interna do agente de testes Java. Ela foi encerrada; o `clean verify` final foi repetido em execução autorizada e aprovou os dez testes. Essa tentativa interrompida não é contabilizada como teste aprovado.
- O encerramento forçado imediatamente após um cadastro não preservou esse último registro em uma tentativa inicial. A verificação final aguardou **1,5 segundo** entre o cadastro e o encerramento forçado, e passou. O teste confirma persistência após reinício nessas condições; não comprova tolerância a queda imediata de energia/processo.
- O lock inicial trouxe um aviso de audit em `source-map-js` 1.2.1. Somente esse pacote transitivo foi corrigido para 1.2.2; nenhuma versão declarada no `package.json` mudou. Build, lint, audit e instalação limpa foram conferidos após o ajuste.
- Não foi testado PostgreSQL: o banco desta etapa é H2. O driver exigido está incluído.
- Não há teste de login, JWT ou roteamento, pois essas funcionalidades não integram o escopo pedido.
- A publicação no GitHub depende da autenticação da conta Z3NYN e ainda não foi realizada na geração desta documentação. Nenhum histórico de commits anterior foi fabricado.

As saídas resumidas efetivas do Maven e da integração pelo navegador estão na pasta `verificacao/`. Os binários, o banco de teste, as ferramentas portáteis e os pacotes instalados não fazem parte do ZIP.
