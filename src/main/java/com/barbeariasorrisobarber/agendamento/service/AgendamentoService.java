package com.barbeariasorrisobarber.agendamento.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barbeariasorrisobarber.agendamento.enuns.StatusAgendamento;
import com.barbeariasorrisobarber.agendamento.model.Agendamento;
import com.barbeariasorrisobarber.agendamento.model.Servico;
import com.barbeariasorrisobarber.agendamento.repository.AgendamentoRepository;
import com.barbeariasorrisobarber.agendamento.repository.ServicoRepository;
import com.barbeariasorrisobarber.agendamento.utils.ValidationUtils;

@Service
public class AgendamentoService {

	private final AgendamentoRepository agendamentoRepository;
	private final ServicoRepository servicoRepository;

	public AgendamentoService(AgendamentoRepository agendamentoRepository, ServicoRepository servicoRepository) {
		this.agendamentoRepository = agendamentoRepository;
		this.servicoRepository = servicoRepository;
	}

	public List<Agendamento> listarTodos() {
		return StreamSupport.stream(agendamentoRepository.findAll().spliterator(), false)
				.toList();
	}

	public Optional<Agendamento> buscarPorId(UUID id) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		return agendamentoRepository.findById(id);
	}

	@Transactional
	public Agendamento criarAgendamento(Agendamento agendamento) {
		validarCamposBasicos(agendamento);
		calcularDataHoraFimSeAusente(agendamento);

		if (agendamento.getStatus() == null) {
			agendamento.setStatus(StatusAgendamento.PENDENTE);
		}

		checarConflitosHorario(agendamento);

		if (agendamento.getId() == null) {
			agendamento.setId(UUID.randomUUID());
		}

		return agendamentoRepository.save(agendamento);
	}

	private void validarCamposBasicos(Agendamento agendamento) {
		ValidationUtils.validarCampoObrigatorio(agendamento, "agendamento");
		ValidationUtils.validarCampoStringObrigatorio(agendamento.getNomeCliente(), "nomeCliente");
		ValidationUtils.validarCampoStringObrigatorio(agendamento.getTelefoneCliente(), "telefoneCliente");
		ValidationUtils.validarCampoObrigatorio(agendamento.getServicoId(), "servicoId");
		ValidationUtils.validarCampoObrigatorio(agendamento.getDataHoraInicio(), "dataHoraInicio");
	}

	private void calcularDataHoraFimSeAusente(Agendamento agendamento) {
		if (agendamento.getDataHoraFim() != null) {
			return;
		}

		Servico servico = servicoRepository.findById(agendamento.getServicoId())
				.orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado para id informado."));
		if (servico.getDuracao() == null) {
			throw new IllegalArgumentException("Duração do serviço não informada.");
		}
		agendamento.setDataHoraFim(agendamento.getDataHoraInicio().plusMinutes(servico.getDuracao()));
	}

	private void checarConflitosHorario(Agendamento agendamento) {
		List<Agendamento> existentes = listarTodos();
		for (Agendamento e : existentes) {
			LocalDateTime s1 = agendamento.getDataHoraInicio();
			LocalDateTime e1 = agendamento.getDataHoraFim();
			LocalDateTime s2 = e.getDataHoraInicio();
			LocalDateTime e2 = e.getDataHoraFim();

			// determina se devemos comparar por barbeiro (preferível) ou por serviço
			boolean sameResource;
			if (agendamento.getBarbeiroId() != null && e.getBarbeiroId() != null) {
				sameResource = e.getBarbeiroId().equals(agendamento.getBarbeiroId());
			} else {
				sameResource = e.getServicoId() != null && agendamento.getServicoId() != null
					&& e.getServicoId().equals(agendamento.getServicoId());
			}

			boolean deveIgnorar = !sameResource
					|| e.getStatus() == StatusAgendamento.RECUSADO
					|| s1 == null || e1 == null || s2 == null || e2 == null;

			if (deveIgnorar) {
				continue;
			}

			boolean overlap = s1.isBefore(e2) && s2.isBefore(e1);
			if (overlap) {
				throw new IllegalArgumentException("Conflito de horário com outro agendamento existente.");
			}
		}
	}

	@Transactional
	public Agendamento atualizarStatus(UUID id, StatusAgendamento novoStatus) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		ValidationUtils.validarCampoObrigatorio(novoStatus, "status");

		Agendamento ag = agendamentoRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
		ag.setStatus(novoStatus);
		return agendamentoRepository.save(ag);
	}

	@Transactional
	public void deletar(UUID id) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		agendamentoRepository.deleteById(id);
	}

}
