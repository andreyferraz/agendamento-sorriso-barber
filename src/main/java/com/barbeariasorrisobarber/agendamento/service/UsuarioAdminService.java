package com.barbeariasorrisobarber.agendamento.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barbeariasorrisobarber.agendamento.model.UsuarioAdmin;
import com.barbeariasorrisobarber.agendamento.repository.UsuarioAdminRepository;
import com.barbeariasorrisobarber.agendamento.utils.ValidationUtils;

@Service
public class UsuarioAdminService {

    private final UsuarioAdminRepository repository;
    private final PasswordEncoder passwordEncoder;
    private static final String USERNAME_FIELD = "username";

    public UsuarioAdminService(UsuarioAdminRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<UsuarioAdmin> buscarPorId(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        return repository.findById(id);
    }

    public Optional<UsuarioAdmin> buscarPorUsername(String username) {
        ValidationUtils.validarCampoStringObrigatorio(username, USERNAME_FIELD);
        return repository.findByUsername(username);
    }

    @Transactional
    public UsuarioAdmin criarAdmin(String username, String rawPassword) {
        ValidationUtils.validarCampoStringObrigatorio(username, USERNAME_FIELD);
        ValidationUtils.validarCampoStringObrigatorio(rawPassword, "password");

        repository.findByUsername(username).ifPresent(u -> {
            throw new IllegalArgumentException("Usuario já existe: " + username);
        });

        String hash = passwordEncoder.encode(rawPassword);

        UsuarioAdmin novo = new UsuarioAdmin(UUID.randomUUID(), username, hash);
        return repository.save(novo);
    }

    public boolean autenticar(String username, String rawPassword) {
        ValidationUtils.validarCampoStringObrigatorio(username, USERNAME_FIELD);
        ValidationUtils.validarCampoStringObrigatorio(rawPassword, "password");

        Optional<UsuarioAdmin> opt = repository.findByUsername(username);
        if (opt.isEmpty()) {
            return false;
        }

        return passwordEncoder.matches(rawPassword, opt.get().getSenhaHash());
    }

    @Transactional
    public UsuarioAdmin atualizarSenha(UUID id, String novaSenha) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        ValidationUtils.validarCampoStringObrigatorio(novaSenha, "novaSenha");

        UsuarioAdmin existente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado."));

        existente.setSenhaHash(passwordEncoder.encode(novaSenha));
        return repository.save(existente);
    }

    @Transactional
    public void deletar(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        repository.deleteById(id);
    }
}