import type { Permissao } from "../types/Permissao";

interface PermissaoItemProps {
    permissao: Permissao;
    onEditar: (permissao: Permissao) => void;
    onExcluir: (id: number) => void;
}

function PermissaoItem({ permissao, onEditar, onExcluir }: PermissaoItemProps) {
    return (
        <li>
            {permissao.id} — <strong>{permissao.nome}</strong> — {permissao.descricao}{" "}
            <button type="button" onClick={() => onEditar(permissao)}>Editar</button>{" "}
            <button type="button" onClick={() => onExcluir(permissao.id)}>Excluir</button>
        </li>
    );
}

export default PermissaoItem;
