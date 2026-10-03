import { useRef, useState } from 'react';
import type { FormEvent } from 'react';
import api, { mensagemErro } from '../services/api';
export default function RedefinirSenhaPage({ token }: { token: string }) {
  const [senha, setSenha] = useState('');
  const [repeticao, setRepeticao] = useState('');
  const [erro, setErro] = useState('');
  const [ocupado, setOcupado] = useState(false);
  const [concluido, setConcluido] = useState(false);
  const bloqueado = useRef(false);
  async function salvar(event: FormEvent) {
    event.preventDefault(); if (bloqueado.current) return;
    setErro('');
    if (senha !== repeticao || new TextEncoder().encode(senha).length > 72) { setErro('As senhas devem ser iguais e ter até 72 bytes.'); return; }
    bloqueado.current = true; setOcupado(true);
    try {
      await api.post('/auth/redefinir-senha', { token, senha });
      setConcluido(true); setSenha(''); setRepeticao(''); window.history.replaceState({}, '', '/');
    } catch (error) { setErro(mensagemErro(error)); }
    finally { bloqueado.current = false; setOcupado(false); }
  }
  return <main className="confirmation"><section className="panel"><img src="/nexus.svg" width="56" height="56" alt="Nexus" /><p className="eyebrow">RECUPERAÇÃO DE ACESSO</p><h1>{concluido ? 'Senha atualizada' : 'Defina uma nova senha'}</h1><p>{concluido ? 'Suas sessões anteriores foram encerradas. Entre novamente com a nova senha.' : 'O link vale por 30 minutos e pode ser usado uma única vez.'}</p>{erro && <p className="message error" role="alert">{erro}</p>}
    {!concluido && <form onSubmit={event => void salvar(event)}><fieldset disabled={ocupado}><label>Nova senha<input type="password" required minLength={12} maxLength={72} autoComplete="new-password" value={senha} onChange={e => setSenha(e.target.value)} /></label><label>Confirmar nova senha<input type="password" required minLength={12} maxLength={72} autoComplete="new-password" value={repeticao} onChange={e => setRepeticao(e.target.value)} /></label><p className="form-hint">Use pelo menos 12 caracteres.</p><button type="submit">{ocupado ? 'Salvando…' : 'Salvar nova senha'}</button></fieldset></form>}<a className="back-link" href="/">Voltar para o login</a></section></main>;
}
