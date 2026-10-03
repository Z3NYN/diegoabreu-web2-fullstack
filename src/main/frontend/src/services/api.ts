import axios from 'axios';
const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api', timeout: 30000 });
export default api;
export function mensagemErro(error: unknown): string {
  if (axios.isAxiosError(error) && error.code === 'ECONNABORTED') return 'O servidor demorou para responder. Atualize a lista antes de repetir a operação, pois ela pode ter sido concluída.';
  if (axios.isAxiosError(error)) return error.response?.data?.message || 'Não foi possível concluir a operação. Verifique se o servidor está disponível.';
  return 'Ocorreu um erro inesperado.';
}
