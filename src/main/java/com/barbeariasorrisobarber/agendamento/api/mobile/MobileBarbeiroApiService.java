package com.barbeariasorrisobarber.agendamento.api.mobile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.barbeariasorrisobarber.agendamento.enuns.StatusAgendamento;
import com.barbeariasorrisobarber.agendamento.enuns.TipoEntrada;
import com.barbeariasorrisobarber.agendamento.model.Agendamento;
import com.barbeariasorrisobarber.agendamento.model.Barbeiro;
import com.barbeariasorrisobarber.agendamento.model.Servico;
import com.barbeariasorrisobarber.agendamento.model.TransacaoFinanceira;
import com.barbeariasorrisobarber.agendamento.service.AgendamentoService;
import com.barbeariasorrisobarber.agendamento.service.BarbeiroService;
import com.barbeariasorrisobarber.agendamento.service.ServicoService;
import com.barbeariasorrisobarber.agendamento.service.TransacaoFinanceiraService;

@Service
public class MobileBarbeiroApiService {

    private static final String BARBEIRO_PADRAO = "Barbeiro";

    private final BarbeiroService barbeiroService;
    private final AgendamentoService agendamentoService;
    private final ServicoService servicoService;
    private final TransacaoFinanceiraService transacaoFinanceiraService;

    public MobileBarbeiroApiService(BarbeiroService barbeiroService, AgendamentoService agendamentoService,
            ServicoService servicoService, TransacaoFinanceiraService transacaoFinanceiraService) {
        this.barbeiroService = barbeiroService;
        this.agendamentoService = agendamentoService;
        this.servicoService = servicoService;
        this.transacaoFinanceiraService = transacaoFinanceiraService;
    }

    public MobileBarbeiroResponse obterPerfil(String username) {
        Barbeiro barbeiro = carregarBarbeiro(username);
        return toBarbeiroResponse(barbeiro);
    }

    public List<MobileAgendamentoResponse> listarAgendamentos(String username, StatusAgendamento status) {
        Barbeiro barbeiro = carregarBarbeiro(username);
        Map<UUID, String> servicoNomes = montarMapaServicos();

        return agendamentoService.listarTodos().stream()
                .filter(agendamento -> pertenceAoBarbeiro(agendamento, barbeiro.getId()))
                .filter(agendamento -> status == null || agendamento.getStatus() == status)
                .sorted(Comparator.comparing(Agendamento::getDataHoraInicio, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(agendamento -> toAgendamentoResponse(agendamento, barbeiro, servicoNomes))
                .toList();
    }

    public List<MobileAgendamentoResponse> listarPendentes(String username) {
        return listarAgendamentos(username, StatusAgendamento.PENDENTE);
    }

    public MobileComissaoResumoResponse obterComissao(String username) {
        Barbeiro barbeiro = carregarBarbeiro(username);
        UUID barbeiroId = barbeiro.getId();
        if (barbeiroId == null) {
            return new MobileComissaoResumoResponse(null, safeText(barbeiro.getNome(), BARBEIRO_PADRAO),
                    barbeiro.getComissaoPercentual() != null ? barbeiro.getComissaoPercentual() : BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal gerado = calcularComissaoGerada(barbeiro);
        BigDecimal pago = calcularComissaoPaga(barbeiroId);
        BigDecimal pendente = gerado.subtract(pago);
        if (pendente.signum() < 0) {
            pendente = BigDecimal.ZERO;
        }

        return new MobileComissaoResumoResponse(barbeiro.getId(), safeText(barbeiro.getNome(), BARBEIRO_PADRAO),
                barbeiro.getComissaoPercentual() != null ? barbeiro.getComissaoPercentual() : BigDecimal.ZERO,
                gerado, pago, pendente);
    }

    public MobileDashboardResponse obterDashboard(String username) {
        Barbeiro barbeiro = carregarBarbeiro(username);
        MobileBarbeiroResponse perfil = toBarbeiroResponse(barbeiro);
        List<MobileAgendamentoResponse> agendamentos = listarAgendamentos(username, null);
        List<MobileAgendamentoResponse> pendentes = agendamentos.stream()
                .filter(agendamento -> agendamento.status() == StatusAgendamento.PENDENTE)
                .toList();
        long aceitos = agendamentos.stream().filter(agendamento -> agendamento.status() == StatusAgendamento.ACEITO)
                .count();
        long pagos = agendamentos.stream().filter(agendamento -> agendamento.status() == StatusAgendamento.PAGO)
                .count();

        return new MobileDashboardResponse(perfil, agendamentos.size(), pendentes.size(), aceitos, pagos, pendentes,
                obterComissao(username));
    }

    private Barbeiro carregarBarbeiro(String username) {
        return barbeiroService.buscarPorUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado."));
    }

    private boolean pertenceAoBarbeiro(Agendamento agendamento, UUID barbeiroId) {
        return agendamento != null && barbeiroId != null && barbeiroId.equals(agendamento.getBarbeiroId());
    }

    private MobileBarbeiroResponse toBarbeiroResponse(Barbeiro barbeiro) {
        return new MobileBarbeiroResponse(barbeiro.getId(), safeText(barbeiro.getNome(), BARBEIRO_PADRAO),
                safeText(barbeiro.getUsername(), ""), barbeiro.getTelefone(), barbeiro.getEmail(), barbeiro.getFotoUrl(),
                barbeiro.getComissaoPercentual() != null ? barbeiro.getComissaoPercentual() : BigDecimal.ZERO);
    }

    private MobileAgendamentoResponse toAgendamentoResponse(Agendamento agendamento, Barbeiro barbeiro,
            Map<UUID, String> servicoNomes) {
        return new MobileAgendamentoResponse(agendamento.getId(), agendamento.getDataHoraInicio(),
                agendamento.getDataHoraFim(), agendamento.getNomeCliente(), agendamento.getTelefoneCliente(),
                agendamento.getServicoId(), servicoNomes.getOrDefault(agendamento.getServicoId(), "Serviço"),
                barbeiro.getId(), safeText(barbeiro.getNome(), BARBEIRO_PADRAO),
                agendamento.getStatus() != null ? agendamento.getStatus() : StatusAgendamento.PENDENTE);
    }

    private Map<UUID, String> montarMapaServicos() {
        return servicoService.listarTodos().stream()
                .filter(servico -> servico != null && servico.getId() != null)
                .collect(Collectors.toMap(Servico::getId,
                        servico -> safeText(servico.getNome(), "Serviço"),
                        (primeiro, segundo) -> primeiro, LinkedHashMap::new));
    }

    private BigDecimal calcularComissaoGerada(Barbeiro barbeiro) {
        Map<UUID, Servico> servicosPorId = servicoService.listarTodos().stream()
                .filter(servico -> servico != null && servico.getId() != null)
                .collect(Collectors.toMap(Servico::getId, servico -> servico, (primeiro, segundo) -> primeiro,
                        LinkedHashMap::new));

        BigDecimal percentual = barbeiro.getComissaoPercentual() != null ? barbeiro.getComissaoPercentual()
                : BigDecimal.ZERO;

        return agendamentoService.listarTodos().stream()
                .filter(agendamento -> pertenceAoBarbeiro(agendamento, barbeiro.getId()))
                .filter(agendamento -> agendamento.getStatus() == StatusAgendamento.PAGO)
                .map(agendamento -> {
                    Servico servico = servicosPorId.get(agendamento.getServicoId());
                    if (servico == null || servico.getPreco() == null || percentual.signum() <= 0) {
                        return BigDecimal.ZERO;
                    }

                    return servico.getPreco().multiply(percentual)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularComissaoPaga(UUID barbeiroId) {
        BigDecimal totalPago = BigDecimal.ZERO;
        for (TransacaoFinanceira transacao : transacaoFinanceiraService.listarTodos()) {
            if (transacao == null || transacao.getBarbeiroId() == null || transacao.getTipo() == TipoEntrada.ENTRADA
                    || !barbeiroId.equals(transacao.getBarbeiroId())) {
                continue;
            }

            totalPago = totalPago.add(safeValor(transacao.getValor()));
        }
        return totalPago;
    }

    private BigDecimal safeValor(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    private String safeText(String valor, String fallback) {
        return valor != null && !valor.isBlank() ? valor : fallback;
    }
}
