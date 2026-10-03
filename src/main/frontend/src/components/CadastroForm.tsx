import { useState } from 'react';
import type { FormEvent } from 'react';
import type { Campo, Registro } from '../types/Cadastro';
interface Props { campos: Campo[]; editando: Registro | null; ocupado: boolean; onSalvar: (dados: Omit<Registro, 'id'>) => Promise<boolean>; onCancelar: () => void }
export default function CadastroForm({ campos, editando, ocupado, onSalvar, onCancelar }: Props) {
  const [valores, setValores] = useState<Record<string, string>>(() => Object.fromEntries(campos.map(campo => [campo.nome, String(editando?.[campo.nome] ?? '')])));
  async function enviar(event: FormEvent) {
    event.preventDefault();
    const dados: Omit<Registro, 'id'> = { nome: (valores.nome || '').trim() };
    for (const campo of campos) {
      if (campo.nome === 'preco') dados.preco = Number(valores.preco);
      else dados[campo.nome] = (valores[campo.nome] || '').trim();
    }
    if (await onSalvar(dados)) setValores({});
  }
  return <form onSubmit={enviar} className="panel form">
    <h2>{editando ? 'Editar cadastro' : 'Novo cadastro'}</h2>
    {campos.map(campo => <label key={campo.nome}>{campo.label}
      <input name={campo.nome} type={campo.tipo || 'text'} required={campo.obrigatorio} maxLength={campo.nome === 'email' ? 254 : campo.nome === 'username' ? 50 : campo.nome === 'nome' ? 120 : 255} minLength={campo.nome === 'username' ? 3 : undefined} pattern={campo.nome === 'email' ? '[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}' : campo.nome === 'username' ? '[a-zA-Z0-9._\\-]{3,50}' : undefined} min={campo.tipo === 'number' ? 0 : undefined} max={campo.tipo === 'number' ? 999999999.99 : undefined} step={campo.tipo === 'number' ? '0.01' : undefined}
        value={valores[campo.nome] || ''} disabled={ocupado} onChange={event => setValores({ ...valores, [campo.nome]: event.target.value })} />
    </label>)}
    {campos.some(campo => campo.nome === 'email') && <p className="form-hint">A confirmação comprova o acesso à caixa de e-mail. Alterar o endereço exige uma nova confirmação.</p>}
    <div className="actions"><button disabled={ocupado} type="submit">{ocupado ? 'Aguarde…' : editando ? 'Salvar alterações' : 'Cadastrar'}</button>
      {editando && <button className="secondary" type="button" disabled={ocupado} onClick={onCancelar}>Cancelar edição</button>}</div>
  </form>;
}
