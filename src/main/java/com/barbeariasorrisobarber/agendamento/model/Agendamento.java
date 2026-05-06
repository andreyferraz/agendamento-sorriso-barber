package com.barbeariasorrisobarber.agendamento.model;

import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.barbeariasorrisobarber.agendamento.enuns.StatusAgendamento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("agendamento")
public class Agendamento implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("nome_cliente")
    private String nomeCliente;

    @Column("telefone_cliente")
    private String telefoneCliente;

    @Column("servico_id")
    private UUID servicoId;

    @Column("barbeiro_id")
    private UUID barbeiroId;

    @Column("data_hora_inicio")
    private LocalDateTime dataHoraInicio;

    @Column("data_hora_fim")
    private LocalDateTime dataHoraFim;

    @Column("status")
    private StatusAgendamento status;

    @Column("google_event_id")
    private String googleEventId;

    @Transient 
    private boolean isNew = false;

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return isNew; }
    
}
