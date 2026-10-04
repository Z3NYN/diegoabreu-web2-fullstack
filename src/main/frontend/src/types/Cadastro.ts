export interface Registro { id: number; nome: string; username?: string; email?: string; descricao?: string; emailConfirmado?: boolean; statusEmail?: string; preco?: number; quantidade?: number; estoqueMinimo?: number }
export interface Campo { nome: 'nome' | 'username' | 'email' | 'descricao' | 'preco' | 'estoqueMinimo'; label: string; tipo?: 'text' | 'email' | 'number'; obrigatorio?: boolean }
