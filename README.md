# Nexus

**Gestão de usuários, permissões e produtos em uma aplicação full stack.**

Projeto de **Diego Abreu**, desenvolvido para a avaliação N1 de **Programação Web II — UEG**, com base nas Aulas 01 a 06. A interface reúne os três cadastros, com criação, consulta, edição e exclusão integradas à API e ao banco de dados.

## Funcionalidades

- CRUD completo de usuários, permissões e produtos.
- Formulários controlados, validação de campos e mensagens de erro.
- E-mail e username únicos, com normalização dos dados no servidor.
- Produtos com preço não negativo, até duas casas decimais e representação `BigDecimal` no backend.
- Persistência em arquivo H2, mantendo os registros após reiniciar a aplicação.
- Interface responsiva com identidade visual Nexus.
- Confirmação de e-mail por SMTP, quando configurado, com token de uso único e expiração em 24 horas.

## Tecnologias

| Camada | Tecnologias |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1.1, Maven Wrapper, Spring Web MVC |
| Persistência | Spring Data JPA e H2; driver PostgreSQL disponível |
| Dependências adicionais | Spring Security, DevTools, Validation e Mail |
| Frontend | React, TypeScript, Vite e Axios |
| Verificação | Testes de integração HTTP, JPA e SMTP; TypeScript e Oxlint |

O `groupId` é `br.ueg.trindade`, e o pacote principal é `br.ueg.trindade.diego_web2_fullstack`. As versões das dependências estão em `pom.xml` e `src/main/frontend/package-lock.json`.

## Executar localmente

### Pré-requisitos

- JDK 21, com `JAVA_HOME` e `PATH` configurados.
- Node.js 22.12 ou superior da linha 22, ou Node.js 24, com npm.
- Internet na primeira instalação das dependências.

Maven e PostgreSQL não precisam ser instalados: o projeto inclui o Maven Wrapper e utiliza H2 por padrão.

Clone o repositório e entre na pasta:

```powershell
git clone https://github.com/Z3NYN/diegoabreu-web2-fullstack.git
cd diegoabreu-web2-fullstack
```

### 1. Iniciar o backend

Na pasta que contém `pom.xml`, execute no PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

### 2. Iniciar o frontend

Em outro terminal, a partir da raiz do projeto:

```powershell
cd src/main/frontend
npm ci
npm run dev
```

### 3. Acessar a aplicação

Abra [http://localhost:5173](http://localhost:5173). A API utiliza `http://localhost:8080/api`. Mantenha os dois terminais em execução.

No Linux ou macOS, substitua `.\mvnw.cmd` por `./mvnw`; se necessário, conceda permissão com `chmod +x mvnw`.

O frontend utiliza a porta 5173 fixa. Se ela estiver ocupada, encerre a instância anterior. Para alterar o endereço da API, copie `src/main/frontend/.env.example` para `.env.local` e ajuste `VITE_API_URL`. Uma mudança na origem do frontend também exige ajustar o CORS no backend.

## Arquitetura

```text
React → Controller → Service → Repository → H2
```

Os controllers tratam as requisições e acessam apenas os services. Os services concentram as regras de negócio, e cada entidade possui um `JpaRepository`. No frontend, as páginas coordenam as operações e reutilizam componentes de formulário, listagem e item com dados recebidos por props.

```text
src/
├── main/
│   ├── java/br/ueg/trindade/diego_web2_fullstack/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   ├── resources/application.properties
│   └── frontend/
│       ├── public/
│       └── src/
│           ├── components/
│           ├── pages/
│           ├── services/api.ts
│           └── types/
└── test/java/
scripts/Iniciar-Com-Email.ps1
```

## API

Os recursos disponíveis são `usuarios`, `permissoes` e `produtos`.

| Método | Endpoint | Resultado |
| --- | --- | --- |
| GET | `/api/{recurso}` | Lista de registros — 200 |
| GET | `/api/{recurso}/{id}` | Registro pelo identificador — 200 |
| POST | `/api/{recurso}` | Cadastro — 201 |
| PUT | `/api/{recurso}/{id}` | Atualização — 200 |
| DELETE | `/api/{recurso}/{id}` | Exclusão, sem corpo — 204 |

Dados inválidos retornam 400; registros inexistentes, 404; conflitos de unicidade, 409. As mensagens de erro são devolvidas em JSON no campo `message`.

Exemplos de corpo para cadastro:

**Usuário**

```json
{"nome": "Ana Silva", "username": "ana.silva", "email": "ana@example.com"}
```

**Permissão**

```json
{"nome": "Consulta", "descricao": "Consultar registros"}
```

**Produto**

```json
{"nome": "Teclado", "preco": 99.90}
```

A entidade `Usuario` contém o campo `senha`, oculto nas respostas por `@JsonIgnore`. O formulário da N1 utiliza nome, username e e-mail; este contrato não cadastra credenciais nem altera uma senha existente durante a edição.

## Banco de dados

A configuração padrão está em `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:file:./data/database
spring.jpa.hibernate.ddl-auto=update
```

Execute o backend sempre pela raiz do projeto para utilizar o mesmo arquivo `data/database.mv.db`. Esse banco e os artefatos de compilação ficam fora do Git. Os testes utilizam bancos em memória isolados. O console H2 permanece desativado.

## Confirmação de e-mail

Esta funcionalidade adicional requer uma conta SMTP e um remetente verificado. Por padrão, o envio está desativado, e o usuário aparece como **pendente de envio**. A aplicação não simula uma confirmação entregue.

Quando habilitado, o cadastro ou a troca de endereço envia um link. O token é armazenado apenas como hash, expira em 24 horas e é invalidado após uso ou reenvio. O intervalo mínimo entre reenvios é de 60 segundos. A página solicita um clique explícito para confirmar o endereço.

| Método | Endpoint | Ação |
| --- | --- | --- |
| POST | `/api/usuarios/{id}/confirmacao-email` | Enviar ou reenviar a confirmação |
| POST | `/api/usuarios/confirmar-email` | Confirmar com o corpo `{"token":"token-do-link"}` |

### Configuração com Brevo

1. Crie uma conta na Brevo, verifique o remetente e habilite o envio transacional conforme as instruções do provedor.
2. Obtenha o login SMTP e uma chave SMTP na seção SMTP & API.
3. Encerre o backend que estiver rodando e execute, na raiz:

```powershell
.\scripts\Iniciar-Com-Email.ps1
```

O script solicita a chave de forma oculta e configura o backend apenas no processo atual, usando SMTP com STARTTLS. Mantenha o frontend em outro terminal. Não coloque credenciais no código, no frontend ou no Git.

Outros provedores podem ser configurados com estas variáveis de ambiente:

| Variável | Finalidade |
| --- | --- |
| `NEXUS_EMAIL_ENABLED` | `true` para habilitar o envio |
| `NEXUS_EMAIL_FROM` | Endereço do remetente verificado |
| `SMTP_HOST` / `SMTP_PORT` | Servidor SMTP e porta; padrão de porta 587 |
| `SMTP_USERNAME` / `SMTP_PASSWORD` | Credenciais SMTP |
| `SMTP_AUTH` / `SMTP_STARTTLS` | Autenticação e TLS; padrão `true` |
| `NEXUS_FRONTEND_URL` | Endereço para o link; padrão `http://localhost:5173` |

O endereço `localhost` funciona apenas no computador em que a aplicação está rodando. Aceitação pelo SMTP não garante chegada à caixa de entrada. O envio local pelo protocolo foi testado; **a entrega por um provedor externo ainda depende da configuração e de uma verificação real**.

Referência: [documentação SMTP da Brevo](https://help.brevo.com/hc/en-us/articles/7924908994450-Send-transactional-emails-using-Brevo-SMTP).

## Verificação

Na raiz:

```powershell
.\mvnw.cmd verify
```

No frontend:

```powershell
cd src/main/frontend
npm run build
npm run lint
```

Na auditoria de **03/10/2026**, os **12 testes** passaram, assim como a compilação e o lint do frontend. Os testes cobrem CRUD, regras de negócio, respostas de erro, dados sensíveis ocultos e confirmação de e-mail, incluindo expiração, uso único, concorrência e falha SMTP. A persistência em arquivo e o CRUD das três entidades também foram verificados com a aplicação em execução.

O projeto compila para Java 21. A execução disponível nesta auditoria utilizou JDK 22; a execução especificamente no JDK 21 ainda precisa ser confirmada.

## Escopo acadêmico

Esta entrega atende ao escopo técnico da N1: aplicação em camadas, entidade própria `Produto` e integração React → API → H2. A confirmação de e-mail é uma evolução adicional solicitada para a Nexus.

O CRUD local não exige login. Spring Security e CORS estão configurados para a execução acadêmica, com backend limitado a `127.0.0.1` por padrão. JWT, upload, deploy e relacionamentos adicionais ficam fora deste escopo. A confirmação de e-mail não substitui autenticação ou autorização.

Consulte o [checklist da avaliação](CHECKLIST-N1.md) e o [relatório de auditoria](AUDITORIA.md) para as evidências e pendências. O compartilhamento do link com o professor e a avaliação do histórico de commits fazem parte da entrega acadêmica.

## Autor

**Diego Abreu** · Programação Web II · Universidade Estadual de Goiás.
