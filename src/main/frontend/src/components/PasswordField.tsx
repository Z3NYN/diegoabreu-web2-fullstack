import { useId, useState } from 'react';
import Icon from './Icon';
export default function PasswordField({ label, value, onChange, novo = false, hint }: { label: string; value: string; onChange: (value: string) => void; novo?: boolean; hint?: string }) {
  const [visivel, setVisivel] = useState(false);
  const id = useId();
  return <div className="password-field"><label htmlFor={id}>{label}</label><div className="password-input"><input id={id} type={visivel ? 'text' : 'password'} value={value} onChange={event => onChange(event.target.value)} required minLength={novo ? 12 : undefined} maxLength={72} autoComplete={novo ? 'new-password' : 'current-password'} aria-describedby={hint ? `${id}-hint` : undefined} /><button type="button" className="password-toggle" aria-label={`${visivel ? 'Ocultar' : 'Mostrar'} ${label.toLocaleLowerCase('pt-BR')}`} aria-pressed={visivel} onClick={() => setVisivel(!visivel)}><Icon name={visivel ? 'eyeOff' : 'eye'} /></button></div>{hint && <p id={`${id}-hint`} className="form-hint">{hint}</p>}</div>;
}
