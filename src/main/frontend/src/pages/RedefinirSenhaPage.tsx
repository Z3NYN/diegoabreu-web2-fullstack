import { useRef, useState } from 'react';
import type { FormEvent } from 'react';
import api, { mensagemErro } from '../services/api';
import AuthFrame from '../components/AuthFrame';
import PasswordField from '../components/PasswordField';
import Icon from '../components/Icon';
export default function RedefinirSenhaPage({ token }: { token: string }) {
  const [senha, setSenha] = useState('');
  const [repeticao, setRepeticao] = useState('');
  const [erro, setErro] = useState('');
  const [ocupado, setOcupado] = useState(false);
  const [concluido, setConcluido] = useState(false);
  const bloqueado = useRef(false);
  const titulo = useRef<HTMLHeadingElement>(null);
  const mensagemFalha = useRef<HTMLParagraphElement>(null);
  async function salvar(event: FormEvent) {
    event.preventDefault(); if (bloqueado.current) return;
    setErro('');
    if (senha !== repeticao) { setErro('A confirmação precisa ser igual à nova senha.'); return; }
    if (new TextEncoder().encode(senha).length > 72) { setErro('Essa senha é muito longa. Use uma frase mais curta (limite de 72 bytes).'); return; }
    bloqueado.current = true; setOcupado(true);
    try {
      await api.post('/auth/redefinir-senha', { token, senha });
      setConcluido(true); setSenha(''); setRepeticao(''); window.history.replaceState({}, '', '/');
      requestAnimationFrame(() => titulo.current?.focus());
    } catch (error) { setErro(mensagemErro(error)); requestAnimationFrame(() => mensagemFalha.current?.focus()); }
    finally { bloqueado.current = false; setOcupado(false); }
  }
  return <AuthFrame>{concluido && <span className="result-symbol"><Icon name="check" /></span>}<p className="eyebrow">RECUPERAÇÃO DE ACESSO</p><h1 ref={titulo} tabIndex={-1}>{concluido ? 'Senha atualizada' : 'Defina uma nova senha'}</h1><p className="auth-description" role={concluido ? 'status' : undefined}>{concluido ? 'Suas sessões anteriores foram encerradas. Entre novamente com a nova senha.' : 'Escolha uma senha diferente. O link é válido por 30 minutos e pode ser usado uma única vez.'}</p>{erro && <p ref={mensagemFalha} tabIndex={-1} className="message error" role="alert">{erro}</p>}
    {!concluido && <form onSubmit={event => void salvar(event)} aria-busy={ocupado}><fieldset disabled={ocupado}><PasswordField label="Nova senha" value={senha} onChange={setSenha} novo hint="Pelo menos 12 caracteres. Você pode usar uma frase." /><PasswordField label="Confirmar nova senha" value={repeticao} onChange={setRepeticao} novo /><button className="auth-submit" type="submit">{ocupado ? 'Salvando…' : 'Salvar nova senha'}<Icon name="arrow" /></button></fieldset></form>}<a className={concluido ? 'button-link auth-submit' : 'back-link'} href="/">{concluido ? 'Entrar com a nova senha' : 'Voltar para o login'}</a></AuthFrame>;
}
