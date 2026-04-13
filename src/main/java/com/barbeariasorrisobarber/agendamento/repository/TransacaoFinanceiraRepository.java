package com.barbeariasorrisobarber.agendamento.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.barbeariasorrisobarber.agendamento.model.TransacaoFinanceira;

public interface TransacaoFinanceiraRepository extends CrudRepository<TransacaoFinanceira, UUID> {

}
