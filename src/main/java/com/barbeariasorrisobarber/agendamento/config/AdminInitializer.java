package com.barbeariasorrisobarber.agendamento.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.barbeariasorrisobarber.agendamento.service.UsuarioAdminService;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inicializador que cria um usuário admin a partir de variáveis de ambiente.
 * Configure os secrets no GitHub com os nomes: ADMIN_USERNAME e ADMIN_PASSWORD
 * e forneça essas variáveis ao ambiente (deployment / CI) para que o usuário
 * admin seja criado na primeira inicialização.
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private final UsuarioAdminService usuarioAdminService;
    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    @Value("${admin.bootstrap.username:}")
    private String bootstrapUsername;

    @Value("${admin.bootstrap.password:}")
    private String bootstrapPassword;

    public AdminInitializer(UsuarioAdminService usuarioAdminService) {
        this.usuarioAdminService = usuarioAdminService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String username = System.getenv("ADMIN_USERNAME");
        String password = System.getenv("ADMIN_PASSWORD");

        // fallback para propriedades admin.bootstrap.* quando variáveis de ambiente não definidas
        if ((username == null || username.isBlank()) && bootstrapUsername != null && !bootstrapUsername.isBlank()) {
            username = bootstrapUsername;
            password = bootstrapPassword;
            logger.info("Using bootstrap admin credentials from application.properties");
        }

        if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
            var exists = usuarioAdminService.buscarPorUsername(username);
            if (exists.isEmpty()) {
                usuarioAdminService.criarAdmin(username, password);
                logger.info("Admin user created: {}", username);
            } else {
                // if password provided via env/properties, update the existing user's password
                try {
                    var existing = exists.get();
                    usuarioAdminService.atualizarSenha(existing.getId(), password);
                    logger.info("Admin user password updated for: {}", username);
                } catch (Exception e) {
                    logger.warn("Failed to update admin password for {}: {}", username, e.getMessage());
                }
            }
        } else {
            logger.info("No admin credentials provided via environment or properties. Admin not created.");
        }
    }
}
