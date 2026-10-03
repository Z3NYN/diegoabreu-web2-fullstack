# Auditoria da Nexus — 03/10/2026

## Parecer

Os requisitos técnicos funcionais da N1 estão implementados e as verificações abaixo passaram. A aplicação foi publicada no [repositório GitHub](https://github.com/Z3NYN/diegoabreu-web2-fullstack), preservando os quatro commits anteriores. Compartilhamento com o professor e avaliação da regularidade do histórico continuam pendentes. Java 21 é o alvo de compilação, mas a execução foi verificada no JDK 22 disponível.

A confirmação e a recuperação de senha têm implementação SMTP e testes de protocolo. Conta Brevo e remetente foram verificados, e as credenciais foram configuradas localmente com proteção do Windows. O autor confirmou o recebimento da mensagem e a confirmação da conta em uma caixa real. A aplicação não apresenta envio falso quando SMTP está desativado.

## Achados e correções

| Prioridade | Problema identificado | Correção e verificação |
| --- | --- | --- |
| Alta | CRUD acessível sem login | Sessão obrigatória para os três cadastros, CSRF nas escritas e login condicionado à confirmação do e-mail. Backend continua limitado a 127.0.0.1 por padrão. Testes de operações anônimas retornam 401. |
| Média | Identificador abc retornava 403 no lugar de 400 porque o processamento de erros era bloqueado | Resposta JSON para parâmetro inválido; dispatch de erro permitido sem liberar acesso direto a outros caminhos. Teste falhou antes e passou após correção. |
| Média | Preço com mais de duas casas podia ser arredondado silenciosamente; tamanho de campos excedia armazenamento | ProdutoService exige até duas casas e máximo 999999999,99; precisão do banco explícita. Nome até 120 e descrição até 255 no Service. Testes HTTP para entradas excessivas. |
| Média | Verificação de username duplicado dependia apenas de consulta, sujeita a concorrência | Username normalizado para minúsculas e restrição única no banco, mantendo verificação amigável no Service. E-mail já era normalizado e único. |
| Média | Falha ao recarregar a lista após POST/PUT/DELETE podia parecer falha da operação já concluída | Escrita bem-sucedida e atualização da lista têm tratamento separado; alerta solicita atualizar a lista sem sugerir repetir o cadastro. Correção inspecionada; não houve injeção de falha de rede no navegador. |
| Média | Timeout de 10 segundos da interface podia vencer antes dos timeouts SMTP | Timeout HTTP de 30 segundos; mensagem orienta consultar a lista antes de repetir após timeout. |
| Média | Dados de confirmação eram gravados somente após aceitar o envio SMTP | Dados e restrições são enviados ao banco com saveAndFlush antes de SMTP. Isso reduz links inválidos por erro de persistência, mas não torna banco e SMTP uma operação atômica. |
| Baixa | Regex HTML de username tinha hífen sem escape para o modo v do navegador | Escape corrigido; username com hífen validado no navegador e sem aviso de regex. |
| Baixa | Script SMTP restaurava só a senha, deixando configurações no terminal | Todas as variáveis alteradas são restauradas no finally; chave em entrada oculta. Sintaxe PowerShell validada, sem executar credenciais externas. |

## Verificações executadas nesta auditoria

- Maven package: **22 testes, zero falhas e zero erros**, JAR atualizado. Relatórios em target/surefire-reports.
- CRUD das três entidades, GET por id, atualização do registro existente, exclusão e erros 404: testes HTTP reais com servidor Spring e H2 em memória.
- Entrada JSON inválida, id não numérico, caminho inexistente, limites de nome/descrição/preço: testes HTTP.
- Senha e hash interno ocultos; tentativa de enviar emailConfirmado=true pelo cliente não confirma o e-mail; senha existente preservada no PUT: testes HTTP e banco.
- E-mail inválido, normalização, duplicidade, expiração, uso único, troca de endereço, falha SMTP e intervalo de reenvio: testes de integração do Service. Nestes testes, EmailService é substituído por um mock.
- Duas confirmações simultâneas com o mesmo token: apenas uma é aceita; teste de concorrência com duas threads e banco real de teste.
- EmailService envia confirmação e recuperação pelo protocolo SMTP a um servidor local de teste; assunto, destinatário, link e validade inspecionados. Não é teste de recebimento externo.
- Persistência em arquivo: Produto e Permissao criados pela API, servidor encerrado e reiniciado, dados recuperados e registros descartáveis removidos.
- Frontend: npm run build e npm run lint aprovados. npm audit e npm audit --omit=dev retornaram zero vulnerabilidades conhecidas no momento da consulta.
- Navegador: e-mail válido aceito; domínio sem ponto, dois @ e espaços rejeitados. Username com hífen aceito. Nenhum aviso/erro de script na consulta realizada.
- Página de confirmação com token inválido: erro explícito, sem falso sucesso.
- Responsividade: tela móvel inspecionada; largura do conteúdo medida em 390 e 1280 pixels sem transbordamento horizontal. Captura completa do desktop não ficou disponível pela ferramenta, portanto essa inspeção visual integral não é afirmada.
- Portas locais verificadas: 127.0.0.1:8080 e 127.0.0.1:5173.

## Revisão do checklist N1

| Área | Resultado |
| --- | --- |
| Spring Boot, Maven, groupId br.ueg.trindade, dependências Web/JPA/H2/PostgreSQL/DevTools/Security | Implementado; compilação release 21 verificada |
| React Vite TypeScript em src/main/frontend e npm run dev | Implementado; execução verificada |
| Usuario, Permissao, Produto com Entity/Id/GeneratedValue | Implementado; persistência testada |
| Senha oculta | Verificado |
| H2 e JpaRepository por entidade | Verificado |
| Controller → Service → Repository, pacotes separados, endpoints /api | Verificado por inspeção e testes |
| Regra de negócio no Service | Verificado |
| Axios services/api.ts, baseURL e CrossOrigin | Verificado |
| components: formulário controlado, listagem, item via props | Implementado e inspecionado |
| useState e useEffect | State nos formulários e páginas; Effect na página para buscar dados; key recria o formulário ao trocar edição, conforme aula |
| pages UsuariosPage/PermissoesPage/ProdutosPage | Configuram CadastroPage compartilhada; lógica permanece em pages |
| App apenas compondo páginas | AuthGate, confirmação de e-mail e redefinição de senha; sem CRUD em App |
| Editar/Excluir e lista atualizada | Implementado; CRUD no navegador das três entidades já verificado nesta sessão |
| Integração React → API → H2 e entidade própria em camadas | Verificado |
| GitHub, commits regulares e link compartilhado | Aplicação publicada em main e quatro commits anteriores preservados. Compartilhamento com o professor e regularidade do histórico não comprovados |

## Limites e pendências

1. Conta Brevo e remetente verificados, SMTP configurado localmente; recebimento da confirmação comprovado pelo autor. O script scripts/Iniciar-Com-Email.ps1 lê a configuração criptografada em .nexus/smtp.clixml, ignorada pelo Git. Credenciais devem ser fornecidas apenas localmente.
2. Runtime JDK 21 não disponível nesta verificação. Compilação release 21 e execução JDK 22 passaram.
3. Login foi adicionado após o escopo original da N1. Todas as contas autenticadas e confirmadas possuem acesso ao CRUD; o cadastro Permissao ainda não implementa controle por perfil ou proprietário. Exposição pública exige HTTPS, cookie Secure e revisão de autorização.
4. SMTP e banco não têm atomicidade conjunta nem fila de envio/retry persistente. Aceitação pelo servidor de e-mail não comprova entrega ao destinatário. Para operação pública, esses aspectos precisam ser evoluídos junto com autenticação e limitação de abuso.
5. Consulta de vulnerabilidades foi feita no npm; não foi executado scanner de CVEs das dependências Maven, auditoria de infraestrutura ou pentest externo.
6. Sintaxe do script validada e backend iniciado com as credenciais SMTP locais. A recuperação de lista após falha foi revisada em código, não testada por injeção de falha no navegador.

Os testes demonstram os comportamentos cobertos; não constituem garantia de ausência de todos os defeitos. Dados do usuário foram preservados, e os registros descartáveis da auditoria foram excluídos.

## Evolução de autenticação — 03/10/2026

Tela inicial de login, cadastro com senha, confirmação obrigatória e recuperação foram adicionados por solicitação do autor. Sessão armazenada no servidor, cookie HttpOnly/SameSite=Lax, CSRF habilitado, rotação do identificador da sessão no login e expiração por inatividade de 30 minutos. Senha BCrypt com custo 12; versões de credencial invalidam sessões após recuperação e troca de endereço. Campos de senha, tokens, hashes e versão não são expostos nas respostas.

Testes adicionais: bloqueio de GET/POST/PUT/DELETE anônimos nos três cadastros; login inválido ou sem confirmação; escrita sem CSRF; logout; invalidação de duas sessões após recuperação; senha antiga rejeitada; resposta genérica para endereço desconhecido; falha SMTP sem criar conta ou token; link expirado e usado; reenvio invalida o link anterior; duas redefinições concorrentes aceitam apenas uma. Os testes de fluxo usam EmailService substituído por mock; testes de protocolo SMTP cobrem as mensagens reais de confirmação e recuperação em servidor local isolado.

Limites de requisições são locais ao processo: 10 tentativas de login por minuto por IP e 20 solicitações dos demais fluxos públicos de autenticação por minuto por IP. Não há proteção distribuída entre múltiplas instâncias. Não há usuário inicial ou senha padrão, nem conversão automática de senhas antigas em texto simples para credenciais utilizáveis.

A conta Brevo e o remetente Nexus foram verificados. Credenciais foram salvas apenas em .nexus/smtp.clixml, criptografadas para o usuário Windows e ignoradas pelo Git. Backend foi iniciado com SMTP real. Nenhuma credencial foi colocada no repositório; o autor confirmou o recebimento externo e a ativação da conta. A chave compartilhada no chat deve ser substituída pelo autor no provedor e reconfigurada localmente.

## Diagnóstico de entrega e correção de endereço

O provedor registrou o e-mail de teste como entregue e aberto. A confirmação inicial foi enviada ao endereço digitado pelo usuário, que continha um erro no domínio; o provedor também registrou entrega nesse destinatário. Isso não comprova recebimento pela pessoa que fez o cadastro.

Foi adicionada a opção de corrigir o endereço de uma conta ainda não confirmada mediante a senha cadastrada, com CSRF e limite de tentativas. A senha permanece intacta; o link anterior é invalidado e uma nova confirmação é enviada. Dois testes adicionais verificam exigência de senha, CSRF, preservação da credencial, invalidação do link anterior e ausência de sessão autenticada antes de confirmar.

O formulário sugere conferir alguns domínios frequentemente digitados incorretamente, sem substituir automaticamente o endereço. A confirmação do e-mail continua sendo a prova de acesso à caixa postal. O autor confirmou que recebeu a nova mensagem e confirmou sua conta. A recuperação possui testes HTTP e SMTP aprovados; o recebimento desse segundo fluxo na caixa pessoal não foi verificado.

## Refinamento da experiência

Interface refinada com identidade Nexus, formulários e mensagens mais legíveis, navegação responsiva, busca local nos três cadastros, foco no formulário durante a edição e opção de mostrar/ocultar a senha. Ajuda de confirmação agrupada para reduzir distrações na entrada. A busca filtra os registros carregados; não substitui paginação no servidor.
