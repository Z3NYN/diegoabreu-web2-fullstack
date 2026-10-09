import api from "./api";
import type { Usuario, UsuarioDados } from "../types/Usuario";

export const usuarioService = {
    listar: () => api.get<Usuario[]>("/usuarios"),
    buscar: (id: number) => api.get<Usuario>(`/usuarios/${id}`),
    criar: (dados: UsuarioDados) => api.post<Usuario>("/usuarios", dados),
    atualizar: (id: number, dados: Omit<UsuarioDados, "senha">) =>
        api.put<Usuario>(`/usuarios/${id}`, dados),
    excluir: (id: number) => api.delete<void>(`/usuarios/${id}`),
};
