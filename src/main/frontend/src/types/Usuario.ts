export interface Usuario {
    id: number;
    nome: string;
    username: string;
    email: string;
}

// A senha participa somente do envio; nunca é retornada pela API.
export interface UsuarioDados {
    nome: string;
    username: string;
    email: string;
    senha: string;
}
