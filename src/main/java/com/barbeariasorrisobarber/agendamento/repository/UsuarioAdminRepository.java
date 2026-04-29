package com.barbeariasorrisobarber.agendamento.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.barbeariasorrisobarber.agendamento.model.UsuarioAdmin;

public interface UsuarioAdminRepository extends CrudRepository<UsuarioAdmin, UUID> {

	List<UsuarioAdmin> findByUsernameNot(String username);

	Optional<UsuarioAdmin> findByUsername(String username);

}
