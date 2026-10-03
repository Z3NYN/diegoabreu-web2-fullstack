package br.ueg.trindade.diego_web2_fullstack.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.*;
import br.ueg.trindade.diego_web2_fullstack.repository.UsuarioRepository;
import java.util.List;
@Configuration
public class SecurityConfig {
    @Bean public org.springframework.security.core.userdetails.UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return login -> {
            var usuario = usuarios.findByEmailIgnoreCaseOrUsernameIgnoreCase(login, login)
                .filter(user -> user.isEmailConfirmado() && user.getSenha() != null && user.getSenha().startsWith("$2"))
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Credenciais inválidas"));
            return org.springframework.security.core.userdetails.User.withUsername(usuario.getId().toString()).password(usuario.getSenha()).roles("USER").build();
        };
    }
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean public SecurityContextRepository securityContextRepository() { return new HttpSessionSecurityContextRepository(); }
    @Bean public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of("http://localhost:5173"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Content-Type", "X-CSRF-TOKEN")); cors.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**", cors); return source;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository context, UsuarioRepository usuarios) throws Exception {
        return http.cors(Customizer.withDefaults())
            .csrf(Customizer.withDefaults())
            .securityContext(security -> security.securityContextRepository(context))
            .formLogin(form -> form.disable()).httpBasic(basic -> basic.disable()).logout(logout -> logout.disable())
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, ex) -> escreverErro(response, 401, "Entre na sua conta para continuar."))
                .accessDeniedHandler((request, response, ex) -> escreverErro(response, 403, "Acesso negado ou sessão de segurança inválida. Atualize a página.")))
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/auth/csrf").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/auth/login", "/api/auth/registrar", "/api/auth/recuperacao", "/api/auth/redefinir-senha", "/api/auth/reenviar-confirmacao", "/api/usuarios/confirmar-email").permitAll()
                .requestMatchers("/api/**").authenticated().anyRequest().denyAll())
            .addFilterBefore(new SessaoValidaFilter(usuarios), AuthorizationFilter.class)
            .build();
    }
    static void escreverErro(jakarta.servlet.http.HttpServletResponse response, int status, String message) throws java.io.IOException {
        response.setStatus(status); response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
