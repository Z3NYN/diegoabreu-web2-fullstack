import { useCallback, useEffect, useRef, useState } from 'react';
import api, { mensagemErro } from '../services/api';
import CadastroForm from '../components/CadastroForm';
import CadastroList from '../components/CadastroList';
import ConfirmarExclusao from '../components/ConfirmarExclusao';
import type { Campo, Registro } from '../types/Cadastro';
interface Props { titulo: string; recurso: string; campos: Campo[] }
export default function CadastroPage({ titulo, recurso, campos }: Props) {
  const [registros, setRegistros] = useState<Registro[]>([]);
  const [editando, setEditando] = useState<Registro | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [ocupado, setOcupado] = useState(false);
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');
  const [busca, setBusca] = useState('');
  const [ordem, setOrdem] = useState('recentes');
  const [formAberto, setFormAberto] = useState(false);
  const [excluindo, setExcluindo] = useState<Registro | null>(null);
  const visiveis = registros.filter(registro => campos.some(campo => String(registro[campo.nome] ?? '').toLocaleLowerCase('pt-BR').includes(busca.trim().toLocaleLowerCase('pt-BR')))).sort((a, b) => ordem === 'nome' ? String(a.nome).localeCompare(String(b.nome), 'pt-BR') : b.id - a.id);
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
      setFormAberto(false);
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
    if (bloqueado.current) return;
    bloqueado.current = true; setOcupado(true); setErro(''); setSucesso('');
    try { await api.delete(`/${recurso}/${id}`); if (editando?.id === id) { setEditando(null); setFormAberto(false); } setExcluindo(null); await atualizarDepoisDaOperacao(); setSucesso('Registro excluído com sucesso.'); }
    catch (error) { setExcluindo(null); setErro(mensagemErro(error)); }
    finally { bloqueado.current = false; setOcupado(false); }
  }
  function abrirFormulario(registro: Registro | null = null) {
    setEditando(registro); setFormAberto(true); setErro(''); setSucesso('');
    requestAnimationFrame(() => { const campo = document.querySelector<HTMLInputElement>('.form input'); campo?.focus(); campo?.scrollIntoView({ behavior: 'smooth', block: 'center' }); });
  }
  return <section><div className="page-heading"><div><h1>{titulo} <span className="heading-count">{carregando ? '…' : registros.length}</span></h1><p>{{ produtos: 'Organize seu catálogo e mantenha os preços atualizados.', usuarios: 'Gerencie os cadastros e acompanhe a confirmação dos e-mails.', permissoes: 'Organize as permissões cadastradas na plataforma.' }[recurso]}</p></div><button disabled={ocupado} onClick={() => abrirFormulario()}><span aria-hidden="true">＋ </span>Novo cadastro</button></div>
    {erro && <p role="alert" className="message error">{erro}</p>}{sucesso && <p role="status" className="message success">{sucesso}</p>}
    <div className={formAberto ? 'grid' : 'list-only'}>{formAberto && <CadastroForm key={editando?.id ?? "novo"} campos={campos} editando={editando} ocupado={ocupado} onSalvar={salvar} onCancelar={() => { setEditando(null); setFormAberto(false); }} />}
      <div className="panel"><div className="list-heading"><h2>Registros cadastrados</h2><button className="secondary" disabled={ocupado || carregando} onClick={() => { setErro(''); setCarregando(true); void carregar().catch(error => setErro(mensagemErro(error))); }}>Atualizar</button></div>
        <div className="list-tools"><label className="search-label">Buscar em {titulo.toLocaleLowerCase('pt-BR')}<input type="search" placeholder="Buscar por nome ou outros dados…" value={busca} onChange={event => setBusca(event.target.value)} /></label><label className="sort-label">Ordenar por<select value={ordem} onChange={event => setOrdem(event.target.value)}><option value="recentes">Mais recentes</option><option value="nome">Nome (A–Z)</option></select></label></div>
        {busca.trim() && <p className="form-hint" role="status">{visiveis.length} de {registros.length} registros encontrados <button className="text-button" onClick={() => setBusca('')}>Limpar busca</button></p>}
        {carregando ? <div className="loading-state" role="status"><span className="spinner" aria-hidden="true" />Carregando registros…</div> : busca.trim() && !visiveis.length ? <p className="empty">Nenhum resultado. Tente outro termo ou limpe a busca.</p> : !registros.length ? <div className="empty"><span className="empty-symbol" aria-hidden="true">＋</span><h3>Nenhum registro cadastrado</h3><p>Adicione um registro para começar a organizar {titulo.toLocaleLowerCase('pt-BR')}.</p><button disabled={ocupado} onClick={() => abrirFormulario()}>Adicionar primeiro registro</button></div> : <CadastroList registros={visiveis} campos={campos} ocupado={ocupado} onEditar={registro => abrirFormulario(registro)} onExcluir={id => setExcluindo(registros.find(registro => registro.id === id) ?? null)} onConfirmacao={recurso === 'usuarios' ? id => void enviarConfirmacao(id) : undefined} />}
      </div></div>
    {excluindo && <ConfirmarExclusao registro={excluindo} ocupado={ocupado} onCancelar={() => setExcluindo(null)} onConfirmar={() => void excluir(excluindo.id)} />}
  </section>;
}
