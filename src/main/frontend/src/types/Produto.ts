export interface Produto {
    id: number;
    nome: string;
    descricao: string;
    preco: number;
}

export type ProdutoDados = Omit<Produto, "id">;
