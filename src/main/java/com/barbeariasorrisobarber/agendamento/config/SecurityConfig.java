package com.barbeariasorrisobarber.agendamento.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.barbeariasorrisobarber.agendamento.model.Barbeiro;
import com.barbeariasorrisobarber.agendamento.model.UsuarioAdmin;
import com.barbeariasorrisobarber.agendamento.repository.BarbeiroRepository;
import com.barbeariasorrisobarber.agendamento.repository.UsuarioAdminRepository;

@Configuration
public class SecurityConfig {

    private static final String LOGIN_PATH = "/login";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_BARBEIRO = "BARBEIRO";

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            http
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                        "/",
                        "/index",
                        "/index.html",
                        "/home",
                        "/agendar",
                        "/agendar/",
                        "/agendar/**",
                        "/agendar/reservar",
                        "/sobre",
                        "/produtos",
                        "/servicos",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/images/**",
                        "/uploads/**",
                        "/favicon.ico",
                        LOGIN_PATH
                    ).permitAll()
                    .requestMatchers("/admin", "/admin/**").hasRole(ROLE_ADMIN)
                    .requestMatchers("/barbeiro", "/barbeiro/**").hasAnyRole(ROLE_ADMIN, ROLE_BARBEIRO)
                    .anyRequest().authenticated()
                )
                .formLogin(form -> form
                    .loginPage(LOGIN_PATH)
                    .loginProcessingUrl(LOGIN_PATH)
                    .successHandler(authenticationSuccessHandler())
                    .failureUrl("/login?error=true")
                    .permitAll()
                )
                .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
                )
                .rememberMe(Customizer.withDefaults());

            return http.build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to build security filter chain.", ex);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UsuarioAdminRepository usuarioRepository,
            BarbeiroRepository barbeiroRepository) {
        return username -> usuarioRepository.findByUsername(username)
                .map(this::toAdminUserDetails)
                .orElseGet(() -> barbeiroRepository.findByUsername(username)
                        .map(this::toBarbeiroUserDetails)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username)));
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
            response.sendRedirect(isAdmin ? "/admin" : "/barbeiro");
        };
    }

    private org.springframework.security.core.userdetails.UserDetails toAdminUserDetails(UsuarioAdmin usuario) {
        return User.withUsername(usuario.getUsername())
                .password(usuario.getSenhaHash())
                .roles(ROLE_ADMIN)
                .build();
    }

    private org.springframework.security.core.userdetails.UserDetails toBarbeiroUserDetails(Barbeiro barbeiro) {
        return User.withUsername(barbeiro.getUsername())
                .password(barbeiro.getSenhaHash())
                .roles(ROLE_BARBEIRO)
                .build();
    }
}