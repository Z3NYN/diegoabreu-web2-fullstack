import AuthGate from './pages/AuthGate';
import ConfirmarEmailPage from './pages/ConfirmarEmailPage';
import RedefinirSenhaPage from './pages/RedefinirSenhaPage';
export default function App() {
  const params = new URLSearchParams(window.location.search);
  const confirmacao = params.get('confirmar-email');
  const recuperacao = params.get('recuperar-senha');
  if (confirmacao !== null) return <ConfirmarEmailPage token={confirmacao} />;
  if (recuperacao !== null) return <RedefinirSenhaPage token={recuperacao} />;
  return <AuthGate />;
}
