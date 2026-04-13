package com.barbeariasorrisobarber.agendamento.model;

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
@Table("usuario_admin")
public class UsuarioAdmin {

    @Id
    private UUID id;

    private String username;

    @Column("senha_hash")
    private String senhaHash;
}
