package com.barbeariasorrisobarber.agendamento.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;

import com.barbeariasorrisobarber.agendamento.model.UsuarioAdmin;
import com.barbeariasorrisobarber.agendamento.repository.UsuarioAdminRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", 
                    "/index", 
                    "/index.html",
                    "/home",
                    "/sobre",
                    "/produtos",
                    "/servicos",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/login",
                    "/login/**",
                    "/favicon.ico"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/admin", true)
                .permitAll()
            )
            .logout(LogoutConfigurer::permitAll);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UsuarioAdminRepository usuarioRepository) {
        return username -> usuarioRepository.findByUsername(username)
                .map(this::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado."));
    }

    private org.springframework.security.core.userdetails.UserDetails toUserDetails(UsuarioAdmin usuario) {
        return User.withUsername(usuario.getUsername())
                .password(usuario.getSenhaHash())
                .roles("ADMIN")
                .build();
    }
}