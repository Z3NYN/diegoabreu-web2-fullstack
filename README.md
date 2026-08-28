# Diego Web 2 - Full Stack

Projeto desenvolvido para a disciplina de **Programação Web II**, utilizando **Spring Boot** no backend e **React** no frontend.

O projeto tem como objetivo aplicar conceitos de desenvolvimento de aplicações web, criação de APIs REST, organização de código e integração entre backend e frontend.

---

## 🚀 Tecnologias utilizadas

### Backend
- Java 25
- Spring Boot
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Maven

### Banco de dados
- H2
- PostgreSQL

### Frontend
- React 19
- Vite
- TypeScript

---

## 📁 Estrutura do projeto

```text
diego-web2-fullstack/
│
├── src/
│   └── main/
│       └── java/
│           └── br/
│               └── ueg/
│                   └── trindade/
│                       └── diego_web2_fullstack/
│                           │
│                           ├── config/
│                           │   └── SecurityConfig.java
│                           │
│                           ├── controller/
│                           │   ├── PermissaoController.java
│                           │   ├── ProdutoController.java
│                           │   └── UsuarioController.java
│                           │
│                           ├── model/
│                           │   ├── Permissao.java
│                           │   ├── Produto.java
│                           │   └── Usuario.java
│                           │
│                           └── DiegoWeb2FullstackApplication.java
│
├── frontend/
│
├── pom.xml
└── README.md
🔌 API REST

Atualmente o backend possui endpoints para consulta de permissões, produtos e usuários.

Permissões
GET /permissoes

Retorna uma lista de permissões cadastradas.

Exemplo:

[
  {
    "id": 1,
    "nome": "Administrador",
    "descricao": "Permissão para administrar o sistema"
  },
  {
    "id": 2,
    "nome": "Usuário",
    "descricao": "Permissão para acessar funcionalidades básicas"
  }
]
Produtos
GET /produtos

Retorna uma lista de produtos.

Exemplo:

[
  {
    "id": 1,
    "nome": "Notebook",
    "preco": 3500.0
  },
  {
    "id": 2,
    "nome": "Mouse Gamer",
    "preco": 150.0
  },
  {
    "id": 3,
    "nome": "Teclado Mecânico",
    "preco": 280.0
  }
]
Usuários
GET /usuarios

Retorna uma lista de usuários.

Por questões de segurança, o campo senha não é exposto na resposta JSON.

🔐 Segurança

O projeto utiliza Spring Security para controle de acesso aos endpoints.

Além disso, o atributo de senha dos usuários utiliza @JsonIgnore, impedindo que a senha seja enviada nas respostas da API.

Exemplo:

@JsonIgnore
private String senha;

Dessa forma, mesmo que o objeto Usuario possua uma senha internamente, ela não será exibida no JSON retornado pela API.

Atualmente a proteção da senha está implementada no nível de exposição dos dados. A implementação de autenticação completa e armazenamento seguro de credenciais poderá ser evoluída posteriormente.

▶️ Como executar o projeto
Backend

Clone o repositório:

git clone https://github.com/Z3NYN/diego-web2-fullstack.git

Entre na pasta:

cd diego-web2-fullstack

Execute o Spring Boot:

Windows
.\mvnw.cmd spring-boot:run

O backend ficará disponível em:

http://localhost:8080
Testando a API

Após iniciar o servidor, acesse:

http://localhost:8080/permissoes
http://localhost:8080/produtos
http://localhost:8080/usuarios
🎓 Disciplina

Programação Web II

Projeto acadêmico desenvolvido como parte das atividades da disciplina.

Universidade Estadual de Goiás — UEG

👨‍💻 Autor

Diego Abreu

Estudante de Sistemas de Informação.

📌 Status do projeto

🚧 Em desenvolvimento

O projeto será evoluído durante a disciplina, incorporando novos recursos de backend, frontend, banco de dados, autenticação e integração entre as aplicações.


### Eu faria mais uma coisa

Na descrição curta do próprio repositório, aquela que aparece logo abaixo do nome no GitHub, coloque:

> **Projeto Full Stack desenvolvido na disciplina de Programação Web II utilizando Spring Boot, React 19 e API REST.**

E nos **Topics** do GitHub, coloque:

```text
java
spring-boot
spring-security
spring-web
react
react19
typescript
vite
rest-api
full-stack
maven
postgresql
ueg