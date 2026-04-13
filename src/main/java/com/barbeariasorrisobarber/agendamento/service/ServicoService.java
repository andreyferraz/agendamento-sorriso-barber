package com.barbeariasorrisobarber.agendamento.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barbeariasorrisobarber.agendamento.model.Servico;
import com.barbeariasorrisobarber.agendamento.repository.ServicoRepository;
import com.barbeariasorrisobarber.agendamento.utils.ValidationUtils;

@Service
public class ServicoService {

	private final ServicoRepository servicoRepository;

	public ServicoService(ServicoRepository servicoRepository) {
		this.servicoRepository = servicoRepository;
	}

	public List<Servico> listarTodos() {
		return StreamSupport.stream(servicoRepository.findAll().spliterator(), false)
				.toList();
	}

	public Optional<Servico> buscarPorId(UUID id) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		return servicoRepository.findById(id);
	}

	@Transactional
	public Servico criarServico(Servico servico) {
		ValidationUtils.validarCampoStringObrigatorio(servico.getNome(), "nome");
		ValidationUtils.validarCampoObrigatorio(servico.getPreco(), "preco");
		ValidationUtils.validarCampoObrigatorio(servico.getDuracao(), "duracao");

		BigDecimal preco = servico.getPreco();
		if (preco != null && preco.signum() < 0) {
			throw new IllegalArgumentException("Preco do servico nao pode ser negativo.");
		}

		if (servico.getId() == null) {
			servico.setId(UUID.randomUUID());
		}

		return servicoRepository.save(servico);
	}

	@Transactional
	public Servico atualizarServico(UUID id, Servico dados) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		ValidationUtils.validarCampoObrigatorio(dados, "dados");

		Servico existente = servicoRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Servico nao encontrado."));

		if (dados.getNome() != null) {
			ValidationUtils.validarCampoStringObrigatorio(dados.getNome(), "nome");
			existente.setNome(dados.getNome());
		}
		if (dados.getDescricao() != null) {
			existente.setDescricao(dados.getDescricao());
		}
		if (dados.getPreco() != null) {
			if (dados.getPreco().signum() < 0) {
				throw new IllegalArgumentException("Preco do servico nao pode ser negativo.");
			}
			existente.setPreco(dados.getPreco());
		}
		if (dados.getDuracao() != null) {
			existente.setDuracao(dados.getDuracao());
		}

		return servicoRepository.save(existente);
	}

	@Transactional
	public void deletar(UUID id) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		servicoRepository.deleteById(id);
	}

}
