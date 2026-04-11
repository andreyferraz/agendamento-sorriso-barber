package com.barbeariasorrisobarber.agendamento.model;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("servico")
public class Servico {

    @Id
    private UUID id;

    private String nome;

    private String descricao;

    private BigDecimal preco;

    private Integer duracao;

}
