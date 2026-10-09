import api from "./api";
import type { Produto, ProdutoDados } from "../types/Produto";

export const produtoService = {
    listar: () => api.get<Produto[]>("/produtos"),
    buscar: (id: number) => api.get<Produto>(`/produtos/${id}`),
    criar: (dados: ProdutoDados) => api.post<Produto>("/produtos", dados),
    atualizar: (id: number, dados: ProdutoDados) =>
        api.put<Produto>(`/produtos/${id}`, dados),
    excluir: (id: number) => api.delete<void>(`/produtos/${id}`),
};
