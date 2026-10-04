import type { HistoricoEstoque } from '../types/Estoque';
export default function MovimentacaoList({ historico }: { historico: HistoricoEstoque }) {
  return <>{historico.total > 100 && <p className="form-hint">Exibindo as últimas 100 de {historico.total} movimentações. As anteriores continuam preservadas no banco.</p>}
    {historico.movimentacoes.length ? <ul className="records">{historico.movimentacoes.map(item => <li className="record" key={item.id}><div className="movement-heading"><strong className={item.tipo === 'ENTRADA' ? 'movement-in' : 'movement-out'}>{item.tipo === 'ENTRADA' ? '+' : '−'}{item.quantidade} · {item.tipo === 'ENTRADA' ? 'Entrada' : 'Saída'}</strong><time dateTime={item.criadoEm}>{new Date(item.criadoEm).toLocaleString('pt-BR')}</time></div><p className="movement-reason">{item.motivo}</p><p className="form-hint">Saldo: {item.saldoAnterior} → {item.saldoAtual} unidades · Registro #{item.id} · Usuário #{item.usuarioId}</p></li>)}</ul> : <p className="empty">Nenhuma movimentação ainda. Registre a primeira entrada deste produto.</p>}
  </>;
}
