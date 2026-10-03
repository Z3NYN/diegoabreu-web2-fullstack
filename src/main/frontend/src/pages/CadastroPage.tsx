import { useCallback, useEffect, useRef, useState } from 'react';
import api, { mensagemErro } from '../services/api';
import CadastroForm from '../components/CadastroForm';
import CadastroList from '../components/CadastroList';
import type { Campo, Registro } from '../types/Cadastro';
interface Props { titulo: string; recurso: string; campos: Campo[] }
export default function CadastroPage({ titulo, recurso, campos }: Props) {
  const [registros, setRegistros] = useState<Registro[]>([]);
  const [editando, setEditando] = useState<Registro | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [ocupado, setOcupado] = useState(false);
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');
  const bloqueado = useRef(false);
  const carregar = useCallback(async () => {
    try { const resposta = await api.get<Registro[]>(`/${recurso}`); setRegistros(resposta.data); }
    finally { setCarregando(false); }
  }, [recurso]);
  useEffect(() => { void carregar().catch(error => setErro(mensagemErro(error))); }, [carregar]);
  async function atualizarDepoisDaOperacao() {
    try { await carregar(); }
    catch { setErro('A operação foi concluída, mas a lista não pôde ser atualizada. Clique em Atualizar para consultar os dados.'); }
  }
  async function salvar(dados: Omit<Registro, 'id'>): Promise<boolean> {
    if (bloqueado.current) return false;
    bloqueado.current = true; setOcupado(true); setErro(''); setSucesso('');
    try {
      if (editando) await api.put(`/${recurso}/${editando.id}`, dados);
      else await api.post(`/${recurso}`, dados);
      setEditando(null);
      await atualizarDepoisDaOperacao(); setSucesso(recurso === 'usuarios' ? 'Cadastro salvo. Consulte o status de confirmação do e-mail na lista.' : 'Cadastro salvo com sucesso.'); return true;
    } catch (error) { setErro(mensagemErro(error)); return false; }
    finally { bloqueado.current = false; setOcupado(false); }
  }
  async function enviarConfirmacao(id: number) {
    if (bloqueado.current) return;
    bloqueado.current = true; setOcupado(true); setErro(''); setSucesso('');
    try {
      await api.post(`/${recurso}/${id}/confirmacao-email`);
      await atualizarDepoisDaOperacao();
      setSucesso('Solicitação de envio aceita. Confira a caixa de entrada e o spam; o e-mail será confirmado somente após abrir o link.');
    } catch (error) { setErro(mensagemErro(error)); }
    finally { bloqueado.current = false; setOcupado(false); }
  }
  async function excluir(id: number) {
    if (bloqueado.current || !window.confirm('Deseja excluir este registro?')) return;
    bloqueado.current = true; setOcupado(true); setErro(''); setSucesso('');
    try { await api.delete(`/${recurso}/${id}`); if (editando?.id === id) setEditando(null); await atualizarDepoisDaOperacao(); setSucesso('Registro excluído com sucesso.'); }
    catch (error) { setErro(mensagemErro(error)); }
    finally { bloqueado.current = false; setOcupado(false); }
  }
  return <section><div className="page-heading"><div><p className="eyebrow">GESTÃO DE CADASTROS</p><h1>{titulo}</h1><p>Cadastre, consulte e mantenha seus registros atualizados.</p></div><span className="count">{registros.length} registros</span></div>
    {erro && <p role="alert" className="message error">{erro}</p>}{sucesso && <p role="status" className="message success">{sucesso}</p>}
    <div className="grid"><CadastroForm key={editando?.id ?? "novo"} campos={campos} editando={editando} ocupado={ocupado} onSalvar={salvar} onCancelar={() => setEditando(null)} />
      <div className="panel"><div className="list-heading"><h2>Registros cadastrados</h2><button className="secondary" disabled={ocupado || carregando} onClick={() => { setErro(''); setCarregando(true); void carregar().catch(error => setErro(mensagemErro(error))); }}>Atualizar</button></div>
        {carregando ? <p role="status">Carregando registros…</p> : <CadastroList registros={registros} campos={campos} ocupado={ocupado} onEditar={registro => { setEditando(registro); setErro(''); setSucesso(''); }} onExcluir={id => void excluir(id)} onConfirmacao={recurso === 'usuarios' ? id => void enviarConfirmacao(id) : undefined} />}
      </div></div>
  </section>;
}
