package com.barbeariasorrisobarber.agendamento.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.barbeariasorrisobarber.agendamento.enuns.StatusAgendamento;
import com.barbeariasorrisobarber.agendamento.model.Agendamento;
import com.barbeariasorrisobarber.agendamento.model.Barbeiro;
import com.barbeariasorrisobarber.agendamento.model.Servico;
import com.barbeariasorrisobarber.agendamento.service.AgendamentoService;
import com.barbeariasorrisobarber.agendamento.service.BarbeiroService;
import com.barbeariasorrisobarber.agendamento.service.ServicoService;

@Controller
public class AgendamentoPublicController {

    private static final String VIEW_AGENDAR = "agendar";
    private static final String ATTR_BARBEIROS = "barbeiros";
    private static final String ATTR_SERVICOS = "servicos";
    private static final String ATTR_DIAS = "diasDisponiveis";
    private static final String ATTR_BARBEIRO_SELECIONADO = "barbeiroSelecionado";
    private static final String ATTR_SERVICO_SELECIONADO = "servicoSelecionado";
    private static final String ATTR_BARBEIRO_SELECIONADO_ID = "barbeiroSelecionadoId";
    private static final String ATTR_SERVICO_SELECIONADO_ID = "servicoSelecionadoId";
    private static final String ATTR_DATA_SELECIONADA = "dataSelecionada";
    private static final String ATTR_HORA_SELECIONADA = "horaSelecionada";
    private static final String ATTR_MENSAGEM = "mensagemDisponibilidade";
    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DAY_TITLE_FORMAT = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", PT_BR);
    private static final int HORIZONTE_DIAS = 14;
    private static final int INTERVALO_MINUTOS = 30;
    private static final LocalTime HORARIO_PADRAO_INICIO = LocalTime.of(9, 0);
    private static final LocalTime HORARIO_PADRAO_FIM = LocalTime.of(18, 0);

    private final BarbeiroService barbeiroService;
    private final ServicoService servicoService;
    private final AgendamentoService agendamentoService;

    public AgendamentoPublicController(BarbeiroService barbeiroService, ServicoService servicoService,
            AgendamentoService agendamentoService) {
        this.barbeiroService = barbeiroService;
        this.servicoService = servicoService;
        this.agendamentoService = agendamentoService;
    }

    @GetMapping({"/agendar", "/agendar/"})
    public String agendar(@RequestParam(name = "barbeiroId", required = false) UUID barbeiroId,
            @RequestParam(name = "servicoId", required = false) UUID servicoId,
            @RequestParam(name = "data", required = false) String data,
            @RequestParam(name = "hora", required = false) String hora,
            Model model) {
        List<Barbeiro> barbeiros = barbeiroService.listarTodos().stream()
                .sorted(Comparator.comparing(Barbeiro::getNome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
        List<Servico> servicos = servicoService.listarTodos().stream()
                .sorted(Comparator.comparing(Servico::getNome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();

        Barbeiro barbeiroSelecionado = selecionarBarbeiro(barbeiros, barbeiroId);
        Servico servicoSelecionado = selecionarServico(servicos, servicoId);

        List<DayAvailabilityView> diasDisponiveis = barbeiroSelecionado != null && servicoSelecionado != null
                ? montarDisponibilidade(barbeiroSelecionado, servicoSelecionado)
                : List.of();

        model.addAttribute(ATTR_BARBEIROS, barbeiros);
        model.addAttribute(ATTR_SERVICOS, servicos);
        model.addAttribute(ATTR_BARBEIRO_SELECIONADO, barbeiroSelecionado);
        model.addAttribute(ATTR_SERVICO_SELECIONADO, servicoSelecionado);
        UUID barbeiroSelecionadoId = barbeiroSelecionado != null ? barbeiroSelecionado.getId() : null;
        UUID servicoSelecionadoId = servicoSelecionado != null ? servicoSelecionado.getId() : null;
        model.addAttribute(ATTR_BARBEIRO_SELECIONADO_ID, barbeiroSelecionadoId != null ? barbeiroSelecionadoId.toString() : null);
        model.addAttribute(ATTR_SERVICO_SELECIONADO_ID, servicoSelecionadoId != null ? servicoSelecionadoId.toString() : null);
        model.addAttribute(ATTR_DIAS, diasDisponiveis);
        model.addAttribute(ATTR_DATA_SELECIONADA, data);
        model.addAttribute(ATTR_HORA_SELECIONADA, hora);
        model.addAttribute(ATTR_MENSAGEM,
                barbeiroSelecionado != null && servicoSelecionado != null && diasDisponiveis.isEmpty()
                        ? "Não há horários disponíveis para essa combinação nos próximos dias."
                        : null);

        return VIEW_AGENDAR;
    }

    @PostMapping("/agendar/reservar")
    public String reservar(@RequestParam("nomeCliente") String nomeCliente,
            @RequestParam("telefoneCliente") String telefoneCliente,
            @RequestParam("barbeiroId") UUID barbeiroId,
            @RequestParam("servicoId") UUID servicoId,
            @RequestParam("dataAgendamento") String dataAgendamento,
            @RequestParam("horaAgendamento") String horaAgendamento,
            RedirectAttributes redirectAttributes) {
        try {
            Barbeiro barbeiro = barbeiroService.buscarPorId(barbeiroId)
                    .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado."));
            Servico servico = servicoService.buscarPorId(servicoId)
                    .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado."));

            LocalDate data = LocalDate.parse(dataAgendamento);
            LocalTime hora = LocalTime.parse(horaAgendamento, TIME_FORMAT);
            LocalDateTime inicio = LocalDateTime.of(data, hora);

            if (!slotDisponivel(barbeiro, servico, inicio)) {
                throw new IllegalArgumentException("Horário indisponível para a seleção informada.");
            }

            Agendamento agendamento = new Agendamento();
            agendamento.setNomeCliente(nomeCliente);
            agendamento.setTelefoneCliente(telefoneCliente);
            agendamento.setBarbeiroId(barbeiroId);
            agendamento.setServicoId(servicoId);
            agendamento.setDataHoraInicio(inicio);
            agendamento.setStatus(StatusAgendamento.PENDENTE);

            agendamentoService.criarAgendamento(agendamento);
            redirectAttributes.addFlashAttribute("success", "Agendamento solicitado com sucesso.");
            return "redirect:/agendar";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/agendar?barbeiroId=" + barbeiroId + "&servicoId=" + servicoId + "&data=" + dataAgendamento
                    + "&hora=" + horaAgendamento;
        }
    }

    private Barbeiro selecionarBarbeiro(List<Barbeiro> barbeiros, UUID barbeiroId) {
        if (barbeiroId == null) {
            return null;
        }

        return barbeiros.stream()
                .filter(barbeiro -> barbeiro.getId() != null && barbeiro.getId().equals(barbeiroId))
                .findFirst()
                .orElse(null);
    }

    private Servico selecionarServico(List<Servico> servicos, UUID servicoId) {
        if (servicoId == null) {
            return null;
        }

        return servicos.stream()
                .filter(servico -> servico.getId() != null && servico.getId().equals(servicoId))
                .findFirst()
                .orElse(null);
    }

    private List<DayAvailabilityView> montarDisponibilidade(Barbeiro barbeiro, Servico servico) {
        LocalTime horarioInicio = parseHorario(barbeiro.getHorarioInicioAtendimento(), HORARIO_PADRAO_INICIO);
        LocalTime horarioFim = parseHorario(barbeiro.getHorarioFimAtendimento(), HORARIO_PADRAO_FIM);
        int duracaoServico = servico.getDuracao() != null && servico.getDuracao() > 0 ? servico.getDuracao() : 30;

        List<Agendamento> agendamentosDoBarbeiro = agendamentoService.listarTodos().stream()
                .filter(agendamento -> barbeiro.getId() != null && barbeiro.getId().equals(agendamento.getBarbeiroId()))
                .filter(agendamento -> agendamento.getStatus() != StatusAgendamento.RECUSADO)
                .filter(agendamento -> agendamento.getDataHoraInicio() != null && agendamento.getDataHoraFim() != null)
                .toList();

        List<DayAvailabilityView> dias = new java.util.ArrayList<>();
        LocalDate hoje = LocalDate.now();
        LocalDateTime agora = LocalDateTime.now();

        for (int offset = 0; offset < HORIZONTE_DIAS; offset++) {
            LocalDate data = hoje.plusDays(offset);
            LocalDateTime janelaInicio = data.atTime(horarioInicio);
            LocalDateTime janelaFim = data.atTime(horarioFim);
            List<TimeSlotView> slots = new java.util.ArrayList<>();

            LocalDateTime cursor = janelaInicio;
            while (!cursor.plusMinutes(duracaoServico).isAfter(janelaFim)) {
                if ((cursor.isAfter(agora) || cursor.equals(agora)) && slotLivre(cursor, duracaoServico, agendamentosDoBarbeiro)) {
                    slots.add(new TimeSlotView(cursor.toLocalTime().format(TIME_FORMAT)));
                }
                cursor = cursor.plusMinutes(INTERVALO_MINUTOS);
            }

            if (!slots.isEmpty()) {
                dias.add(new DayAvailabilityView(
                        data.toString(),
                        formatarTituloDia(data),
                        formatarSubtituloDia(data),
                        slots));
            }
        }

        return dias;
    }

    private boolean slotLivre(LocalDateTime inicio, int duracaoServico, List<Agendamento> agendamentos) {
        LocalDateTime fim = inicio.plusMinutes(duracaoServico);
        return agendamentos.stream().noneMatch(agendamento -> {
            LocalDateTime existenteInicio = agendamento.getDataHoraInicio();
            LocalDateTime existenteFim = agendamento.getDataHoraFim();
            return existenteInicio.isBefore(fim) && inicio.isBefore(existenteFim);
        });
    }

    private boolean slotDisponivel(Barbeiro barbeiro, Servico servico, LocalDateTime inicio) {
        return montarDisponibilidade(barbeiro, servico).stream()
                .anyMatch(dia -> dia.data().equals(inicio.toLocalDate().toString())
                        && dia.slots().stream().anyMatch(slot -> slot.hora().equals(inicio.toLocalTime().format(TIME_FORMAT))));
    }

    private LocalTime parseHorario(String valor, LocalTime fallback) {
        if (valor == null || valor.isBlank()) {
            return fallback;
        }

        try {
            return LocalTime.parse(valor.trim(), TIME_FORMAT);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String formatarTituloDia(LocalDate data) {
        String texto = data.format(DAY_TITLE_FORMAT);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private String formatarSubtituloDia(LocalDate data) {
        return data.format(DATE_FORMAT);
    }

    private record DayAvailabilityView(String data, String titulo, String subtitulo, List<TimeSlotView> slots) {
    }

    private record TimeSlotView(String hora) {
    }
}
