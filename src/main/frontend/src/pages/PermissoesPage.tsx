import { useEffect, useState } from "react";
import PermissaoForm from "../components/PermissaoForm";
import PermissaoList from "../components/PermissaoList";
import { permissaoService } from "../services/permissaoService";
import { mensagemErro } from "../services/erroApi";
import type { Permissao, PermissaoDados } from "../types/Permissao";

function PermissoesPage() {
    const [permissoes, setPermissoes] = useState<Permissao[]>([]);
    const [permissaoEdicao, setPermissaoEdicao] = useState<Permissao | null>(null);
    const [erro, setErro] = useState("");
    const [carregando, setCarregando] = useState(true);

    useEffect(() => {
        permissaoService.listar()
            .then((resposta) => setPermissoes(resposta.data))
            .catch((falha: unknown) => setErro(mensagemErro(falha)))
            .finally(() => setCarregando(false));
    }, []);

    async function carregar() {
        setCarregando(true);
        try {
            const resposta = await permissaoService.listar();
            setPermissoes(resposta.data);
        } finally {
            setCarregando(false);
        }
    }

    async function salvar(dados: PermissaoDados) {
        setErro("");
        if (permissaoEdicao) {
            await permissaoService.atualizar(permissaoEdicao.id, dados);
        } else {
            await permissaoService.criar(dados);
        }
        setPermissaoEdicao(null);
        await carregar();
    }

    async function editar(permissao: Permissao) {
        setErro("");
        try {
            const resposta = await permissaoService.buscar(permissao.id);
            setPermissaoEdicao(resposta.data);
        } catch (falha) {
            setErro(mensagemErro(falha));
        }
    }

    async function excluir(id: number) {
        setErro("");
        try {
            await permissaoService.excluir(id);
            if (permissaoEdicao?.id === id) setPermissaoEdicao(null);
            await carregar();
        } catch (falha) {
            setErro(mensagemErro(falha));
        }
    }

    return (
        <section aria-labelledby="permissoes-titulo">
            <h2 id="permissoes-titulo">Permissões</h2>
            <PermissaoForm key={permissaoEdicao?.id ?? "nova"} permissao={permissaoEdicao} onSalvar={salvar} onCancelar={() => setPermissaoEdicao(null)} />
            {erro && <p role="alert">{erro}</p>}
            <h3>Permissões cadastradas</h3>
            {carregando ? <p>Carregando...</p> : <PermissaoList permissoes={permissoes} onEditar={editar} onExcluir={excluir} />}
        </section>
    );
}

export default PermissoesPage;
