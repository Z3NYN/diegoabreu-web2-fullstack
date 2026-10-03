import CadastroPage from './CadastroPage';
import type { Campo } from '../types/Cadastro';
const campos: Campo[] = [{"nome": "nome", "label": "Nome", "tipo": "text", "obrigatorio": true}, {"nome": "username", "label": "Username", "tipo": "text", "obrigatorio": true}, {"nome": "email", "label": "E-mail", "tipo": "email", "obrigatorio": true}];
export default function UsuariosPage() { return <CadastroPage titulo="Usuários" recurso="usuarios" campos={campos} />; }
