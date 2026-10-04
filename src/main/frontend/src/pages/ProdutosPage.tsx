import CadastroPage from './CadastroPage';
import type { Campo } from '../types/Cadastro';
const campos: Campo[] = [{"nome": "nome", "label": "Nome", "tipo": "text", "obrigatorio": true}, {"nome": "preco", "label": "Preço (R$)", "tipo": "number", "obrigatorio": true}, {nome: "estoqueMinimo", label: "Estoque mínimo (unidades)", tipo: "number", obrigatorio: true}];
export default function ProdutosPage() { return <CadastroPage titulo="Produtos" recurso="produtos" campos={campos} />; }
