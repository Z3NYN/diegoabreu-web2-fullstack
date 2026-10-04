import api from './api';
import type { HistoricoEstoque, PedidoEstoque, ResumoEstoque } from '../types/Estoque';
export const estoqueService = {
  resumo: () => api.get<ResumoEstoque>('/estoque/resumo'),
  historico: (id: number) => api.get<HistoricoEstoque>(`/estoque/produtos/${id}/movimentacoes`),
  movimentar: (id: number, dados: PedidoEstoque) => api.post(`/estoque/produtos/${id}/movimentacoes`, dados),
};
