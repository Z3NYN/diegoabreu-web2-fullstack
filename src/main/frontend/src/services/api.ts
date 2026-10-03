import axios from 'axios';
const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api', timeout: 30000, withCredentials: true });
api.interceptors.request.use(async config => {
  if (!['get', 'head', 'options'].includes(config.method || 'get')) {
    try {
      const { data } = await api.get<{ token: string }>('/auth/csrf');
      config.headers.set('X-CSRF-TOKEN', data.token);
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 401) window.dispatchEvent(new Event('nexus:session-expired'));
      throw error;
    }
  }
  return config;
});
api.interceptors.response.use(response => response, error => {
  if (axios.isAxiosError(error) && error.response?.status === 401 && !error.config?.url?.startsWith('/auth/'))
    window.dispatchEvent(new Event('nexus:session-expired'));
  return Promise.reject(error);
});
export default api;
export function mensagemErro(error: unknown): string {
  if (axios.isAxiosError(error) && error.code === 'ECONNABORTED') return 'O servidor demorou para responder. Atualize a lista antes de repetir a operação, pois ela pode ter sido concluída.';
  if (axios.isAxiosError(error)) return error.response?.data?.message || 'Não foi possível concluir a operação. Verifique se o servidor está disponível.';
  return 'Ocorreu um erro inesperado.';
}
