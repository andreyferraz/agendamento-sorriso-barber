package com.barbeariasorrisobarber.agendamento.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barbeariasorrisobarber.agendamento.enuns.StatusAgendamento;
import com.barbeariasorrisobarber.agendamento.enuns.TipoEntrada;
import com.barbeariasorrisobarber.agendamento.model.Agendamento;
import com.barbeariasorrisobarber.agendamento.model.Barbeiro;
import com.barbeariasorrisobarber.agendamento.model.Servico;
import com.barbeariasorrisobarber.agendamento.model.TransacaoFinanceira;
import com.barbeariasorrisobarber.agendamento.repository.AgendamentoRepository;
import com.barbeariasorrisobarber.agendamento.repository.ServicoRepository;
import com.barbeariasorrisobarber.agendamento.utils.ValidationUtils;

@Service
public class AgendamentoService {

	private static final String AGENDAMENTO_NAO_ENCONTRADO = "Agendamento não encontrado.";

	private final AgendamentoRepository agendamentoRepository;
	private final ServicoRepository servicoRepository;
	private final BarbeiroService barbeiroService;
	private final TransacaoFinanceiraService transacaoFinanceiraService;

	public AgendamentoService(AgendamentoRepository agendamentoRepository, ServicoRepository servicoRepository,
			BarbeiroService barbeiroService, TransacaoFinanceiraService transacaoFinanceiraService) {
		this.agendamentoRepository = agendamentoRepository;
		this.servicoRepository = servicoRepository;
		this.barbeiroService = barbeiroService;
		this.transacaoFinanceiraService = transacaoFinanceiraService;
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
		agendamento.setNew(true);

		return agendamentoRepository.save(agendamento);
	}

	@Transactional
	public Agendamento atualizarAgendamento(UUID id, Agendamento dados) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		validarCamposBasicos(dados);
		calcularDataHoraFimSeAusente(dados);

		Agendamento existente = agendamentoRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException(AGENDAMENTO_NAO_ENCONTRADO));

		existente.setNomeCliente(dados.getNomeCliente());
		existente.setTelefoneCliente(dados.getTelefoneCliente());
		existente.setBarbeiroId(dados.getBarbeiroId());
		existente.setServicoId(dados.getServicoId());
		existente.setDataHoraInicio(dados.getDataHoraInicio());
		existente.setDataHoraFim(dados.getDataHoraFim());
		if (dados.getStatus() != null) {
			existente.setStatus(dados.getStatus());
		}
		existente.setGoogleEventId(dados.getGoogleEventId());
		existente.setNew(false);

		checarConflitosHorario(existente);

		return agendamentoRepository.save(existente);
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
			if (!isMesmoAgendamento(agendamento, e)
					&& deveCompararConflito(agendamento, e)
					&& temSobreposicao(agendamento, e)) {
				throw new IllegalArgumentException("Conflito de horário com outro agendamento existente.");
			}
		}
	}

	private boolean isMesmoAgendamento(Agendamento agendamento, Agendamento existente) {
		return Objects.equals(agendamento.getId(), existente.getId()) && agendamento.getId() != null;
	}

	private boolean deveCompararConflito(Agendamento agendamento, Agendamento existente) {
		boolean sameResource;
		if (agendamento.getBarbeiroId() != null && existente.getBarbeiroId() != null) {
			sameResource = existente.getBarbeiroId().equals(agendamento.getBarbeiroId());
		} else {
			sameResource = existente.getServicoId() != null && agendamento.getServicoId() != null
				&& existente.getServicoId().equals(agendamento.getServicoId());
		}

		return sameResource
				&& existente.getStatus() != StatusAgendamento.RECUSADO
				&& agendamento.getDataHoraInicio() != null
				&& agendamento.getDataHoraFim() != null
				&& existente.getDataHoraInicio() != null
				&& existente.getDataHoraFim() != null;
	}

	private boolean temSobreposicao(Agendamento agendamento, Agendamento existente) {
		LocalDateTime s1 = agendamento.getDataHoraInicio();
		LocalDateTime e1 = agendamento.getDataHoraFim();
		LocalDateTime s2 = existente.getDataHoraInicio();
		LocalDateTime e2 = existente.getDataHoraFim();
		return s1.isBefore(e2) && s2.isBefore(e1);
	}

	@Transactional
	public Agendamento atualizarStatus(UUID id, StatusAgendamento novoStatus) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		ValidationUtils.validarCampoObrigatorio(novoStatus, "status");

		if (novoStatus == StatusAgendamento.PAGO) {
			return marcarComoPago(id);
		}

		Agendamento ag = agendamentoRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException(AGENDAMENTO_NAO_ENCONTRADO));
		ag.setStatus(novoStatus);
		return agendamentoRepository.save(ag);
	}

	public Agendamento marcarComoPago(UUID id) {
		ValidationUtils.validarCampoObrigatorio(id, "id");

		Agendamento agendamento = agendamentoRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException(AGENDAMENTO_NAO_ENCONTRADO));

		if (agendamento.getStatus() == StatusAgendamento.PAGO) {
			return agendamento;
		}

		if (agendamento.getStatus() != StatusAgendamento.ACEITO) {
			throw new IllegalArgumentException("Somente agendamentos aceitos podem ser marcados como pagos.");
		}

		Servico servico = servicoRepository.findById(agendamento.getServicoId())
				.orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado para o agendamento."));
		Barbeiro barbeiro = barbeiroService.buscarPorId(agendamento.getBarbeiroId())
				.orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado para o agendamento."));

		BigDecimal precoServico = servico.getPreco();
		if (precoServico == null) {
			throw new IllegalArgumentException("Não foi possível registrar a entrada do agendamento.");
		}

		TransacaoFinanceira transacao = new TransacaoFinanceira();
		transacao.setTipo(TipoEntrada.ENTRADA);
		transacao.setValor(precoServico.setScale(2, RoundingMode.HALF_UP));
		transacao.setDescricao("Pagamento do agendamento " + agendamento.getId());
		transacao.setAgendamentoId(agendamento.getId());
		transacao.setBarbeiroId(barbeiro.getId());
		transacaoFinanceiraService.criarTransacao(transacao);

		agendamento.setStatus(StatusAgendamento.PAGO);
		return agendamentoRepository.save(agendamento);
	}

	@Transactional
	public void deletar(UUID id) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		agendamentoRepository.deleteById(id);
	}

}
