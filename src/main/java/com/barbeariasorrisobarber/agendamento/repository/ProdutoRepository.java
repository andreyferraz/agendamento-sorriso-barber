package com.barbeariasorrisobarber.agendamento.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.barbeariasorrisobarber.agendamento.model.Produto;

public interface ProdutoRepository extends CrudRepository<Produto, UUID> {

}
