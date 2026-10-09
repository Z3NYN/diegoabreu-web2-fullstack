import { useState } from "react";
import type { FormEvent } from "react";
import type { Usuario, UsuarioDados } from "../types/Usuario";
import { mensagemErro } from "../services/erroApi";

interface UsuarioFormProps {
    usuario: Usuario | null;
    onSalvar: (dados: UsuarioDados) => Promise<void>;
    onCancelar: () => void;
}

function UsuarioForm({ usuario, onSalvar, onCancelar }: UsuarioFormProps) {
    const [nome, setNome] = useState(usuario?.nome ?? "");
    const [username, setUsername] = useState(usuario?.username ?? "");
    const [email, setEmail] = useState(usuario?.email ?? "");
    const [senha, setSenha] = useState("");
    const [erro, setErro] = useState("");
    const [salvando, setSalvando] = useState(false);

    async function salvar(evento: FormEvent<HTMLFormElement>) {
        evento.preventDefault();
        setErro("");
        setSalvando(true);
        try {
            await onSalvar({ nome, username, email, senha });
            setNome("");
            setUsername("");
            setEmail("");
            setSenha("");
        } catch (falha) {
            setErro(mensagemErro(falha));
        } finally {
            setSalvando(false);
        }
    }

    return (
        <form onSubmit={salvar}>
            <h3>{usuario ? `Editar usuário ${usuario.id}` : "Cadastrar usuário"}</h3>
            <div><label htmlFor="usuario-nome">Nome: </label><input id="usuario-nome" value={nome} onChange={(e) => setNome(e.target.value)} required /></div>
            <div><label htmlFor="usuario-username">Username: </label><input id="usuario-username" value={username} onChange={(e) => setUsername(e.target.value)} required /></div>
            <div><label htmlFor="usuario-email">Email: </label><input id="usuario-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required /></div>
            {!usuario && <div><label htmlFor="usuario-senha">Senha: </label><input id="usuario-senha" type="password" value={senha} onChange={(e) => setSenha(e.target.value)} required /></div>}
            <button type="submit" disabled={salvando}>{salvando ? "Salvando..." : "Salvar"}</button>{" "}
            {usuario && <button type="button" onClick={onCancelar} disabled={salvando}>Cancelar edição</button>}
            {erro && <p role="alert">{erro}</p>}
        </form>
    );
}

export default UsuarioForm;
