package com.barbeariasorrisobarber.agendamento.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
