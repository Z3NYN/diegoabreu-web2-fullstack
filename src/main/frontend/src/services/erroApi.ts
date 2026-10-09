import { isAxiosError } from "axios";

export function mensagemErro(erro: unknown): string {
    if (isAxiosError(erro)) {
        const dados: unknown = erro.response?.data;
        if (typeof dados === "string" && dados.trim()) {
            return dados;
        }
        if (dados && typeof dados === "object" && "message" in dados &&
            typeof dados.message === "string") {
            return dados.message;
        }
        if (erro.response) {
            return `Não foi possível concluir a operação (HTTP ${erro.response.status}).`;
        }
        return "Não foi possível acessar a API. Confira se o backend está rodando na porta 8080.";
    }
    return "Não foi possível concluir a operação.";
}
