import Layout from './components/Layout';
import ConfirmarEmailPage from './pages/ConfirmarEmailPage';
export default function App() {
  const token = new URLSearchParams(window.location.search).get('confirmar-email');
  return token !== null ? <ConfirmarEmailPage token={token} /> : <Layout />;
}
