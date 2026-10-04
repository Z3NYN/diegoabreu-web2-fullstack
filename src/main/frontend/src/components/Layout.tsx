import { useState } from 'react';
import UsuariosPage from '../pages/UsuariosPage';
import PermissoesPage from '../pages/PermissoesPage';
import ProdutosPage from '../pages/ProdutosPage';
import EstoquePage from '../pages/EstoquePage';
const paginas = { estoque: { nome: 'Estoque', componente: EstoquePage }, produtos: { nome: 'Produtos', componente: ProdutosPage }, usuarios: { nome: 'Usuários', componente: UsuariosPage }, permissoes: { nome: 'Permissões', componente: PermissoesPage } };
export default function Layout({ nome, onSair, saindo }: { nome: string; onSair: () => void; saindo: boolean }) {
  const [pagina, setPagina] = useState<keyof typeof paginas>('estoque');
  const Pagina = paginas[pagina].componente;
  return <><header><div className="brand"><img src="/nexus.svg" width="44" height="44" alt="" /><div><strong>Nexus</strong><small>Controle de estoque</small></div></div><nav aria-label="Cadastros">{Object.entries(paginas).map(([key, item]) => <button key={key} aria-current={pagina === key ? 'page' : undefined} className={pagina === key ? 'active' : ''} onClick={() => setPagina(key as keyof typeof paginas)}>{item.nome}</button>)}</nav><div className="session-actions"><span>{nome}</span><button className="secondary" disabled={saindo} onClick={onSair}>{saindo ? 'Saindo…' : 'Sair'}</button></div></header><main><Pagina /></main><footer>Nexus · Controle de estoque</footer></>;
}
