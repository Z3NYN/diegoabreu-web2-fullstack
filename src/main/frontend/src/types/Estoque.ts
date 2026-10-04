export interface ProdutoEstoque { id: number; nome: string; preco: number; quantidade: number; estoqueMinimo: number }
export interface ResumoEstoque { produtos: number; unidades: number; estoqueBaixo: number; valorEstoque: number }
export interface Movimentacao { id: number; produtoId: number; usuarioId: number; tipo: 'ENTRADA' | 'SAIDA'; quantidade: number; saldoAnterior: number; saldoAtual: number; motivo: string; criadoEm: string }
export interface HistoricoEstoque { movimentacoes: Movimentacao[]; total: number }
export interface PedidoEstoque { tipo: 'ENTRADA' | 'SAIDA'; quantidade: number; motivo: string; chave: string }
