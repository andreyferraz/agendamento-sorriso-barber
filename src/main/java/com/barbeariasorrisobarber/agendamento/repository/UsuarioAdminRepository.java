package com.barbeariasorrisobarber.agendamento.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.barbeariasorrisobarber.agendamento.model.UsuarioAdmin;

public interface UsuarioAdminRepository extends CrudRepository<UsuarioAdmin, UUID> {

}
