import { useState } from "react";
import type { FormEvent } from "react";
import type { Permissao, PermissaoDados } from "../types/Permissao";
import { mensagemErro } from "../services/erroApi";

interface PermissaoFormProps {
    permissao: Permissao | null;
    onSalvar: (dados: PermissaoDados) => Promise<void>;
    onCancelar: () => void;
}

function PermissaoForm({ permissao, onSalvar, onCancelar }: PermissaoFormProps) {
    const [nome, setNome] = useState(permissao?.nome ?? "");
    const [descricao, setDescricao] = useState(permissao?.descricao ?? "");
    const [erro, setErro] = useState("");
    const [salvando, setSalvando] = useState(false);

    async function salvar(evento: FormEvent<HTMLFormElement>) {
        evento.preventDefault();
        setErro("");
        setSalvando(true);
        try {
            await onSalvar({ nome, descricao });
            setNome("");
            setDescricao("");
        } catch (falha) {
            setErro(mensagemErro(falha));
        } finally {
            setSalvando(false);
        }
    }

    return (
        <form onSubmit={salvar}>
            <h3>{permissao ? `Editar permissão ${permissao.id}` : "Cadastrar permissão"}</h3>
            <div><label htmlFor="permissao-nome">Nome: </label><input id="permissao-nome" value={nome} onChange={(e) => setNome(e.target.value)} required /></div>
            <div><label htmlFor="permissao-descricao">Descrição: </label><input id="permissao-descricao" value={descricao} onChange={(e) => setDescricao(e.target.value)} required /></div>
            <button type="submit" disabled={salvando}>{salvando ? "Salvando..." : "Salvar"}</button>{" "}
            {permissao && <button type="button" onClick={onCancelar} disabled={salvando}>Cancelar edição</button>}
            {erro && <p role="alert">{erro}</p>}
        </form>
    );
}

export default PermissaoForm;
