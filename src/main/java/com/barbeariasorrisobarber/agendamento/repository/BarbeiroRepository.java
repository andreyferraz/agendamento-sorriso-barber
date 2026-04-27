package com.barbeariasorrisobarber.agendamento.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.barbeariasorrisobarber.agendamento.model.Barbeiro;

public interface BarbeiroRepository extends CrudRepository<Barbeiro, UUID> {

}
