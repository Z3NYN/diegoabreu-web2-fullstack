import { useRef, useState } from 'react';
import type { FormEvent } from 'react';
import type { PedidoEstoque, ProdutoEstoque } from '../types/Estoque';
export default function MovimentacaoForm({ produto, ocupado, onSalvar }: { produto: ProdutoEstoque; ocupado: boolean; onSalvar: (pedido: PedidoEstoque) => Promise<boolean> }) {
  const [tipo, setTipo] = useState<PedidoEstoque['tipo']>('ENTRADA');
  const [quantidade, setQuantidade] = useState('');
  const [motivo, setMotivo] = useState('');
  const operacao = useRef<{ assinatura: string; chave: string } | null>(null);
  async function enviar(event: FormEvent) {
    event.preventDefault();
    const assinatura = JSON.stringify([tipo, Number(quantidade), motivo.trim()]);
    if (operacao.current?.assinatura !== assinatura) operacao.current = { assinatura, chave: crypto.randomUUID() };
    if (await onSalvar({ tipo, quantidade: Number(quantidade), motivo: motivo.trim(), chave: operacao.current.chave })) { setQuantidade(''); setMotivo(''); operacao.current = null; }
  }
  return <form className="panel" onSubmit={event => void enviar(event)}><h2>Movimentar estoque</h2><p className="form-hint">Registre o que entrou ou saiu de <strong>{produto.nome}</strong>. O histórico é permanente.</p><fieldset disabled={ocupado}>
    <label>Tipo de movimentação<select value={tipo} onChange={event => setTipo(event.target.value as PedidoEstoque['tipo'])}><option value="ENTRADA">Entrada — adicionar unidades</option><option value="SAIDA">Saída — retirar unidades</option></select></label>
    <label>Quantidade (unidades)<input type="number" value={quantidade} onChange={event => setQuantidade(event.target.value)} required min={1} max={tipo === 'SAIDA' ? produto.quantidade : 1000000 - produto.quantidade} step={1} /></label>
    <label>Motivo<input value={motivo} onChange={event => setMotivo(event.target.value)} required minLength={3} maxLength={255} placeholder={tipo === 'ENTRADA' ? 'Ex.: reposição de mercadoria' : 'Ex.: venda ou consumo interno'} /></label>
    <p className="form-hint">Saldo disponível: <strong>{produto.quantidade} unidades</strong>. {tipo === 'SAIDA' && produto.quantidade === 0 ? 'Não há unidades disponíveis para saída.' : 'Confira os dados antes de registrar.'}</p>
    <button className="auth-submit" disabled={ocupado || (tipo === 'SAIDA' && produto.quantidade === 0) || (tipo === 'ENTRADA' && produto.quantidade === 1000000)}>{ocupado ? 'Registrando…' : tipo === 'ENTRADA' ? 'Registrar entrada' : 'Registrar saída'}</button>
  </fieldset></form>;
}
