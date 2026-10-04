import { useEffect, useRef } from 'react';
import type { Registro } from '../types/Cadastro';

export default function ConfirmarExclusao({ registro, ocupado, onCancelar, onConfirmar }: {
  registro: Registro; ocupado: boolean; onCancelar: () => void; onConfirmar: () => void;
}) {
  const dialog = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const elemento = dialog.current;
    const focoAnterior = document.activeElement instanceof HTMLElement ? document.activeElement : null;
    elemento?.showModal();
    return () => { elemento?.close(); focoAnterior?.focus(); };
  }, []);
  return <dialog ref={dialog} className="delete-dialog" aria-labelledby="delete-title" aria-describedby="delete-description" onCancel={event => { event.preventDefault(); if (!ocupado) onCancelar(); }}>
    <div className="delete-symbol" aria-hidden="true">!</div>
    <h2 id="delete-title">Excluir este registro?</h2>
    <p id="delete-description"><strong>{registro.nome}</strong> será removido permanentemente. Esta ação não pode ser desfeita.</p>
    <div className="actions"><button autoFocus className="secondary" disabled={ocupado} onClick={onCancelar}>Manter registro</button><button className="delete-confirm" disabled={ocupado} onClick={onConfirmar}>{ocupado ? 'Excluindo…' : 'Sim, excluir'}</button></div>
  </dialog>;
}
