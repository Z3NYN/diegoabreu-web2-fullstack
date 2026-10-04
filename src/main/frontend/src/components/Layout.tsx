import { useRef, useState } from 'react';
import Icon from './Icon';
import type { IconName } from './Icon';
import UsuariosPage from '../pages/UsuariosPage';
import PermissoesPage from '../pages/PermissoesPage';
import ProdutosPage from '../pages/ProdutosPage';
import EstoquePage from '../pages/EstoquePage';
const paginas = { estoque: { nome: 'Estoque', componente: EstoquePage }, produtos: { nome: 'Produtos', componente: ProdutosPage }, usuarios: { nome: 'Usuários', componente: UsuariosPage }, permissoes: { nome: 'Permissões', componente: PermissoesPage } };
const icones: Record<keyof typeof paginas, IconName> = { estoque: 'stock', produtos: 'box', usuarios: 'users', permissoes: 'key' };
export default function Layout({ nome, onSair, saindo }: { nome: string; onSair: () => void; saindo: boolean }) {
  const [pagina, setPagina] = useState<keyof typeof paginas>('estoque');
  const Pagina = paginas[pagina].componente;
  const conteudo = useRef<HTMLElement>(null);
  function navegar(destino: keyof typeof paginas) { setPagina(destino); requestAnimationFrame(() => { conteudo.current?.focus(); window.scrollTo({ top: 0, behavior: 'auto' }); }); }
  return <div className="workspace"><a href="#conteudo" className="skip-link">Pular para o conteúdo</a><aside className="sidebar"><div className="brand"><img src="/nexus.svg" width="36" height="36" alt="" /><div><strong>Nexus<span className="brand-period">.</span></strong><small>Controle de estoque</small></div></div><p className="nav-label">PLATAFORMA</p><nav aria-label="Navegação principal">{Object.entries(paginas).map(([key, item]) => <button key={key} aria-current={pagina === key ? 'page' : undefined} className={pagina === key ? 'active' : ''} onClick={() => navegar(key as keyof typeof paginas)}><Icon name={icones[key as keyof typeof paginas]} />{item.nome}</button>)}</nav><div className="sidebar-foot"><span className="account-avatar" aria-hidden="true">{nome.trim().slice(0, 1).toLocaleUpperCase('pt-BR')}</span><div><strong>{nome}</strong><small>Conta conectada</small></div></div></aside><div className="workspace-body"><header className="workspace-topbar"><p><span>Nexus</span><span className="breadcrumb-divider">/</span>{paginas[pagina].nome}</p><button className="quiet" disabled={saindo} onClick={onSair}><Icon name="logout" />{saindo ? 'Saindo…' : 'Sair'}</button></header><main id="conteudo" ref={conteudo} tabIndex={-1} className="workspace-main">{pagina === 'estoque' ? <EstoquePage onProdutos={() => navegar('produtos')} /> : <Pagina />}</main><footer className="workspace-footer">Nexus<span>Produtos e movimentações</span></footer></div></div>;
}
