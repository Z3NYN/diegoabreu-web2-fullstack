import CadastroItem from './CadastroItem';
import type { Campo, Registro } from '../types/Cadastro';
interface Props { registros: Registro[]; campos: Campo[]; ocupado: boolean; onEditar: (registro: Registro) => void; onExcluir: (id: number) => void; onConfirmacao?: (id: number) => void }
export default function CadastroList({ registros, ...props }: Props) {
  return registros.length ? <ul className="records">{registros.map(registro => <CadastroItem key={registro.id} registro={registro} {...props} />)}</ul> : <p className="empty">Nenhum registro cadastrado. Use o formulário para começar.</p>;
}
