export interface Permissao {
    id: number;
    nome: string;
    descricao: string;
}

export type PermissaoDados = Omit<Permissao, "id">;
