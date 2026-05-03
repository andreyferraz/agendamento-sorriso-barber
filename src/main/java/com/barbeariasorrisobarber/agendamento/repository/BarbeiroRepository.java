package com.barbeariasorrisobarber.agendamento.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.barbeariasorrisobarber.agendamento.model.Barbeiro;

public interface BarbeiroRepository extends CrudRepository<Barbeiro, UUID> {

	Optional<Barbeiro> findByUsername(String username);

	List<Barbeiro> findByUsernameNot(String username);

}
