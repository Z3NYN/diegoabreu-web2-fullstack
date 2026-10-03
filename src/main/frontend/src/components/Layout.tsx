import { useState } from 'react';
import UsuariosPage from '../pages/UsuariosPage';
import PermissoesPage from '../pages/PermissoesPage';
import ProdutosPage from '../pages/ProdutosPage';
const paginas = { usuarios: { nome: 'Usuários', componente: UsuariosPage }, permissoes: { nome: 'Permissões', componente: PermissoesPage }, produtos: { nome: 'Produtos', componente: ProdutosPage } };
export default function Layout({ nome, onSair, saindo }: { nome: string; onSair: () => void; saindo: boolean }) {
  const [pagina, setPagina] = useState<keyof typeof paginas>('produtos');
  const Pagina = paginas[pagina].componente;
  return <><header><div className="brand"><img src="/nexus.svg" width="44" height="44" alt="" /><div><strong>Nexus</strong><small>Gestão de cadastros</small></div></div><nav aria-label="Cadastros">{Object.entries(paginas).map(([key, item]) => <button key={key} aria-current={pagina === key ? 'page' : undefined} className={pagina === key ? 'active' : ''} onClick={() => setPagina(key as keyof typeof paginas)}>{item.nome}</button>)}</nav><div className="session-actions"><span>{nome}</span><button className="secondary" disabled={saindo} onClick={onSair}>{saindo ? 'Saindo…' : 'Sair'}</button></div></header><main><Pagina /></main><footer>Nexus · Tudo conectado, gestão simplificada.</footer></>;
}
