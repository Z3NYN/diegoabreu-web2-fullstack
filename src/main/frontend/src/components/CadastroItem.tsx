import type { Campo, Registro } from '../types/Cadastro';
interface Props { registro: Registro; campos: Campo[]; ocupado: boolean; onEditar: (registro: Registro) => void; onExcluir: (id: number) => void; onConfirmacao?: (id: number) => void }
export default function CadastroItem({ registro, campos, ocupado, onEditar, onExcluir, onConfirmacao }: Props) {
  return <li className="record"><div className="details">{campos.map(campo => <div key={campo.nome}><span>{campo.label}</span><strong>{campo.nome === 'preco' ? Number(registro[campo.nome]).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) : String(registro[campo.nome] ?? '—')}</strong></div>)}</div>
    {registro.email && <p className={`email-badge ${registro.emailConfirmado ? 'verified' : ''}`}>{registro.emailConfirmado ? '✓ E-mail confirmado' : registro.statusEmail === 'AGUARDANDO_CONFIRMACAO' ? 'Aguardando confirmação por e-mail' : 'E-mail ainda não confirmado'}</p>}
    <div className="actions">{onConfirmacao && !registro.emailConfirmado && <button className="secondary" disabled={ocupado} onClick={() => onConfirmacao(registro.id)}>{registro.statusEmail === 'AGUARDANDO_CONFIRMACAO' ? 'Reenviar confirmação' : 'Enviar confirmação'}</button>}<button className="secondary" data-edit-id={registro.id} disabled={ocupado} onClick={() => onEditar(registro)}>Editar</button><button className="danger" disabled={ocupado} onClick={() => onExcluir(registro.id)}>Excluir</button></div>
  </li>;
}
