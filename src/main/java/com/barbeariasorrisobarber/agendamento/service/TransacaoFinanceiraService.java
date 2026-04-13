package com.barbeariasorrisobarber.agendamento.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barbeariasorrisobarber.agendamento.model.TransacaoFinanceira;
import com.barbeariasorrisobarber.agendamento.repository.TransacaoFinanceiraRepository;
import com.barbeariasorrisobarber.agendamento.utils.ValidationUtils;

@Service
public class TransacaoFinanceiraService {

	private final TransacaoFinanceiraRepository repository;

	public TransacaoFinanceiraService(TransacaoFinanceiraRepository repository) {
		this.repository = repository;
	}

	public List<TransacaoFinanceira> listarTodos() {
		return StreamSupport.stream(repository.findAll().spliterator(), false)
				.toList();
	}

	public Optional<TransacaoFinanceira> buscarPorId(UUID id) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		return repository.findById(id);
	}

	@Transactional
	public TransacaoFinanceira criarTransacao(TransacaoFinanceira transacao) {
		ValidationUtils.validarCampoObrigatorio(transacao, "transacao");
		ValidationUtils.validarCampoObrigatorio(transacao.getTipo(), "tipo");
		ValidationUtils.validarCampoObrigatorio(transacao.getValor(), "valor");

		BigDecimal valor = transacao.getValor();
		if (valor.signum() <= 0) {
			throw new IllegalArgumentException("Valor da transacao deve ser maior que zero.");
		}

		if (transacao.getData() == null) {
			transacao.setData(LocalDateTime.now());
		}

		if (transacao.getId() == null) {
			transacao.setId(UUID.randomUUID());
		}

		return repository.save(transacao);
	}

	@Transactional
	public TransacaoFinanceira atualizarTransacao(UUID id, TransacaoFinanceira dados) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		ValidationUtils.validarCampoObrigatorio(dados, "dados");

		TransacaoFinanceira existente = repository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Transacao financeira nao encontrada."));

		if (dados.getTipo() != null) {
			existente.setTipo(dados.getTipo());
		}
		if (dados.getValor() != null) {
			if (dados.getValor().signum() <= 0) {
				throw new IllegalArgumentException("Valor da transacao deve ser maior que zero.");
			}
			existente.setValor(dados.getValor());
		}
		if (dados.getData() != null) {
			existente.setData(dados.getData());
		}
		if (dados.getDescricao() != null) {
			existente.setDescricao(dados.getDescricao());
		}
		if (dados.getAgendamentoId() != null) {
			existente.setAgendamentoId(dados.getAgendamentoId());
		}

		return repository.save(existente);
	}

	@Transactional
	public void deletar(UUID id) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		repository.deleteById(id);
	}

}
