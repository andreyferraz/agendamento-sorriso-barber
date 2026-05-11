package com.barbeariasorrisobarber.agendamento.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.barbeariasorrisobarber.agendamento.model.Barbeiro;
import com.barbeariasorrisobarber.agendamento.repository.BarbeiroRepository;
import com.barbeariasorrisobarber.agendamento.utils.ValidationUtils;

@Service
public class BarbeiroService {

    private static final String NOME_FIELD = "nome";
    private static final String USERNAME_FIELD = "username";

    private final BarbeiroRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;

    public BarbeiroService(BarbeiroRepository repository, PasswordEncoder passwordEncoder,
            FileUploadService fileUploadService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.fileUploadService = fileUploadService;
    }

    public List<Barbeiro> listarTodos() {
        return StreamSupport.stream(repository.findAll().spliterator(), false).toList();
    }

    public Optional<Barbeiro> buscarPorId(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        return repository.findById(id);
    }

    public Optional<Barbeiro> buscarPorUsername(String username) {
        ValidationUtils.validarCampoStringObrigatorio(username, USERNAME_FIELD);
        return repository.findByUsername(username);
    }

    @Transactional
    public Barbeiro criarBarbeiro(Barbeiro dados, String rawPassword, MultipartFile foto) {
        validarDados(dados, rawPassword, true);
        garantirUsernameUnico(dados.getUsername(), null);

        Barbeiro novo = copiarDados(dados);
        novo.setId(UUID.randomUUID());
        novo.setSenhaHash(passwordEncoder.encode(rawPassword));
        novo.setNew(true);

        if (foto != null && !foto.isEmpty()) {
            novo.setFotoUrl(fileUploadService.salvarImagem(foto));
        }

        return repository.save(novo);
    }

    @Transactional
    public Barbeiro atualizarBarbeiro(UUID id, Barbeiro dados, String rawPassword, MultipartFile foto) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        validarDados(dados, rawPassword, false);

        Barbeiro existente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado."));

        garantirUsernameUnico(dados.getUsername(), id);

        String fotoAnterior = existente.getFotoUrl();
        copiarDados(dados, existente);

        if (rawPassword != null && !rawPassword.isBlank()) {
            existente.setSenhaHash(passwordEncoder.encode(rawPassword));
        }

        if (foto != null && !foto.isEmpty()) {
            existente.setFotoUrl(fileUploadService.salvarImagem(foto));
            if (fotoAnterior != null && !fotoAnterior.isBlank()) {
                try {
                    fileUploadService.removerImagem(fotoAnterior);
                } catch (Exception ex) {
                    // ignore cleanup failures on replacement
                }
            }
        }

        existente.setNew(false);
        return repository.save(existente);
    }

    @Transactional
    public void deletar(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        repository.findById(id).ifPresent(barbeiro -> {
            if (barbeiro.getFotoUrl() != null && !barbeiro.getFotoUrl().isBlank()) {
                try {
                    fileUploadService.removerImagem(barbeiro.getFotoUrl());
                } catch (Exception ex) {
                    // ignore cleanup failures
                }
            }
        });
        repository.deleteById(id);
    }

    public boolean autenticar(String username, String rawPassword) {
        ValidationUtils.validarCampoStringObrigatorio(username, USERNAME_FIELD);
        ValidationUtils.validarCampoStringObrigatorio(rawPassword, "password");

        Optional<Barbeiro> opt = repository.findByUsername(username);
        if (opt.isEmpty()) {
            return false;
        }

        return passwordEncoder.matches(rawPassword, opt.get().getSenhaHash());
    }

    private void validarDados(Barbeiro dados, String rawPassword, boolean senhaObrigatoria) {
        ValidationUtils.validarCampoObrigatorio(dados, "barbeiro");
        ValidationUtils.validarCampoStringObrigatorio(dados.getNome(), NOME_FIELD);
        ValidationUtils.validarCampoStringObrigatorio(dados.getUsername(), USERNAME_FIELD);
        ValidationUtils.validarCampoObrigatorio(dados.getComissaoPercentual(), "comissaoPercentual");

        if (senhaObrigatoria) {
            ValidationUtils.validarCampoStringObrigatorio(rawPassword, "password");
        }
    }

    private void garantirUsernameUnico(String username, UUID idAtual) {
        repository.findByUsername(username).ifPresent(existente -> {
            if (idAtual == null || !idAtual.equals(existente.getId())) {
                throw new IllegalArgumentException("Barbeiro já existe: " + username);
            }
        });
    }

    private Barbeiro copiarDados(Barbeiro origem) {
        Barbeiro destino = new Barbeiro();
        copiarDados(origem, destino);
        return destino;
    }

    private void copiarDados(Barbeiro origem, Barbeiro destino) {
        destino.setNome(origem.getNome());
        destino.setUsername(origem.getUsername());
        destino.setTelefone(origem.getTelefone());
        destino.setEmail(origem.getEmail());
        destino.setComissaoPercentual(origem.getComissaoPercentual());
        destino.setHorarioInicioAtendimento(origem.getHorarioInicioAtendimento());
        destino.setHorarioFimAtendimento(origem.getHorarioFimAtendimento());
        destino.setHorariosSegunda(origem.getHorariosSegunda());
        destino.setHorariosTerca(origem.getHorariosTerca());
        destino.setHorariosQuarta(origem.getHorariosQuarta());
        destino.setHorariosQuinta(origem.getHorariosQuinta());
        destino.setHorariosSexta(origem.getHorariosSexta());
        destino.setHorariosSabado(origem.getHorariosSabado());
        destino.setHorariosDomingo(origem.getHorariosDomingo());
        destino.setHorarioSegundaInicio(origem.getHorarioSegundaInicio());
        destino.setHorarioSegundaFim(origem.getHorarioSegundaFim());
        destino.setHorarioTercaInicio(origem.getHorarioTercaInicio());
        destino.setHorarioTercaFim(origem.getHorarioTercaFim());
        destino.setHorarioQuartaInicio(origem.getHorarioQuartaInicio());
        destino.setHorarioQuartaFim(origem.getHorarioQuartaFim());
        destino.setHorarioQuintaInicio(origem.getHorarioQuintaInicio());
        destino.setHorarioQuintaFim(origem.getHorarioQuintaFim());
        destino.setHorarioSextaInicio(origem.getHorarioSextaInicio());
        destino.setHorarioSextaFim(origem.getHorarioSextaFim());
        destino.setHorarioSabadoInicio(origem.getHorarioSabadoInicio());
        destino.setHorarioSabadoFim(origem.getHorarioSabadoFim());
        destino.setHorarioDomingoInicio(origem.getHorarioDomingoInicio());
        destino.setHorarioDomingoFim(origem.getHorarioDomingoFim());
    }
}
