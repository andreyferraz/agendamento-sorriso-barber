package com.barbeariasorrisobarber.agendamento.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import com.barbeariasorrisobarber.agendamento.model.UsuarioAdmin;
import com.barbeariasorrisobarber.agendamento.repository.UsuarioAdminRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UsuarioAdminServiceTest {

    @Mock
    private UsuarioAdminRepository repository;

    @Mock
    private FileUploadService fileUploadService;

    private UsuarioAdminService service;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
        service = new UsuarioAdminService(repository, passwordEncoder, fileUploadService);
    }

    @Test
    void criarAdmin_deveSalvarEHashSenha() {
        when(repository.findByUsername("admin")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        UsuarioAdmin saved = service.criarAdmin("admin", "secret123");

        assertNotNull(saved.getId());
        assertEquals("admin", saved.getUsername());
        assertTrue(passwordEncoder.matches("secret123", saved.getSenhaHash()));
        verify(repository).save(any(UsuarioAdmin.class));
    }

    @Test
    void criarAdmin_usuarioExistente_deveLancar() {
        UsuarioAdmin existent = new UsuarioAdmin(UUID.randomUUID(), "admin", "hash", null, true);
        when(repository.findByUsername("admin")).thenReturn(Optional.of(existent));

        assertThrows(IllegalArgumentException.class, () -> service.criarAdmin("admin", "pwd"));
    }

    @Test
    void autenticar_deveRetornarTrueQuandoSenhaCorreta() {
        String raw = "mypwd";
        String hash = passwordEncoder.encode(raw);
        UsuarioAdmin existent = new UsuarioAdmin(UUID.randomUUID(), "u", hash, null, true);
        when(repository.findByUsername("u")).thenReturn(Optional.of(existent));

        assertTrue(service.autenticar("u", raw));
    }

    @Test
    void autenticar_deveRetornarFalseQuandoUsuarioNaoEncontrado() {
        when(repository.findByUsername("missing")).thenReturn(Optional.empty());
        assertFalse(service.autenticar("missing", "any"));
    }

    @Test
    void autenticar_deveRetornarFalseQuandoSenhaIncorreta() {
        String hash = passwordEncoder.encode("right");
        UsuarioAdmin existent = new UsuarioAdmin(UUID.randomUUID(), "u2", hash, null, true);
        when(repository.findByUsername("u2")).thenReturn(Optional.of(existent));

        assertFalse(service.autenticar("u2", "wrong"));
    }

    @Test
    void buscarPorId_nullId_deveLancar() {
        assertThrows(IllegalArgumentException.class, () -> service.buscarPorId(null));
    }

    @Test
    void buscarPorUsername_nullOuVazio_deveLancar() {
        assertThrows(IllegalArgumentException.class, () -> service.buscarPorUsername(null));
        assertThrows(IllegalArgumentException.class, () -> service.buscarPorUsername("\n\t"));
    }

    @Test
    void atualizarSenha_deveAtualizarHash() {
        UUID id = UUID.randomUUID();
        String oldHash = passwordEncoder.encode("old");
        UsuarioAdmin existente = new UsuarioAdmin(id, "adm", oldHash, null, true);
        when(repository.findById(id)).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        UsuarioAdmin updated = service.atualizarSenha(id, "newpass");

        assertNotEquals(oldHash, updated.getSenhaHash());
        assertTrue(passwordEncoder.matches("newpass", updated.getSenhaHash()));
        verify(repository).save(any(UsuarioAdmin.class));
    }

    @Test
    void atualizarSenha_usuarioNaoEncontrado_deveLancar() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.atualizarSenha(id, "x"));
    }

    @Test
    void deletar_deveChamarRepository() {
        UUID id = UUID.randomUUID();
        UsuarioAdmin u = new UsuarioAdmin(id, "toDel", "hash", "img.webp", true);
        when(repository.findById(id)).thenReturn(Optional.of(u));
        doNothing().when(fileUploadService).removerImagem("img.webp");
        doNothing().when(repository).deleteById(id);

        service.deletar(id);

        verify(fileUploadService).removerImagem("img.webp");
        verify(repository).deleteById(id);
    }
}
