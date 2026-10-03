# Checklist N1 — Diego Abreu

Referência: Avaliação N1 — Checklist e Aulas 01 a 06. Verificado em 03/10/2026. Implementado = presente no código; verificado = inspecionado ou executado; pendente = falta evidência ou ação externa.

| Item | Estado | Arquivos e evidência |
| --- | --- | --- |
| GitHub criado, link compartilhado, commits regulares | Repositório e publicação verificados; compartilhamento externo pendente | [Repositório](https://github.com/Z3NYN/diegoabreu-web2-fullstack) acessível; aplicação publicada em main, mantendo os quatro commits anteriores. Pasta local conectada a origin/main. Compartilhamento com o professor não verificado; regularidade ao longo das aulas depende da avaliação do histórico. |
| Spring Boot Maven Java 21 br.ueg.trindade e dependências | Implementado e compilação verificada | pom.xml e Wrapper; release 21, testes no JDK 22. Runtime JDK 21 não verificado. |
| React Vite TypeScript em src/main/frontend, npm run dev | Implementado e verificado | package.json; frontend iniciado e acessado no navegador na porta 5173. |
| Usuario, Permissao e entidade própria com Entity, Id e GeneratedValue | Implementado e verificado | model/; CRUD HTTP das três entidades persistido em JPA. |
| Senha oculta nas respostas | Implementado e verificado | Usuario.java, JsonIgnore; testes de resposta e preservação da senha no PUT. |
| H2 em application.properties e JpaRepository por entidade | Implementado e verificado | resources/application.properties, repository/; produto permaneceu após reinício real. |
| Pacotes controller, service, repository e model | Implementado e inspecionado | src/main/java/br/ueg/trindade/diego_web2_fullstack. |
| Controller REST /api com GET POST PUT DELETE, PathVariable, apenas Service | Implementado e verificado | Três controllers; testes das cinco operações por recurso e 404. |
| Regra de negócio no Service | Implementado e verificado | ProdutoService: nome obrigatório, preço não negativo; testes POST/PUT e erro de validação apresentado no navegador. |
| Axios services/api.ts, baseURL e CrossOrigin | Implementado e verificado | services/api.ts, controllers, SecurityConfig; preflight e integração no navegador. |
| components: listagem, item via props, formulário controlado e hooks | Implementado e verificado | CadastroList, CadastroItem, CadastroForm; useState e useEffect em CadastroPage para carregar API. |
| pages: UsuariosPage PermissoesPage e página própria | Implementado e verificado | Páginas configuram CadastroPage, que reutiliza lógica e estados das telas. |
| App apenas renderizando páginas | Implementado e inspecionado | App compõe AuthGate, ConfirmarEmailPage ou RedefinirSenhaPage; AuthGate libera Layout após validar a sessão. Sem CRUD em App. |
| Editar e Excluir recarregam lista | Implementado e verificado no navegador | CadastroPage recarrega após POST/PUT/DELETE. |
| CRUD no navegador das três entidades | Implementado e verificado | Cadastro → listagem → edição → exclusão executado em Usuario, Permissao e Produto; registros descartáveis removidos. |
| React → API → H2 integrado | Implementado e verificado | Servidores simultâneos, operações no navegador e reinício do backend. |
| Entidade própria CRUD em camadas | Implementado e verificado | Produto, ProdutoRepository, ProdutoService, ProdutoController, ProdutosPage. |

## Evidências de execução

- Maven package: JAR gerado; 22 testes aprovados, zero falhas. Relatórios em target/surefire-reports após Maven.
- npm ci concluído; compilação TypeScript/Vite e lint sem avisos.
- CRUD manual no navegador das três entidades.
- Erro do service apresentado no frontend e persistência após reinício. Layout inspecionado em desktop e viewport de 390 × 844.
- README contém comandos para repetir as verificações.

O repositório, seu histórico e a publicação da aplicação foram verificados. Compartilhamento com o professor e avaliação da regularidade dos commits permanecem pendentes externos. A execução em JDK 21 é a limitação de ambiente registrada.


## Evolução solicitada: Nexus e confirmação de e-mail

Marca Nexus e logo no cabeçalho e favicon. Validação do e-mail no servidor e frontend, bloqueio de duplicidade e confirmação por link SMTP com token de uso único, expiração de 24h, reenvio com intervalo e invalidação quando o endereço muda. Campos internos não são expostos ao cliente.

SMTP externo configurado localmente com remetente verificado. O autor confirmou o recebimento da mensagem e a ativação da conta; instruções e script de configuração no README.

Validação adicional: envio pelo protocolo SMTP a um servidor local de teste aprovado. Entrega externa de confirmação pela Brevo também comprovada pelo autor.

Auditoria ampliada registrada em AUDITORIA.md: correções de validação, erros HTTP, interface, script SMTP e isolamento local. A consulta anterior ao npm não apontou vulnerabilidades conhecidas.

## Evolução solicitada: acesso autenticado e recuperação

- Tela de login como entrada da plataforma; conta não confirmada não pode entrar.
- Registro com senha BCrypt, confirmação por e-mail e reenvio público por endereço.
- API exige sessão também para acesso direto ao CRUD; proteção CSRF e cookie HttpOnly/SameSite=Lax.
- Recuperação por SMTP com token aleatório de uso único, hash no banco, validade de 30 minutos e reenvio com invalidação do link anterior.
- Redefinição da senha, troca de e-mail e exclusão da conta invalidam o acesso das sessões anteriores.
- Testes HTTP e de concorrência aprovados; confirmação e recuperação enviadas a servidor SMTP local de teste.
- Correção de endereço para conta ainda não confirmada exige a senha existente, preserva a credencial e invalida o link anterior. Formulário sugere conferir domínios com erros comuns.

Conta Brevo e remetente Nexus verificados com o endereço autorizado pelo autor. SMTP configurado localmente; chave protegida pelo Windows em .nexus/smtp.clixml, fora do Git. E-mail de teste registrado como entregue e aberto no provedor. Após corrigir o endereço digitado incorretamente, o autor confirmou o recebimento da mensagem e a ativação da conta. O recebimento externo da recuperação ainda não foi verificado.
