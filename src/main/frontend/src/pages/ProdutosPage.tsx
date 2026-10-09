import { useEffect, useState } from "react";
import ProdutoForm from "../components/ProdutoForm";
import ProdutoList from "../components/ProdutoList";
import { produtoService } from "../services/produtoService";
import { mensagemErro } from "../services/erroApi";
import type { Produto, ProdutoDados } from "../types/Produto";

function ProdutosPage() {
    const [produtos, setProdutos] = useState<Produto[]>([]);
    const [produtoEdicao, setProdutoEdicao] = useState<Produto | null>(null);
    const [erro, setErro] = useState("");
    const [carregando, setCarregando] = useState(true);

    useEffect(() => {
        produtoService.listar()
            .then((resposta) => setProdutos(resposta.data))
            .catch((falha: unknown) => setErro(mensagemErro(falha)))
            .finally(() => setCarregando(false));
    }, []);

    async function carregar() {
        setCarregando(true);
        try {
            const resposta = await produtoService.listar();
            setProdutos(resposta.data);
        } finally {
            setCarregando(false);
        }
    }

    async function salvar(dados: ProdutoDados) {
        setErro("");
        if (produtoEdicao) {
            await produtoService.atualizar(produtoEdicao.id, dados);
        } else {
            await produtoService.criar(dados);
        }
        setProdutoEdicao(null);
        await carregar();
    }

    async function editar(produto: Produto) {
        setErro("");
        try {
            const resposta = await produtoService.buscar(produto.id);
            setProdutoEdicao(resposta.data);
        } catch (falha) {
            setErro(mensagemErro(falha));
        }
    }

    async function excluir(id: number) {
        setErro("");
        try {
            await produtoService.excluir(id);
            if (produtoEdicao?.id === id) setProdutoEdicao(null);
            await carregar();
        } catch (falha) {
            setErro(mensagemErro(falha));
        }
    }

    return (
        <section aria-labelledby="produtos-titulo">
            <h2 id="produtos-titulo">Produtos</h2>
            <ProdutoForm key={produtoEdicao?.id ?? "novo"} produto={produtoEdicao} onSalvar={salvar} onCancelar={() => setProdutoEdicao(null)} />
            {erro && <p role="alert">{erro}</p>}
            <h3>Produtos cadastrados</h3>
            {carregando ? <p>Carregando...</p> : <ProdutoList produtos={produtos} onEditar={editar} onExcluir={excluir} />}
        </section>
    );
}

export default ProdutosPage;
