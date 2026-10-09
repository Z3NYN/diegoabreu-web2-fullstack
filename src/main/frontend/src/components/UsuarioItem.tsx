import type { Usuario } from "../types/Usuario";
interface UsuarioItemProps {
    usuario: Usuario;
    onEditar: (usuario: Usuario) => void;
    onExcluir: (id: number) => void;
}
function UsuarioItem({ usuario, onEditar, onExcluir }: UsuarioItemProps) {
    return (
        <li>
            {usuario.id} — <strong>{usuario.nome}</strong> ({usuario.username}) — {usuario.email}{" "}
            <button type="button" onClick={() => onEditar(usuario)}>Editar</button>{" "}
            <button type="button" onClick={() => onExcluir(usuario.id)}>Excluir</button>
        </li>
    );
}
export default UsuarioItem;
