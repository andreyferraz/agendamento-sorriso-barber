package com.barbeariasorrisobarber.agendamento.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.barbeariasorrisobarber.agendamento.model.Agendamento;

public interface AgendamentoRepository extends CrudRepository<Agendamento, UUID> {

}
