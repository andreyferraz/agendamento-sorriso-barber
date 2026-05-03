package com.barbeariasorrisobarber.agendamento.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.barbeariasorrisobarber.agendamento.enuns.StatusAgendamento;
import com.barbeariasorrisobarber.agendamento.model.Agendamento;
import com.barbeariasorrisobarber.agendamento.model.Barbeiro;
import com.barbeariasorrisobarber.agendamento.model.Servico;
import com.barbeariasorrisobarber.agendamento.service.AgendamentoService;
import com.barbeariasorrisobarber.agendamento.service.BarbeiroService;
import com.barbeariasorrisobarber.agendamento.service.ServicoService;

@Controller
public class BarbeiroController {

    private static final String VIEW_BARBEIRO = "barbeiro/dashboard";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final BarbeiroService barbeiroService;
    private final AgendamentoService agendamentoService;
    private final ServicoService servicoService;

    public BarbeiroController(BarbeiroService barbeiroService, AgendamentoService agendamentoService,
            ServicoService servicoService) {
        this.barbeiroService = barbeiroService;
        this.agendamentoService = agendamentoService;
        this.servicoService = servicoService;
    }

    @GetMapping("/barbeiro")
    public String dashboard(Model model) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/login";
        }

        Barbeiro barbeiro = barbeiroService.buscarPorUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Barbeiro não encontrado."));

        Map<UUID, String> servicoNomes = servicoService.listarTodos().stream()
                .collect(Collectors.toMap(Servico::getId,
                        servico -> servico.getNome() != null ? servico.getNome() : "Serviço",
                        (primeiro, segundo) -> primeiro));

        var agendamentos = agendamentoService.listarTodos().stream()
                .filter(agendamento -> barbeiro.getId() != null && barbeiro.getId().equals(agendamento.getBarbeiroId()))
                .sorted(Comparator.comparing(Agendamento::getDataHoraInicio, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(agendamento -> new AgendamentoBarbeiroView(
                        agendamento.getId(),
                        formatarData(agendamento),
                        formatarHora(agendamento),
                        agendamento.getNomeCliente(),
                        servicoNomes.getOrDefault(agendamento.getServicoId(), "Serviço"),
                        agendamento.getStatus() != null ? agendamento.getStatus().name() : StatusAgendamento.PENDENTE.name()))
                .toList();

        model.addAttribute("barbeiro", barbeiro);
        model.addAttribute("agendamentos", agendamentos);
        return VIEW_BARBEIRO;
    }

    private String formatarData(Agendamento agendamento) {
        LocalDateTime inicio = agendamento.getDataHoraInicio();
        return inicio != null ? inicio.toLocalDate().format(DATE_FORMAT) : "-";
    }

    private String formatarHora(Agendamento agendamento) {
        LocalDateTime inicio = agendamento.getDataHoraInicio();
        return inicio != null ? inicio.toLocalTime().format(TIME_FORMAT) : "-";
    }

    private record AgendamentoBarbeiroView(UUID id, String data, String hora, String cliente, String servico, String status) {
    }
}
