import { useState } from "react";
import type { FormEvent } from "react";
import type { Produto, ProdutoDados } from "../types/Produto";
import { mensagemErro } from "../services/erroApi";

interface ProdutoFormProps {
    produto: Produto | null;
    onSalvar: (dados: ProdutoDados) => Promise<void>;
    onCancelar: () => void;
}

function ProdutoForm({ produto, onSalvar, onCancelar }: ProdutoFormProps) {
    const [nome, setNome] = useState(produto?.nome ?? "");
    const [descricao, setDescricao] = useState(produto?.descricao ?? "");
    const [preco, setPreco] = useState(produto ? String(produto.preco) : "");
    const [erro, setErro] = useState("");
    const [salvando, setSalvando] = useState(false);

    async function salvar(evento: FormEvent<HTMLFormElement>) {
        evento.preventDefault();
        setErro("");
        setSalvando(true);
        try {
            await onSalvar({ nome, descricao, preco: Number(preco) });
            setNome("");
            setDescricao("");
            setPreco("");
        } catch (falha) {
            setErro(mensagemErro(falha));
        } finally {
            setSalvando(false);
        }
    }

    return (
        <form onSubmit={salvar}>
            <h3>{produto ? `Editar produto ${produto.id}` : "Cadastrar produto"}</h3>
            <div><label htmlFor="produto-nome">Nome: </label><input id="produto-nome" value={nome} onChange={(e) => setNome(e.target.value)} required /></div>
            <div><label htmlFor="produto-descricao">Descrição: </label><input id="produto-descricao" value={descricao} onChange={(e) => setDescricao(e.target.value)} required /></div>
            <div><label htmlFor="produto-preco">Preço: </label><input id="produto-preco" type="number" min="0" step="0.01" value={preco} onChange={(e) => setPreco(e.target.value)} required /></div>
            <button type="submit" disabled={salvando}>{salvando ? "Salvando..." : "Salvar"}</button>{" "}
            {produto && <button type="button" onClick={onCancelar} disabled={salvando}>Cancelar edição</button>}
            {erro && <p role="alert">{erro}</p>}
        </form>
    );
}

export default ProdutoForm;
