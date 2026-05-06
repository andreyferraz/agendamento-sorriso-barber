package com.barbeariasorrisobarber.agendamento.model;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("barbeiro")
public class Barbeiro implements Persistable<UUID> {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Id
    private UUID id;

    private String nome;

    private String username;

    private String telefone;

    private String email;

    @Column("foto_url")
    private String fotoUrl;

    @Column("senha_hash")
    private String senhaHash;

    @Column("comissao_percentual")
    private BigDecimal comissaoPercentual;

    @Column("horario_inicio_atendimento")
    private String horarioInicioAtendimento;

    @Column("horario_fim_atendimento")
    private String horarioFimAtendimento;

    @Column("horarios_segunda")
    private String horariosSegunda;

    @Column("horarios_terca")
    private String horariosTerca;

    @Column("horarios_quarta")
    private String horariosQuarta;

    @Column("horarios_quinta")
    private String horariosQuinta;

    @Column("horarios_sexta")
    private String horariosSexta;

    @Column("horarios_sabado")
    private String horariosSabado;

    @Column("horarios_domingo")
    private String horariosDomingo;

    @Column("horario_segunda_inicio")
    private String horarioSegundaInicio;

    @Column("horario_segunda_fim")
    private String horarioSegundaFim;

    @Column("horario_terca_inicio")
    private String horarioTercaInicio;

    @Column("horario_terca_fim")
    private String horarioTercaFim;

    @Column("horario_quarta_inicio")
    private String horarioQuartaInicio;

    @Column("horario_quarta_fim")
    private String horarioQuartaFim;

    @Column("horario_quinta_inicio")
    private String horarioQuintaInicio;

    @Column("horario_quinta_fim")
    private String horarioQuintaFim;

    @Column("horario_sexta_inicio")
    private String horarioSextaInicio;

    @Column("horario_sexta_fim")
    private String horarioSextaFim;

    @Column("horario_sabado_inicio")
    private String horarioSabadoInicio;

    @Column("horario_sabado_fim")
    private String horarioSabadoFim;

    @Column("horario_domingo_inicio")
    private String horarioDomingoInicio;

    @Column("horario_domingo_fim")
    private String horarioDomingoFim;

    @Transient
    private boolean isNew = false;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public String getHorarioInicioAtendimentoFormatado() {
        return formatarHorario(horarioInicioAtendimento);
    }

    public String getHorarioFimAtendimentoFormatado() {
        return formatarHorario(horarioFimAtendimento);
    }

    public String getHorarioSegundaInicioFormatado() {
        return formatarHorario(horarioSegundaInicio);
    }

    public String getHorarioSegundaFimFormatado() {
        return formatarHorario(horarioSegundaFim);
    }

    public String getHorarioTercaInicioFormatado() {
        return formatarHorario(horarioTercaInicio);
    }

    public String getHorarioTercaFimFormatado() {
        return formatarHorario(horarioTercaFim);
    }

    public String getHorarioQuartaInicioFormatado() {
        return formatarHorario(horarioQuartaInicio);
    }

    public String getHorarioQuartaFimFormatado() {
        return formatarHorario(horarioQuartaFim);
    }

    public String getHorarioQuintaInicioFormatado() {
        return formatarHorario(horarioQuintaInicio);
    }

    public String getHorarioQuintaFimFormatado() {
        return formatarHorario(horarioQuintaFim);
    }

    public String getHorarioSextaInicioFormatado() {
        return formatarHorario(horarioSextaInicio);
    }

    public String getHorarioSextaFimFormatado() {
        return formatarHorario(horarioSextaFim);
    }

    public String getHorarioSabadoInicioFormatado() {
        return formatarHorario(horarioSabadoInicio);
    }

    public String getHorarioSabadoFimFormatado() {
        return formatarHorario(horarioSabadoFim);
    }

    public String getHorarioDomingoInicioFormatado() {
        return formatarHorario(horarioDomingoInicio);
    }

    public String getHorarioDomingoFimFormatado() {
        return formatarHorario(horarioDomingoFim);
    }

    public boolean possuiHorarioSemanalConfigurado() {
        return temValor(horariosSegunda)
                || temValor(horariosTerca)
                || temValor(horariosQuarta)
                || temValor(horariosQuinta)
                || temValor(horariosSexta)
                || temValor(horariosSabado)
                || temValor(horariosDomingo);
    }

    public String getHorarioInicioAtendimentoEfetivo() {
        return horarioInicioAtendimento;
    }

    public String getHorarioFimAtendimentoEfetivo() {
        return horarioFimAtendimento;
    }

    public List<LocalTime> getHorariosDoDia(DayOfWeek diaDaSemana) {
        return parseHorarios(getHorariosDoDiaRaw(diaDaSemana));
    }

    public String getHorarioResumo() {
        if (possuiHorarioSemanalConfigurado()) {
            List<String> partes = new ArrayList<>();
            adicionarResumo(partes, "Seg", horariosSegunda);
            adicionarResumo(partes, "Ter", horariosTerca);
            adicionarResumo(partes, "Qua", horariosQuarta);
            adicionarResumo(partes, "Qui", horariosQuinta);
            adicionarResumo(partes, "Sex", horariosSexta);
            adicionarResumo(partes, "Sáb", horariosSabado);
            adicionarResumo(partes, "Dom", horariosDomingo);

            if (!partes.isEmpty()) {
                return String.join(" | ", partes);
            }
        }

        if (temValor(horarioInicioAtendimento) && temValor(horarioFimAtendimento)) {
            return "Atende de " + formatarHorario(horarioInicioAtendimento) + " às " + formatarHorario(horarioFimAtendimento);
        }

        return "Horário não informado";
    }

    private String getHorariosDoDiaRaw(DayOfWeek diaDaSemana) {
        return switch (diaDaSemana) {
            case MONDAY -> horariosSegunda;
            case TUESDAY -> horariosTerca;
            case WEDNESDAY -> horariosQuarta;
            case THURSDAY -> horariosQuinta;
            case FRIDAY -> horariosSexta;
            case SATURDAY -> horariosSabado;
            case SUNDAY -> horariosDomingo;
        };
    }

    private void adicionarResumo(List<String> partes, String dia, String horarios) {
        List<LocalTime> horariosDoDia = parseHorarios(horarios);
        if (!horariosDoDia.isEmpty()) {
            partes.add(dia + ": " + horariosDoDia.stream().map(horario -> horario.format(TIME_FORMAT)).collect(java.util.stream.Collectors.joining(", ")));
        }
    }

    private List<LocalTime> parseHorarios(String valor) {
        List<LocalTime> horarios = new ArrayList<>();
        if (valor == null || valor.isBlank()) {
            return horarios;
        }

        for (String item : valor.split("[\\n,;]+")) {
            String token = item == null ? "" : item.trim();
            if (token.isEmpty()) {
                continue;
            }

            try {
                horarios.add(LocalTime.parse(token).withSecond(0).withNano(0));
            } catch (Exception ex) {
                try {
                    horarios.add(LocalTime.parse(token, TIME_FORMAT));
                } catch (Exception ignored) {
                    // ignore invalid tokens
                }
            }
        }

        return horarios.stream().distinct().sorted().toList();
    }

    private boolean temValor(String valor) {
        return valor != null && !valor.isBlank();
    }

    private String formatarHorario(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }

        String trimmed = valor.trim();
        if (trimmed.matches("\\d{2}:\\d{2}")) {
            return trimmed;
        }

        try {
            long epochMillis = Long.parseLong(trimmed);
            return Instant.ofEpochMilli(epochMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime()
                    .format(TIME_FORMAT);
        } catch (NumberFormatException ex) {
            try {
                return LocalTime.parse(trimmed).format(TIME_FORMAT);
            } catch (Exception ignored) {
                return trimmed;
            }
        }
    }

}
