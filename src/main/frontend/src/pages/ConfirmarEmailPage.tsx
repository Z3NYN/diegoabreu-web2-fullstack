import { useRef, useState } from 'react';
import api, { mensagemErro } from '../services/api';
import AuthFrame from '../components/AuthFrame';
import Icon from '../components/Icon';
export default function ConfirmarEmailPage({ token }: { token: string }) {
  const [ocupado, setOcupado] = useState(false);
  const [confirmado, setConfirmado] = useState(false);
  const [erro, setErro] = useState('');
  const titulo = useRef<HTMLHeadingElement>(null);
  const mensagemFalha = useRef<HTMLParagraphElement>(null);
  async function confirmar() {
    if (ocupado) return;
    setOcupado(true); setErro('');
    try { await api.post('/usuarios/confirmar-email', { token }); setConfirmado(true); window.history.replaceState({}, '', '/'); requestAnimationFrame(() => titulo.current?.focus()); }
    catch (error) { setErro(mensagemErro(error)); requestAnimationFrame(() => mensagemFalha.current?.focus()); }
    finally { setOcupado(false); }
  }
  return <AuthFrame><span className="result-symbol"><Icon name={confirmado ? 'check' : 'mail'} /></span><p className="eyebrow">CONFIRMAÇÃO DE E-MAIL</p><h1 ref={titulo} tabIndex={-1}>{confirmado ? 'E-mail confirmado' : 'Confirme seu e-mail'}</h1>
    <p className="auth-description" role={confirmado ? 'status' : undefined}>{confirmado ? 'Seu endereço foi verificado. Entre com seu e-mail e senha para acessar a plataforma.' : 'Confirme o endereço usado no cadastro para liberar seu acesso. Este link é válido por 24 horas.'}</p>
    {erro && <p ref={mensagemFalha} tabIndex={-1} role="alert" className="message error">{erro}</p>}
    {!confirmado && <button className="auth-submit" disabled={ocupado} onClick={() => void confirmar()}>{ocupado ? 'Confirmando…' : 'Confirmar meu e-mail'}<Icon name="check" /></button>}
    <a className={confirmado ? 'button-link auth-submit' : 'back-link'} href="/">{confirmado ? 'Entrar na Nexus' : 'Voltar para o login'}{confirmado && <Icon name="arrow" />}</a>
    {erro && <p className="form-hint">Se o link expirou ou já foi usado, solicite outro em “Ajuda com a confirmação de e-mail” na tela de login.</p>}
  </AuthFrame>;
}
