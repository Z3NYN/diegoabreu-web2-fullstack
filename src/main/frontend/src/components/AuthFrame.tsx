import type { ReactNode } from 'react';
export default function AuthFrame({ children }: { children: ReactNode }) {
  return <main className="auth-shell">
    <aside className="auth-intro" aria-label="Nexus, controle de estoque">
      <div className="brand"><img src="/nexus.svg" width="40" height="40" alt="" /><strong>Nexus<span className="brand-period">.</span></strong></div>
      <div className="auth-visual" aria-hidden="true"><svg viewBox="0 0 460 340" fill="none" focusable="false">
        <path className="network-grid" d="M0 80h460M0 160h460M0 240h460M60 0v340M140 0v340M220 0v340M300 0v340M380 0v340" />
        <g className="network-routes"><path d="M60 240h80V80h160v160h80" /><path d="M60 80v80h320" /><path d="M220 80v160H60" /></g>
        <g className="network-flow"><path d="M60 240h80V80h160v160h80" /><path d="M60 80v80h320" /></g>
        <g className="network-nodes"><rect x="42" y="222" width="36" height="36" rx="3" /><rect x="122" y="62" width="36" height="36" rx="3" /><rect x="282" y="62" width="36" height="36" rx="3" /><rect x="362" y="222" width="36" height="36" rx="3" /><rect x="362" y="142" width="36" height="36" rx="3" /></g>
        <g className="network-core"><rect x="191" y="131" width="58" height="58" rx="4" /><path d="m205 160 15-8 15 8-15 8-15-8Zm0 0v16l15 8 15-8v-16M220 168v16" /></g>
      </svg><span className="diagram-label">CATÁLOGO / MOVIMENTAÇÕES / REPOSIÇÃO</span></div>
      <div className="auth-intro-copy"><p className="eyebrow">CONTROLE DE ESTOQUE</p><p className="auth-intro-title">Do produto<br />ao próximo movimento.</p><p>Saldo, entradas e saídas em um registro organizado.</p></div>
      <div className="auth-note">Nexus · Gestão de produtos e estoque</div>
    </aside>
    <section className="auth-content"><div className="auth-card">{children}</div><p className="auth-footer">Acesso por conta com e-mail confirmado.</p></section>
  </main>;
}
