package br.ueg.trindade.diego_web2_fullstack.config;

import br.ueg.trindade.diego_web2_fullstack.repository.UsuarioRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

class SessaoValidaFilter extends OncePerRequestFilter {
    private final UsuarioRepository usuarios;
    SessaoValidaFilter(UsuarioRepository usuarios) { this.usuarios = usuarios; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UsernamePasswordAuthenticationToken && auth.isAuthenticated()) {
            var usuario = usuarios.findById(Long.valueOf(auth.getName())).orElse(null);
            HttpSession session = request.getSession(false);
            if (usuario == null || !usuario.isEmailConfirmado() || usuario.getSenha() == null || session == null || !usuario.getSenha().equals(session.getAttribute("NEXUS_AUTH_HASH")) || !java.util.Objects.equals(usuario.getVersaoCredencial(), session.getAttribute("NEXUS_AUTH_VERSION"))) {
                SecurityContextHolder.clearContext(); if (session != null) session.invalidate();
                SecurityConfig.escreverErro(response, 401, "Sua sessão expirou. Entre novamente."); return;
            }
        }
        chain.doFilter(request, response);
    }
}
