import type { Produto } from "../types/Produto";

interface ProdutoItemProps {
    produto: Produto;
    onEditar: (produto: Produto) => void;
    onExcluir: (id: number) => void;
}

function ProdutoItem({ produto, onEditar, onExcluir }: ProdutoItemProps) {
    return (
        <li>
            {produto.id} — <strong>{produto.nome}</strong> — {produto.descricao} — Preço: {produto.preco}{" "}
            <button type="button" onClick={() => onEditar(produto)}>Editar</button>{" "}
            <button type="button" onClick={() => onExcluir(produto.id)}>Excluir</button>
        </li>
    );
}

export default ProdutoItem;
