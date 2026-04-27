package com.barbeariasorrisobarber.agendamento.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.barbeariasorrisobarber.agendamento.enuns.TipoEntrada;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("transacao_financeira")
public class TransacaoFinanceira {

    @Id
    private UUID id;

    private TipoEntrada tipo;

    private BigDecimal valor;

    private LocalDateTime data;

    private String descricao;

    @Column("agendamento_id")
    private UUID agendamentoId;

    @Column("barbeiro_id")
    private UUID barbeiroId;

}
