import { useCallback, useEffect, useRef, useState } from 'react';
import api, { mensagemErro } from '../services/api';
import { estoqueService } from '../services/estoque';
import MovimentacaoForm from '../components/MovimentacaoForm';
import MovimentacaoList from '../components/MovimentacaoList';
import type { HistoricoEstoque, PedidoEstoque, ProdutoEstoque, ResumoEstoque } from '../types/Estoque';
export default function EstoquePage() {
  const [produtos, setProdutos] = useState<ProdutoEstoque[]>([]);
  const [resumo, setResumo] = useState<ResumoEstoque | null>(null);
  const [selecionado, setSelecionado] = useState<number | null>(null);
  const [consulta, setConsulta] = useState<{ produtoId: number; revisao: number; dados: HistoricoEstoque | null; erro: string } | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [ocupado, setOcupado] = useState(false);
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');
  const [busca, setBusca] = useState('');
  const [apenasBaixo, setApenasBaixo] = useState(false);
  const [revisao, setRevisao] = useState(0);
  const bloqueado = useRef(false);
  const carregar = useCallback(async () => {
    const [lista, totals] = await Promise.all([api.get<ProdutoEstoque[]>('/produtos'), estoqueService.resumo()]);
    setProdutos(lista.data); setResumo(totals.data);
    setSelecionado(atual => lista.data.some(item => item.id === atual) ? atual : lista.data[0]?.id ?? null);
  }, []);
  useEffect(() => { void Promise.resolve().then(carregar).catch(error => setErro(mensagemErro(error))).finally(() => setCarregando(false)); }, [carregar]);
  useEffect(() => {
    let ativo = true;
    if (selecionado !== null) void estoqueService.historico(selecionado).then(resposta => { if (ativo) setConsulta({ produtoId: selecionado, revisao, dados: resposta.data, erro: '' }); }).catch(error => { if (ativo) setConsulta({ produtoId: selecionado, revisao, dados: null, erro: mensagemErro(error) }); });
    return () => { ativo = false; };
  }, [selecionado, revisao]);
  async function atualizar() {
    setCarregando(true); setErro('');
    try { await carregar(); setRevisao(valor => valor + 1); }
    catch (error) { setErro(mensagemErro(error)); }
    finally { setCarregando(false); }
  }
  async function movimentar(pedido: PedidoEstoque) {
    if (bloqueado.current || selecionado === null) return false;
    bloqueado.current = true; setOcupado(true); setErro(''); setSucesso('');
    try {
      await estoqueService.movimentar(selecionado, pedido);
      setSucesso('Movimentação registrada. O saldo e o histórico foram atualizados.');
      try { await carregar(); } catch { setSucesso('Movimentação registrada. Clique em Atualizar para consultar o saldo atualizado.'); }
      setRevisao(valor => valor + 1); return true;
    } catch (error) { setErro(mensagemErro(error)); return false; }
    finally { bloqueado.current = false; setOcupado(false); }
  }
  const produto = produtos.find(item => item.id === selecionado);
  const consultaAtual = consulta?.produtoId === selecionado && consulta?.revisao === revisao ? consulta : null;
  const historico = consultaAtual?.dados;
  const erroHistorico = consultaAtual?.erro;
  const visiveis = produtos.filter(item => item.nome.toLocaleLowerCase('pt-BR').includes(busca.trim().toLocaleLowerCase('pt-BR')) && (!apenasBaixo || item.quantidade <= item.estoqueMinimo));
  return <section><div className="page-heading"><div><h1>Estoque</h1><p>Saldo dos produtos e registro de entradas e saídas.</p></div><button className="secondary" disabled={ocupado || carregando} onClick={() => void atualizar()}>Atualizar</button></div>
    {erro && <p role="alert" className="message error">{erro}</p>}{sucesso && <p role="status" className="message success">{sucesso}</p>}
    {resumo && <div className="stock-metrics"><div><span>Produtos cadastrados</span><strong>{resumo.produtos}</strong></div><div><span>Unidades em estoque</span><strong>{resumo.unidades.toLocaleString('pt-BR')}</strong></div><div className={resumo.estoqueBaixo ? 'metric-warning' : ''}><span>Produtos para repor</span><strong>{resumo.estoqueBaixo}</strong></div><div><span>Valor ao preço cadastrado</span><strong>{resumo.valorEstoque.toLocaleString('pt-BR', {style: 'currency', currency: 'BRL'})}</strong></div></div>}
    {carregando ? <p role="status" className="loading-state">Carregando estoque…</p> : !produtos.length ? <div className="panel empty"><h2>Comece pelo seu catálogo</h2><p>Abra Produtos no menu, cadastre um item e volte aqui para registrar sua primeira entrada.</p></div> : <>
      <div className="panel stock-catalog"><div className="list-tools"><label className="search-label">Buscar produto<input type="search" placeholder="Buscar pelo nome…" value={busca} onChange={event => setBusca(event.target.value)} /></label><label className="stock-filter"><input type="checkbox" checked={apenasBaixo} onChange={event => setApenasBaixo(event.target.checked)} />Só estoque baixo</label></div><p className="form-hint">Selecione um produto para movimentar ou consultar o histórico. Estoque baixo: saldo igual ou abaixo do mínimo.</p><div className="stock-table-wrap"><table className="stock-table"><thead><tr><th scope="col">Produto</th><th scope="col">Saldo</th><th scope="col">Mínimo</th><th scope="col">Situação</th></tr></thead><tbody>{visiveis.map(item => <tr className={item.id === selecionado ? 'selected' : ''} key={item.id}><td><button className="stock-select" disabled={ocupado} aria-pressed={item.id === selecionado} onClick={() => { setSelecionado(item.id); setSucesso(''); setErro(''); }}>{item.nome}</button></td><td>{item.quantidade}</td><td>{item.estoqueMinimo}</td><td><span className={item.quantidade <= item.estoqueMinimo ? 'movement-out' : 'movement-in'}>{item.quantidade === 0 ? 'Sem estoque' : item.quantidade <= item.estoqueMinimo ? 'Repor' : 'Disponível'}</span></td></tr>)}</tbody></table></div>{!visiveis.length && <p className="empty">Nenhum produto corresponde aos filtros.</p>}</div>
      {produto && <><div className="stock-selection"><h2>{produto.nome}</h2><p>{produto.quantidade} unidades disponíveis · mínimo {produto.estoqueMinimo}</p></div><div className="grid"><MovimentacaoForm key={produto.id} produto={produto} ocupado={ocupado || carregando} onSalvar={movimentar} /><div className="panel"><h2>Histórico de movimentações</h2>{erroHistorico ? <div><p className="message error" role="alert">{erroHistorico}</p><button className="secondary" onClick={() => setRevisao(valor => valor + 1)}>Tentar novamente</button></div> : historico ? <MovimentacaoList historico={historico} /> : <p role="status">Carregando histórico…</p>}</div></div></>}
    </>}
  </section>;
}
