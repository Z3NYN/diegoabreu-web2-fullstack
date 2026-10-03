import { useEffect, useState } from 'react';
import axios from 'axios';
import api, { mensagemErro } from '../services/api';
import Layout from '../components/Layout';
import AuthPage from './AuthPage';
type Conta = { id: number; nome: string; email: string };
export default function AuthGate() {
  const [conta, setConta] = useState<Conta | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [saindo, setSaindo] = useState(false);
  const [erro, setErro] = useState('');
  useEffect(() => {
    let ativo = true;
    const expirar = () => { setConta(null); setErro('Sua sessão terminou. Entre novamente.'); };
    window.addEventListener('nexus:session-expired', expirar);
    api.get<Conta>('/auth/me').then(resposta => { if (ativo) setConta(resposta.data); }).catch(error => {
      if (ativo && !(axios.isAxiosError(error) && error.response?.status === 401)) setErro(mensagemErro(error));
    }).finally(() => { if (ativo) setCarregando(false); });
    return () => { ativo = false; window.removeEventListener('nexus:session-expired', expirar); };
  }, []);
  async function sair() {
    if (saindo) return;
    setSaindo(true); setErro('');
    try { await api.post('/auth/logout'); setConta(null); }
    catch (error) { setErro(mensagemErro(error)); }
    finally { setSaindo(false); }
  }
  if (carregando) return <main className="confirmation"><p role="status">Verificando sua sessão…</p></main>;
  if (!conta) return <AuthPage mensagem={erro} onEntrar={usuario => { setConta(usuario); setErro(''); }} />;
  return <>{erro && <p className="message error" role="alert">{erro}</p>}<Layout nome={conta.nome} onSair={() => void sair()} saindo={saindo} /></>;
}
