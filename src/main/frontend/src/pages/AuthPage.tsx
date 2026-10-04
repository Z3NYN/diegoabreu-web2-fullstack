import { useRef, useState } from 'react';
import type { FormEvent } from 'react';
import api, { mensagemErro } from '../services/api';
type Tela = 'login' | 'cadastro' | 'recuperacao' | 'confirmacao' | 'correcao';
export default function AuthPage({ onEntrar, mensagem }: { onEntrar: (conta: { id: number; nome: string; email: string }) => void; mensagem: string }) {
  const [tela, setTela] = useState<Tela>('login');
  const [nome, setNome] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [novoEmail, setNovoEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [repeticao, setRepeticao] = useState('');
  const [mostrarSenha, setMostrarSenha] = useState(false);
  const [ocupado, setOcupado] = useState(false);
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');
  const bloqueado = useRef(false);
  const titulo = { login: 'Bem-vindo de volta', cadastro: 'Crie sua conta', recuperacao: 'Recupere seu acesso', confirmacao: 'Confirme seu e-mail', correcao: 'Corrija seu e-mail' }[tela];
  const dominioDigitado = (tela === 'correcao' ? novoEmail : email).split('@')[1]?.toLowerCase();
  const sugestoes: Record<string, string> = { 'gmai.com': 'gmail.com', 'gmal.com': 'gmail.com', 'gmial.com': 'gmail.com', 'gmail.con': 'gmail.com', 'hotmai.com': 'hotmail.com', 'outlok.com': 'outlook.com' };
  const sugestao = dominioDigitado && sugestoes[dominioDigitado];
  function navegar(destino: Tela) { setTela(destino); setSenha(''); setRepeticao(''); setMostrarSenha(false); setErro(''); setSucesso(''); }
  async function enviar(event: FormEvent) {
    event.preventDefault(); if (bloqueado.current) return;
    setErro(''); setSucesso('');
    if (tela === 'cadastro' && (senha !== repeticao || new TextEncoder().encode(senha).length > 72)) { setErro('As senhas devem ser iguais e ter até 72 bytes.'); return; }
    bloqueado.current = true; setOcupado(true);
    try {
      if (tela === 'login') {
        const { data } = await api.post('/auth/login', { identificador: email, senha });
        setSenha(''); onEntrar(data);
      } else if (tela === 'correcao') {
        const { data } = await api.post('/auth/corrigir-email', { identificador: email, senha, email: novoEmail });
        setSucesso(data.message); setSenha('');
      } else {
        const endpoint = { cadastro: '/auth/registrar', recuperacao: '/auth/recuperacao', confirmacao: '/auth/reenviar-confirmacao' }[tela];
        const { data } = await api.post(endpoint, tela === 'cadastro' ? { nome, username, email, senha } : { email });
        setSucesso(data.message); setSenha(''); setRepeticao('');
      }
    } catch (error) { setErro(mensagemErro(error)); }
    finally { bloqueado.current = false; setOcupado(false); }
  }
  return <main className="auth-shell"><section className="auth-intro"><div className="brand"><img src="/nexus.svg" width="56" height="56" alt="" /><strong>Nexus</strong></div><h1>Gestão de estoque</h1><p>Produtos, movimentações e controle de reposição.</p><div className="auth-note">Acesse a plataforma com sua conta e e-mail confirmado.</div></section>
    <section className="panel auth-card"><h2>{titulo}</h2><p className="auth-description">{tela === 'login' ? 'Entre para continuar na plataforma.' : tela === 'cadastro' ? 'Após o cadastro, confirme o link recebido por e-mail para liberar seu acesso.' : tela === 'recuperacao' ? 'Informe o e-mail confirmado da sua conta. O link de recuperação vale por 30 minutos.' : tela === 'correcao' ? 'Para uma conta ainda não confirmada, informe o e-mail ou username do cadastro, sua senha e o endereço correto.' : 'Informe o e-mail do cadastro para solicitar um novo link de confirmação.'}</p>
      {(erro || mensagem) && <p role="alert" className="message error">{erro || mensagem}</p>}{sucesso && <p role="status" className="message success">{sucesso}</p>}
      <form onSubmit={event => void enviar(event)}><fieldset disabled={ocupado}>
        {tela === 'cadastro' && <><label>Nome<input value={nome} onChange={e => setNome(e.target.value)} required maxLength={120} autoComplete="name" /></label><label>Username<input value={username} onChange={e => setUsername(e.target.value)} required minLength={3} maxLength={50} pattern={'[a-zA-Z0-9._\\-]{3,50}'} autoComplete="username" /></label></>}
        <label>{tela === 'login' || tela === 'correcao' ? 'E-mail ou username' : 'E-mail'}<input type={tela === 'login' || tela === 'correcao' ? 'text' : 'email'} value={email} onChange={e => setEmail(e.target.value)} required maxLength={254} autoComplete={tela === 'login' || tela === 'correcao' ? 'username' : 'email'} /></label>
        {(tela === 'login' || tela === 'cadastro' || tela === 'correcao') && <><label>Senha<input type={mostrarSenha ? 'text' : 'password'} value={senha} onChange={e => setSenha(e.target.value)} required minLength={tela === 'cadastro' ? 12 : undefined} maxLength={72} autoComplete={tela === 'cadastro' ? 'new-password' : 'current-password'} /></label><button type="button" className="text-button password-toggle" aria-pressed={mostrarSenha} onClick={() => setMostrarSenha(!mostrarSenha)}>{mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}</button></>}
        {tela === 'correcao' && <label>E-mail correto<input type="email" value={novoEmail} onChange={e => setNovoEmail(e.target.value)} required maxLength={254} autoComplete="email" /></label>}
        {sugestao && (tela === 'cadastro' || tela === 'correcao' || tela === 'recuperacao' || tela === 'confirmacao') && <p className="form-hint">Confira o domínio: você quis dizer <strong>@{sugestao}</strong>?</p>}
        {tela === 'cadastro' && <><label>Confirmar senha<input type="password" value={repeticao} onChange={e => setRepeticao(e.target.value)} required minLength={12} maxLength={72} autoComplete="new-password" /></label><p className="form-hint">Use pelo menos 12 caracteres. Você pode usar uma frase como senha.</p></>}
        <button className="auth-submit" type="submit">{ocupado ? 'Aguarde…' : { login: 'Entrar na Nexus', cadastro: 'Criar conta', recuperacao: 'Enviar link de recuperação', confirmacao: 'Reenviar confirmação', correcao: 'Corrigir e enviar confirmação' }[tela]}</button>
      </fieldset></form>
      <div className="auth-links">{tela === 'login' ? <><button disabled={ocupado} onClick={() => navegar('recuperacao')}>Esqueci minha senha</button><p>Ainda não tem conta? <button disabled={ocupado} onClick={() => navegar('cadastro')}>Criar conta</button></p><details className="auth-help"><summary>Preciso de ajuda com a confirmação</summary><button disabled={ocupado} onClick={() => navegar('confirmacao')}>Não recebi a confirmação</button><button disabled={ocupado} onClick={() => navegar('correcao')}>Errei meu e-mail no cadastro</button></details></> : <button disabled={ocupado} onClick={() => navegar('login')}>Voltar para o login</button>}</div>
    </section></main>;
}
