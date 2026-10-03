import CadastroPage from './CadastroPage';
import type { Campo } from '../types/Cadastro';
const campos: Campo[] = [{"nome": "nome", "label": "Nome", "tipo": "text", "obrigatorio": true}, {"nome": "descricao", "label": "Descrição", "tipo": "text", "obrigatorio": false}];
export default function PermissoesPage() { return <CadastroPage titulo="Permissões" recurso="permissoes" campos={campos} />; }
