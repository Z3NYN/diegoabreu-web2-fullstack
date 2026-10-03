import { useState } from 'react';
import api, { mensagemErro } from '../services/api';
export default function ConfirmarEmailPage({ token }: { token: string }) {
  const [ocupado, setOcupado] = useState(false);
  const [confirmado, setConfirmado] = useState(false);
  const [erro, setErro] = useState('');
  async function confirmar() {
    if (ocupado) return;
    setOcupado(true); setErro('');
    try { await api.post('/usuarios/confirmar-email', { token }); setConfirmado(true); window.history.replaceState({}, '', '/'); }
    catch (error) { setErro(mensagemErro(error)); }
    finally { setOcupado(false); }
  }
  return <main className="confirmation"><div className="panel"><img src="/nexus.svg" width="56" height="56" alt="Nexus" /><p className="eyebrow">NEXUS · CONFIRMAÇÃO DE E-MAIL</p><h1>{confirmado ? 'E-mail confirmado' : 'Confirme seu e-mail'}</h1>
    <p>{confirmado ? 'Seu endereço foi verificado com sucesso. Você já pode voltar à aplicação.' : 'Clique abaixo para confirmar que você tem acesso ao endereço usado no cadastro. O link é válido por 24 horas.'}</p>
    {erro && <p role="alert" className="message error">{erro}</p>}
    {!confirmado && <button disabled={ocupado} onClick={() => void confirmar()}>{ocupado ? 'Confirmando…' : 'Confirmar meu e-mail'}</button>}
    <a className="back-link" href="/">Voltar à Nexus</a>
  </div></main>;
}
