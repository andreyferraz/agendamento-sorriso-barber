package com.barbeariasorrisobarber.agendamento.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;

import com.barbeariasorrisobarber.agendamento.model.UsuarioAdmin;
import com.barbeariasorrisobarber.agendamento.repository.UsuarioAdminRepository;

@SpringBootTest
class UsuarioAdminServiceIntegrationTest {

    @Autowired
    private UsuarioAdminService usuarioAdminService;

    @Autowired
    private UsuarioAdminRepository usuarioAdminRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private static final Logger log = LoggerFactory.getLogger(UsuarioAdminServiceIntegrationTest.class);

    @Test
    void criarUsuarioEAutenticar() {
        String username = "testuser" + System.currentTimeMillis();
        String password = "Test@1234";

        UsuarioAdmin created = usuarioAdminService.criarAdmin(username, password);
        assertNotNull(created);
        System.out.println("jdbcTemplate.datasource=" + jdbcTemplate.getDataSource());
        System.out.println("autowired.datasource=" + dataSource);
        Optional<UsuarioAdmin> found = usuarioAdminRepository.findByUsername(username);
        int dbCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuario_admin WHERE username = ?", new Object[] { username }, Integer.class);
        System.out.println("db.count=" + dbCount);
        assertTrue(dbCount > 0, "Expected DB to contain the created user");
        assertTrue(found.isPresent(), "Repository should return the created user");
        assertNotNull(found.get().getSenhaHash());
        log.info("created.hash={}", created.getSenhaHash());
        log.info("found.hash={}", found.get().getSenhaHash());
        // direct DB check within same transaction/context
        String dbHash = jdbcTemplate.queryForObject("SELECT senha_hash FROM usuario_admin WHERE username = ?", new Object[] { username }, String.class);
        log.info("db.hash={}", dbHash);
        log.info("password matches created: {}", passwordEncoder.matches(password, created.getSenhaHash()));
        log.info("password matches found: {}", passwordEncoder.matches(password, found.get().getSenhaHash()));
        log.info("password matches db: {}", passwordEncoder.matches(password, dbHash));
        // verify password encoder directly
        assertTrue(passwordEncoder.matches(password, found.get().getSenhaHash()), "PasswordEncoder should match the saved hash");
        // verify service-level authentication
        boolean serviceAuth = usuarioAdminService.autenticar(username, password);
        System.out.println("service.auth=" + serviceAuth);
        System.out.println("password matches db (println): " + passwordEncoder.matches(password, dbHash));
        System.out.println("found.hash (println)=" + found.get().getSenhaHash());
        System.out.println("created.hash (println)=" + created.getSenhaHash());
        if (!serviceAuth) {
            String msg = "serviceAuth returned false. created=" + created.getSenhaHash()
                + " found=" + found.get().getSenhaHash() + " db=" + dbHash + " matchesFound="
                + passwordEncoder.matches(password, found.get().getSenhaHash());
            System.err.println(msg);
            throw new AssertionError(msg);
        }
    }
}
