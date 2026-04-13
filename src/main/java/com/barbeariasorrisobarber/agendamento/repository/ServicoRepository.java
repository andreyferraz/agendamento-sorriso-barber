package com.barbeariasorrisobarber.agendamento.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.barbeariasorrisobarber.agendamento.model.Servico;

public interface ServicoRepository extends CrudRepository<Servico, UUID> {

}
