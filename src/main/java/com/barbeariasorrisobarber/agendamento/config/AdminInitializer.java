package com.barbeariasorrisobarber.agendamento.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.barbeariasorrisobarber.agendamento.service.UsuarioAdminService;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
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

    private final JdbcTemplate jdbcTemplate;

    public AdminInitializer(UsuarioAdminService usuarioAdminService, JdbcTemplate jdbcTemplate) {
        this.usuarioAdminService = usuarioAdminService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Optional<BootstrapCredentials> credentials = resolveBootstrapCredentials();
        if (credentials.isEmpty()) {
            logger.info("No admin credentials provided via environment or properties. Admin not created.");
            return;
        }

        BootstrapCredentials bootstrapCredentials = credentials.get();
        Optional<com.barbeariasorrisobarber.agendamento.model.UsuarioAdmin> existingUser = findExistingUser(bootstrapCredentials.username());

        if (existingUser.isEmpty()) {
            usuarioAdminService.criarAdmin(bootstrapCredentials.username(), bootstrapCredentials.password());
            logger.info("Admin user created: {}", bootstrapCredentials.username());
            return;
        }

        logger.info("Admin user already exists: {}. Bootstrap password will not be applied again.", bootstrapCredentials.username());
    }

    private Optional<BootstrapCredentials> resolveBootstrapCredentials() {
        String username = System.getenv("ADMIN_USERNAME");
        String password = System.getenv("ADMIN_PASSWORD");

        // fallback para propriedades admin.bootstrap.* quando variáveis de ambiente não definidas
        if ((username == null || username.isBlank()) && bootstrapUsername != null && !bootstrapUsername.isBlank()) {
            username = bootstrapUsername;
            password = bootstrapPassword;
            logger.info("Using bootstrap admin credentials from application.properties");
        }

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(new BootstrapCredentials(username, password));
    }

    private Optional<com.barbeariasorrisobarber.agendamento.model.UsuarioAdmin> findExistingUser(String username) {
        try {
            return usuarioAdminService.buscarPorUsername(username);
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "";
            if (msg.contains("no such column") || msg.contains("foto_url")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE usuario_admin ADD COLUMN foto_url TEXT;");
                    logger.info("Applied migration: added foto_url column to usuario_admin");
                    return usuarioAdminService.buscarPorUsername(username);
                } catch (Exception migEx) {
                    logger.warn("Failed to apply migration for foto_url: {}", migEx.getMessage());
                    return Optional.empty();
                }
            }
            throw new IllegalStateException("Failed to load admin user: " + username, ex);
        }
    }

    private record BootstrapCredentials(String username, String password) {
    }
}
