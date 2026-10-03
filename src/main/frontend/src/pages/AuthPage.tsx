import { useRef, useState } from 'react';
import type { FormEvent } from 'react';
import api, { mensagemErro } from '../services/api';
type Tela = 'login' | 'cadastro' | 'recuperacao' | 'confirmacao';
export default function AuthPage({ onEntrar, mensagem }: { onEntrar: (conta: { id: number; nome: string; email: string }) => void; mensagem: string }) {
  const [tela, setTela] = useState<Tela>('login');
  const [nome, setNome] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [repeticao, setRepeticao] = useState('');
  const [ocupado, setOcupado] = useState(false);
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');
  const bloqueado = useRef(false);
  const titulo = { login: 'Bem-vindo de volta', cadastro: 'Crie sua conta', recuperacao: 'Recupere seu acesso', confirmacao: 'Confirme seu e-mail' }[tela];
  function navegar(destino: Tela) { setTela(destino); setSenha(''); setRepeticao(''); setErro(''); setSucesso(''); }
  async function enviar(event: FormEvent) {
    event.preventDefault(); if (bloqueado.current) return;
    setErro(''); setSucesso('');
    if (tela === 'cadastro' && (senha !== repeticao || new TextEncoder().encode(senha).length > 72)) { setErro('As senhas devem ser iguais e ter até 72 bytes.'); return; }
    bloqueado.current = true; setOcupado(true);
    try {
      if (tela === 'login') {
        const { data } = await api.post('/auth/login', { identificador: email, senha });
        setSenha(''); onEntrar(data);
      } else {
        const endpoint = { cadastro: '/auth/registrar', recuperacao: '/auth/recuperacao', confirmacao: '/auth/reenviar-confirmacao' }[tela];
        const { data } = await api.post(endpoint, tela === 'cadastro' ? { nome, username, email, senha } : { email });
        setSucesso(data.message); setSenha(''); setRepeticao('');
      }
    } catch (error) { setErro(mensagemErro(error)); }
    finally { bloqueado.current = false; setOcupado(false); }
  }
  return <main className="auth-shell"><section className="auth-intro"><div className="brand"><img src="/nexus.svg" width="56" height="56" alt="" /><strong>Nexus</strong></div><p className="eyebrow">TUDO CONECTADO</p><h1>Sua gestão.<br />Em um só lugar.</h1><p>Organize usuários, permissões e produtos com clareza e praticidade.</p><div className="auth-note">Acesse a plataforma com sua conta e e-mail confirmado.</div></section>
    <section className="panel auth-card"><p className="eyebrow">SUA CONTA NEXUS</p><h2>{titulo}</h2><p className="auth-description">{tela === 'login' ? 'Entre para continuar na plataforma.' : tela === 'cadastro' ? 'Após o cadastro, confirme o link recebido por e-mail para liberar seu acesso.' : tela === 'recuperacao' ? 'Informe o e-mail confirmado da sua conta. O link de recuperação vale por 30 minutos.' : 'Informe o e-mail do cadastro para solicitar um novo link de confirmação.'}</p>
      {(erro || mensagem) && <p role="alert" className="message error">{erro || mensagem}</p>}{sucesso && <p role="status" className="message success">{sucesso}</p>}
      <form onSubmit={event => void enviar(event)}><fieldset disabled={ocupado}>
        {tela === 'cadastro' && <><label>Nome<input value={nome} onChange={e => setNome(e.target.value)} required maxLength={120} autoComplete="name" /></label><label>Username<input value={username} onChange={e => setUsername(e.target.value)} required minLength={3} maxLength={50} pattern={'[a-zA-Z0-9._\\-]{3,50}'} autoComplete="username" /></label></>}
        <label>{tela === 'login' ? 'E-mail ou username' : 'E-mail'}<input type={tela === 'login' ? 'text' : 'email'} value={email} onChange={e => setEmail(e.target.value)} required maxLength={254} autoComplete={tela === 'login' ? 'username' : 'email'} /></label>
        {(tela === 'login' || tela === 'cadastro') && <label>Senha<input type="password" value={senha} onChange={e => setSenha(e.target.value)} required minLength={tela === 'cadastro' ? 12 : undefined} maxLength={72} autoComplete={tela === 'login' ? 'current-password' : 'new-password'} /></label>}
        {tela === 'cadastro' && <><label>Confirmar senha<input type="password" value={repeticao} onChange={e => setRepeticao(e.target.value)} required minLength={12} maxLength={72} autoComplete="new-password" /></label><p className="form-hint">Use pelo menos 12 caracteres. Você pode usar uma frase como senha.</p></>}
        <button className="auth-submit" type="submit">{ocupado ? 'Aguarde…' : { login: 'Entrar na Nexus', cadastro: 'Criar conta', recuperacao: 'Enviar link de recuperação', confirmacao: 'Reenviar confirmação' }[tela]}</button>
      </fieldset></form>
      <div className="auth-links">{tela === 'login' ? <><button disabled={ocupado} onClick={() => navegar('recuperacao')}>Esqueci minha senha</button><button disabled={ocupado} onClick={() => navegar('confirmacao')}>Não recebi a confirmação</button><p>Ainda não tem conta? <button disabled={ocupado} onClick={() => navegar('cadastro')}>Criar conta</button></p></> : <button disabled={ocupado} onClick={() => navegar('login')}>Voltar para o login</button>}</div>
    </section></main>;
}
