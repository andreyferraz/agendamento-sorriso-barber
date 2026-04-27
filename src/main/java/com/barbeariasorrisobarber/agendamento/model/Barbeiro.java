package com.barbeariasorrisobarber.agendamento.model;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("barbeiro")
public class Barbeiro {

    @Id
    private UUID id;

    private String nome;

    private String telefone;

    private String email;

    @Column("senha_hash")
    private String senhaHash;

    @Column("comissao_percentual")
    private BigDecimal comissaoPercentual;

}
