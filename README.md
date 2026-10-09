# N1 — Programação Web II

Projeto baseado exclusivamente nos PDFs das Aulas 01–06 e no checklist `Aula_06.5-Avaliacao-N1.pdf`. O frontend enviado em `frontend.tar.gz` foi usado como ponto inicial, com as versões declaradas preservadas. O único ajuste de dependência no `package-lock.json` foi o patch de `source-map-js` de 1.2.1 para 1.2.2, compatível com a versão exigida pelo PostCSS, após um aviso concreto no audit.

## O que foi implementado

- Spring Boot 4.1.0, Maven, Java 21 e grupo `br.ueg.trindade`.
- Dependências Spring Web, Data JPA, Rest Repositories, H2, PostgreSQL Driver, DevTools e Spring Security.
- CRUDs de Usuario (`id`, `nome`, `username`, `senha`, `email`), Permissao (`id`, `nome`, `descricao`) e Produto (`id`, `nome`, `descricao`, `preco`).
- Camadas `model`, `repository`, `service` e `controller`; cada controller acessa somente seu service.
- Regra de negócio de Produto: preço não pode ser negativo. A validação fica no service.
- React + Vite + TypeScript em `src/main/frontend`, com Axios, componentes de formulário/listagem/item, páginas, props, `useState` e `useEffect`.
- Cadastro, listagem, edição e exclusão nas três páginas, com atualização da lista após as operações.
- DTOs de entrada e resposta para Usuario e `@JsonIgnore` na senha do modelo: senha recebida no cadastro e omitida de todas as respostas JSON. Conforme Aula 05, o PUT de Usuario preserva a senha cadastrada.

`App.tsx` apenas renderiza as três páginas. A configuração de Security libera as requisições deste estágio e desabilita CSRF para permitir POST/PUT/DELETE, sem implementar autenticação.

## Requisitos para executar

- JDK **21**; confira `java -version` e `javac -version`.
- Maven 3.9 ou superior; confira `mvn -version` e se ele utiliza o JDK 21.
- Node.js **22.12 ou superior**, ou **24**, e npm. A versão de Vite do frontend enviado exige um Node compatível.
- Portas 8080 (backend) e 5173 (frontend) disponíveis.

Não precisa instalar PostgreSQL para executar: o projeto usa H2, conforme a Aula 03. O driver PostgreSQL permanece como dependência exigida.

## Executar

Extraia o ZIP e abra um terminal dentro da pasta `projeto-n1`, onde está o `pom.xml`:

```sh
mvn clean verify
mvn spring-boot:run
```

Deixe o backend em execução. Em outro terminal, na mesma pasta do projeto:

```sh
cd src/main/frontend
npm ci
npm run dev -- --port 5173 --strictPort
```

Abra **http://localhost:5173**. O frontend chama **http://localhost:8080/api**. CORS permite `http://localhost:5173` e `http://127.0.0.1:5173`. Mantenha a porta 5173 para coincidir com a configuração.

O H2 grava o banco no diretório de execução do backend, com URL `jdbc:h2:file:./database.db`, usuário `sa` e senha vazia. Reiniciar o backend preserva os dados. Execute os comandos Maven sempre na raiz do projeto para manter o mesmo banco.

Para compilar e conferir o frontend:

```sh
npm run build
npm run lint
```

## API

| Recurso | Coleção | Registro |
| --- | --- | --- |
| Usuario | `/api/usuarios` | `/api/usuarios/{id}` |
| Permissao | `/api/permissoes` | `/api/permissoes/{id}` |
| Produto | `/api/produtos` | `/api/produtos/{id}` |

`GET` da coleção lista; `GET` por ID consulta; `POST` na coleção cadastra; `PUT` por ID atualiza; `DELETE` por ID exclui. Os IDs de consulta, atualização e exclusão são recebidos por `@PathVariable`. Um ID inexistente retorna 404 e preço negativo retorna 400.

Exemplos de corpo para POST:

```json
{"nome":"Ana","username":"ana","senha":"senha-de-estudo","email":"ana@example.com"}
```

```json
{"nome":"CADASTRAR","descricao":"Permissão de cadastro"}
```

```json
{"nome":"Caderno","descricao":"Caderno simples","preco":12.50}
```

Para atualizar Usuario, envie `nome`, `username` e `email`. A senha continua sendo a do cadastro, mesmo se um cliente enviar outro valor no PUT.

Este é o estágio acadêmico das Aulas 01–06: não há login, JWT ou controle de acesso e a senha é armazenada como no CRUD didático. Use dados fictícios ao demonstrar a atividade.

## Conferência dos materiais

| Material | Aplicação no projeto |
| --- | --- |
| Aula 01, páginas 12–16 | Spring Boot 4.1.0, Java 21, Maven, grupo, dependências e Vite/TypeScript |
| Aula 02, páginas 14–19 | Usuario, senha fora do JSON, Permissao e entidade própria |
| Aula 03, páginas 7–18 | JPA, JpaRepository, H2 em arquivo e operações REST |
| Aula 04, páginas 10–18 | Axios, CORS, props, estado, efeitos e erros de requisição |
| Aula 05, páginas 4–15 | Formulários, PUT/DELETE, atualização da lista e senha preservada no PUT |
| Aula 06, páginas 3–21 | Controller → Service → Repository, páginas e regra de negócio própria |
| Avaliação N1, páginas 2–4 | Checklist de ambiente, backend, frontend e integração |

A imagem do Spring Initializr seleciona **Java 25**: para esta avaliação, corrija para **21**. A imagem também não lista **Spring Security**, que deve ser acrescentado. Rest Repositories consta da Aula 01 e foi incluído. O nome do pacote Java utiliza `braullyweb2fullstack`, sem os hífens exibidos no campo da imagem, pois hífens não são válidos em identificadores Java.

## Publicar no GitHub

O projeto foi publicado em 09/10/2026 na conta **Z3NYN**, na branch [atividade-n1-braully](https://github.com/Z3NYN/diegoabreu-web2-fullstack/tree/atividade-n1-braully) do repositório existente `diegoabreu-web2-fullstack`. O [PR nº 1](https://github.com/Z3NYN/diegoabreu-web2-fullstack/pull/1) está em **rascunho para revisão**; esta entrega não foi incorporada à branch `main`.

Para obter exatamente esta versão pelo Git:

```sh
git clone --branch atividade-n1-braully https://github.com/Z3NYN/diegoabreu-web2-fullstack.git
cd diegoabreu-web2-fullstack
```

Depois, siga os comandos de execução acima. Ao compartilhar a atividade, envie o link da **branch** indicada, que contém este projeto testado, ou do PR.

Para publicar uma cópia em outro repositório vazio, o procedimento é:

1. No GitHub, crie um repositório vazio para a atividade, sem inicializar README ou `.gitignore`.
2. Abra um terminal na raiz `projeto-n1` e execute:

```sh
git init
git add .
git commit -m "Implementa atividade N1 de Programacao Web II"
git branch -M main
git remote add origin https://github.com/SEU_USUARIO/SEU_REPOSITORIO.git
git push -u origin main
```

Substitua a URL pelo endereço real do repositório e autentique-se quando solicitado. O `.gitignore` exclui o banco local, os pacotes instalados e os arquivos gerados pelos builds.

3. Confira no GitHub se as fontes, `pom.xml`, `package-lock.json` e estas instruções apareceram.
4. Copie o link da versão efetivamente publicada e envie na atividade N1 no Google Sala de Aula.
5. Em alterações futuras, faça commits reais e frequentes. O histórico de trabalho anterior não foi inventado; esta entrega não comprova commits regulares ao longo do semestre.

Os testes efetivamente executados e seus limites estão em [TESTES.md](TESTES.md).
