import { useRef, useState } from 'react';
import type { FormEvent } from 'react';
import type { PedidoEstoque, ProdutoEstoque } from '../types/Estoque';
import Icon from './Icon';
export default function MovimentacaoForm({ produto, ocupado, onSalvar }: { produto: ProdutoEstoque; ocupado: boolean; onSalvar: (pedido: PedidoEstoque) => Promise<boolean> }) {
  const [tipo, setTipo] = useState<PedidoEstoque['tipo']>('ENTRADA');
  const [quantidade, setQuantidade] = useState('');
  const [motivo, setMotivo] = useState('');
  const operacao = useRef<{ assinatura: string; chave: string } | null>(null);
  const campoQuantidade = useRef<HTMLInputElement>(null);
  async function enviar(event: FormEvent) {
    event.preventDefault();
    const assinatura = JSON.stringify([tipo, Number(quantidade), motivo.trim()]);
    if (operacao.current?.assinatura !== assinatura) operacao.current = { assinatura, chave: crypto.randomUUID() };
    if (await onSalvar({ tipo, quantidade: Number(quantidade), motivo: motivo.trim(), chave: operacao.current.chave })) { setQuantidade(''); setMotivo(''); operacao.current = null; }
    requestAnimationFrame(() => campoQuantidade.current?.focus());
  }
  const numero = Number(quantidade);
  const limite = tipo === 'SAIDA' ? produto.quantidade : 1000000 - produto.quantidade;
  const valido = quantidade !== '' && Number.isInteger(numero) && numero >= 1 && numero <= limite;
  const saldoPrevisto = produto.quantidade + (tipo === 'ENTRADA' ? numero : -numero);
  return <form className="panel movement-form" onSubmit={event => void enviar(event)} aria-busy={ocupado}><h2>Registrar movimentação</h2><p className="form-hint">Produto: <strong>{produto.nome}</strong>. Informe a quantidade e o motivo do lançamento.</p><fieldset disabled={ocupado}>
    <fieldset className="type-switch"><legend>Tipo de movimentação</legend><div className="type-switch-options"><label><input type="radio" name={`tipo-${produto.id}`} value="ENTRADA" checked={tipo === 'ENTRADA'} onChange={() => setTipo('ENTRADA')} />Entrada</label><label><input type="radio" name={`tipo-${produto.id}`} value="SAIDA" checked={tipo === 'SAIDA'} onChange={() => setTipo('SAIDA')} />Saída</label></div></fieldset>
    <label className="hinted-label">Quantidade (unidades)<input ref={campoQuantidade} type="number" value={quantidade} onChange={event => setQuantidade(event.target.value)} required min={1} max={limite} step={1} aria-describedby="quantidade-hint" /></label><p className="field-hint" id="quantidade-hint">{tipo === 'SAIDA' ? `Até ${produto.quantidade} unidades disponíveis.` : `Até ${limite.toLocaleString('pt-BR')} unidades nesta entrada.`}</p>
    <label>Motivo<input value={motivo} onChange={event => setMotivo(event.target.value)} required minLength={3} maxLength={255} placeholder={tipo === 'ENTRADA' ? 'Ex.: reposição de mercadoria' : 'Ex.: venda ou consumo interno'} /></label>
    <div className="movement-preview"><span>Saldo atual <strong>{produto.quantidade}</strong></span><span>Após o lançamento <strong>{valido ? saldoPrevisto : '—'}</strong></span></div>
    <p className="form-hint">{tipo === 'SAIDA' && produto.quantidade === 0 ? 'Não há unidades disponíveis para saída.' : 'Confira os dados. O lançamento ficará no histórico.'}</p>
    <button className="auth-submit" disabled={ocupado || (tipo === 'SAIDA' && produto.quantidade === 0) || (tipo === 'ENTRADA' && produto.quantidade === 1000000)}>{ocupado ? 'Registrando…' : tipo === 'ENTRADA' ? 'Registrar entrada' : 'Registrar saída'}<Icon name="arrow" /></button>
  </fieldset></form>;
}
