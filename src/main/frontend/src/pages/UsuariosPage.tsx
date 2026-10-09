import { useEffect, useState } from "react";
import UsuarioForm from "../components/UsuarioForm";
import UsuarioList from "../components/UsuarioList";
import { usuarioService } from "../services/usuarioService";
import { mensagemErro } from "../services/erroApi";
import type { Usuario, UsuarioDados } from "../types/Usuario";

function UsuariosPage() {
    const [usuarios, setUsuarios] = useState<Usuario[]>([]);
    const [usuarioEdicao, setUsuarioEdicao] = useState<Usuario | null>(null);
    const [erro, setErro] = useState("");
    const [carregando, setCarregando] = useState(true);

    useEffect(() => {
        usuarioService.listar()
            .then((resposta) => setUsuarios(resposta.data))
            .catch((falha: unknown) => setErro(mensagemErro(falha)))
            .finally(() => setCarregando(false));
    }, []);

    async function carregar() {
        setCarregando(true);
        try {
            const resposta = await usuarioService.listar();
            setUsuarios(resposta.data);
        } finally {
            setCarregando(false);
        }
    }

    async function salvar(dados: UsuarioDados) {
        setErro("");
        if (usuarioEdicao) {
            // Conforme a Aula 05, PUT altera somente nome, username e email.
            await usuarioService.atualizar(usuarioEdicao.id, {
                nome: dados.nome,
                username: dados.username,
                email: dados.email,
            });
        } else {
            await usuarioService.criar(dados);
        }
        setUsuarioEdicao(null);
        await carregar();
    }

    async function editar(usuario: Usuario) {
        setErro("");
        try {
            const resposta = await usuarioService.buscar(usuario.id);
            setUsuarioEdicao(resposta.data);
        } catch (falha) {
            setErro(mensagemErro(falha));
        }
    }

    async function excluir(id: number) {
        setErro("");
        try {
            await usuarioService.excluir(id);
            if (usuarioEdicao?.id === id) setUsuarioEdicao(null);
            await carregar();
        } catch (falha) {
            setErro(mensagemErro(falha));
        }
    }

    return (
        <section aria-labelledby="usuarios-titulo">
            <h2 id="usuarios-titulo">Usuários</h2>
            <UsuarioForm key={usuarioEdicao?.id ?? "novo"} usuario={usuarioEdicao} onSalvar={salvar} onCancelar={() => setUsuarioEdicao(null)} />
            {erro && <p role="alert">{erro}</p>}
            <h3>Usuários cadastrados</h3>
            {carregando ? <p>Carregando...</p> : <UsuarioList usuarios={usuarios} onEditar={editar} onExcluir={excluir} />}
        </section>
    );
}

export default UsuariosPage;
