package com.barbeariasorrisobarber.agendamento.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.barbeariasorrisobarber.agendamento.service.UsuarioAdminService;

/**
 * Inicializador que cria um usuário admin a partir de variáveis de ambiente.
 * Configure os secrets no GitHub com os nomes: ADMIN_USERNAME e ADMIN_PASSWORD
 * e forneça essas variáveis ao ambiente (deployment / CI) para que o usuário
 * admin seja criado na primeira inicialização.
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private final UsuarioAdminService usuarioAdminService;

    public AdminInitializer(UsuarioAdminService usuarioAdminService) {
        this.usuarioAdminService = usuarioAdminService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String username = System.getenv("ADMIN_USERNAME");
        String password = System.getenv("ADMIN_PASSWORD");

        if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
            // cria apenas se não existir
            var exists = usuarioAdminService.buscarPorUsername(username);
            if (exists.isEmpty()) {
                usuarioAdminService.criarAdmin(username, password);
            }
        }
    }
}
