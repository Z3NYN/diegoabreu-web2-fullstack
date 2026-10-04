import { useRef, useState } from 'react';
import type { FormEvent } from 'react';
import api, { mensagemErro } from '../services/api';
import AuthFrame from '../components/AuthFrame';
import PasswordField from '../components/PasswordField';
import Icon from '../components/Icon';
type Tela = 'login' | 'cadastro' | 'recuperacao' | 'confirmacao' | 'correcao';
const titulos: Record<Tela, string> = { login: 'Entre na sua conta', cadastro: 'Crie sua conta', recuperacao: 'Recupere seu acesso', confirmacao: 'Confirme seu e-mail', correcao: 'Corrija seu e-mail' };
const descricoes: Record<Tela, string> = {
  login: 'Use seu e-mail ou nome de usuário para acessar a Nexus.',
  cadastro: 'Preencha seus dados. Você receberá um link para confirmar o e-mail antes de entrar.',
  recuperacao: 'Informe o e-mail confirmado da conta. O link de recuperação é válido por 30 minutos.',
  confirmacao: 'Solicite um novo link para o e-mail do seu cadastro.',
  correcao: 'Se a conta ainda não foi confirmada, use sua senha para corrigir o endereço e receber um novo link.',
};
export default function AuthPage({ onEntrar, mensagem }: { onEntrar: (conta: { id: number; nome: string; email: string }) => void; mensagem: string }) {
  const [tela, setTela] = useState<Tela>('login');
  const [nome, setNome] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [novoEmail, setNovoEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [repeticao, setRepeticao] = useState('');
  const [ocupado, setOcupado] = useState(false);
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');
  const bloqueado = useRef(false);
  const titulo = useRef<HTMLHeadingElement>(null);
  const retorno = useRef<HTMLDivElement>(null);
  const mensagemFalha = useRef<HTMLParagraphElement>(null);
  const dominio = (tela === 'correcao' ? novoEmail : email).split('@')[1]?.toLowerCase();
  const sugestoes: Record<string, string> = { 'gmai.com': 'gmail.com', 'gmal.com': 'gmail.com', 'gmial.com': 'gmail.com', 'gmail.con': 'gmail.com', 'hotmai.com': 'hotmail.com', 'outlok.com': 'outlook.com' };
  const sugestao = dominio && sugestoes[dominio];
  function navegar(destino: Tela) {
    setTela(destino); setSenha(''); setRepeticao(''); setErro(''); setSucesso('');
    requestAnimationFrame(() => titulo.current?.focus());
  }
  async function enviar(event: FormEvent) {
    event.preventDefault(); if (bloqueado.current) return;
    setErro(''); setSucesso('');
    if (tela === 'cadastro' && senha !== repeticao) { setErro('A confirmação precisa ser igual à senha.'); return; }
    if (tela === 'cadastro' && new TextEncoder().encode(senha).length > 72) { setErro('Essa senha é muito longa. Use uma frase mais curta (limite de 72 bytes).'); return; }
    bloqueado.current = true; setOcupado(true);
    try {
      if (tela === 'login') {
        const { data } = await api.post('/auth/login', { identificador: email, senha });
        setSenha(''); onEntrar(data);
      } else if (tela === 'correcao') {
        const { data } = await api.post('/auth/corrigir-email', { identificador: email, senha, email: novoEmail });
        setSucesso(data.message); setSenha(''); setEmail(novoEmail);
        requestAnimationFrame(() => retorno.current?.focus());
      } else {
        const endpoint = { cadastro: '/auth/registrar', recuperacao: '/auth/recuperacao', confirmacao: '/auth/reenviar-confirmacao' }[tela];
        const { data } = await api.post(endpoint, tela === 'cadastro' ? { nome, username, email, senha } : { email });
        setSucesso(data.message); setSenha(''); setRepeticao('');
        requestAnimationFrame(() => retorno.current?.focus());
      }
    } catch (error) { setErro(mensagemErro(error)); requestAnimationFrame(() => mensagemFalha.current?.focus()); }
    finally { bloqueado.current = false; setOcupado(false); }
  }
  return <AuthFrame>
    {tela !== 'login' && <button type="button" className="auth-back quiet" disabled={ocupado} onClick={() => navegar('login')}>← Voltar para o login</button>}
    {sucesso ? <div className="auth-result" ref={retorno} tabIndex={-1}><span className="result-symbol"><Icon name="mail" /></span><p className="eyebrow">PRÓXIMO PASSO</p><h1>Confira seu e-mail</h1><p role="status">{sucesso}</p><p className="form-hint">Confira também o spam. Abra o link recebido para {tela === 'recuperacao' ? 'definir sua nova senha' : 'confirmar o endereço'}.</p><button className="auth-submit" onClick={() => navegar('login')}>Voltar para o login<Icon name="arrow" /></button>{tela !== 'recuperacao' && <button className="text-button" onClick={() => navegar('confirmacao')}>Solicitar outro link</button>}</div> : <>
      <p className="eyebrow">{tela === 'login' ? 'ACESSO À PLATAFORMA' : 'CONTA NEXUS'}</p><h1 ref={titulo} tabIndex={-1}>{titulos[tela]}</h1><p className="auth-description">{descricoes[tela]}</p>
      {(erro || (tela === 'login' && mensagem)) && <p ref={mensagemFalha} tabIndex={-1} role="alert" className="message error">{erro || mensagem}</p>}
      <form onSubmit={event => void enviar(event)} aria-busy={ocupado}><fieldset disabled={ocupado}>
        {tela === 'cadastro' && <><label>Nome completo<input value={nome} onChange={e => setNome(e.target.value)} required maxLength={120} autoComplete="name" /></label><label className="hinted-label">Nome de usuário<input value={username} onChange={e => setUsername(e.target.value)} required minLength={3} maxLength={50} pattern={'[a-zA-Z0-9._\\-]{3,50}'} autoComplete="username" aria-describedby="username-hint" /></label><p id="username-hint" className="field-hint">De 3 a 50 caracteres: letras, números, ponto, hífen ou sublinhado.</p></>}
        <label>{tela === 'login' || tela === 'correcao' ? 'E-mail ou nome de usuário' : 'E-mail'}<input type={tela === 'login' || tela === 'correcao' ? 'text' : 'email'} value={email} onChange={e => setEmail(e.target.value)} required maxLength={254} autoComplete={tela === 'login' || tela === 'correcao' ? 'username' : 'email'} autoCapitalize="none" spellCheck={false} placeholder={tela === 'login' || tela === 'correcao' ? 'Seu e-mail ou usuário' : 'voce@exemplo.com'} /></label>
        {(tela === 'login' || tela === 'cadastro' || tela === 'correcao') && <PasswordField key={tela} label="Senha" value={senha} onChange={setSenha} novo={tela === 'cadastro'} hint={tela === 'cadastro' ? 'Pelo menos 12 caracteres. Uma frase também funciona.' : undefined} />}
        {tela === 'login' && <div className="forgot-row"><button type="button" className="text-button" onClick={() => navegar('recuperacao')}>Esqueci minha senha</button></div>}
        {tela === 'correcao' && <label>E-mail correto<input type="email" value={novoEmail} onChange={e => setNovoEmail(e.target.value)} required maxLength={254} autoComplete="email" autoCapitalize="none" spellCheck={false} /></label>}
        {sugestao && tela !== 'login' && <p className="message warning">Confira o domínio: você quis dizer <strong>@{sugestao}</strong>?</p>}
        {tela === 'cadastro' && <PasswordField label="Confirmar senha" value={repeticao} onChange={setRepeticao} novo />}
        <button className="auth-submit" type="submit">{ocupado ? <><span className="spinner" aria-hidden="true" />Aguarde…</> : <>{{ login: 'Entrar na Nexus', cadastro: 'Criar conta', recuperacao: 'Enviar link de recuperação', confirmacao: 'Reenviar confirmação', correcao: 'Corrigir e enviar confirmação' }[tela]}<Icon name="arrow" /></>}</button>
      </fieldset></form>
      {tela === 'login' && <div className="auth-links"><p>Primeiro acesso? <button disabled={ocupado} className="text-button" onClick={() => navegar('cadastro')}>Criar conta</button></p><details className="auth-help"><summary>Ajuda com a confirmação de e-mail</summary><button disabled={ocupado} className="text-button" onClick={() => navegar('confirmacao')}>Não recebi o link</button><button disabled={ocupado} className="text-button" onClick={() => navegar('correcao')}>Cadastrei o endereço errado</button></details></div>}
    </>}
  </AuthFrame>;
}
