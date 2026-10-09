import api from "./api";
import type { Permissao, PermissaoDados } from "../types/Permissao";

export const permissaoService = {
    listar: () => api.get<Permissao[]>("/permissoes"),
    buscar: (id: number) => api.get<Permissao>(`/permissoes/${id}`),
    criar: (dados: PermissaoDados) => api.post<Permissao>("/permissoes", dados),
    atualizar: (id: number, dados: PermissaoDados) =>
        api.put<Permissao>(`/permissoes/${id}`, dados),
    excluir: (id: number) => api.delete<void>(`/permissoes/${id}`),
};
