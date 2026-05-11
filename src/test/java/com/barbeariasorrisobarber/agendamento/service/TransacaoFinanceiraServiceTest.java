package com.barbeariasorrisobarber.agendamento.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.barbeariasorrisobarber.agendamento.enuns.TipoEntrada;
import com.barbeariasorrisobarber.agendamento.model.TransacaoFinanceira;
import com.barbeariasorrisobarber.agendamento.repository.TransacaoFinanceiraRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TransacaoFinanceiraServiceTest {

    @Mock
    private TransacaoFinanceiraRepository repository;

    @InjectMocks
    private TransacaoFinanceiraService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private TransacaoFinanceira criarTransacao(UUID id, TipoEntrada tipo, BigDecimal valor, LocalDateTime data,
            String descricao, UUID agendamentoId, UUID barbeiroId, boolean isNew) {
        TransacaoFinanceira transacao = new TransacaoFinanceira();
        transacao.setId(id);
        transacao.setTipo(tipo);
        transacao.setValor(valor);
        transacao.setData(data);
        transacao.setDescricao(descricao);
        transacao.setAgendamentoId(agendamentoId);
        transacao.setBarbeiroId(barbeiroId);
        transacao.setNew(isNew);
        return transacao;
    }

    @Test
    void listarTodos_deveRetornarLista() {
        TransacaoFinanceira t = criarTransacao(UUID.randomUUID(), TipoEntrada.ENTRADA,
                BigDecimal.valueOf(100), LocalDateTime.now(), "desc", null, null, false);
        when(repository.findAll()).thenReturn(List.of(t));

        var lista = service.listarTodos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals(TipoEntrada.ENTRADA, lista.get(0).getTipo());
    }

    @Test
    void buscarPorId_deveRetornarOptionalQuandoExistir() {
        UUID id = UUID.randomUUID();
        TransacaoFinanceira t = criarTransacao(id, TipoEntrada.SAIDA, BigDecimal.valueOf(50),
                LocalDateTime.now(), "x", null, null, false);
        when(repository.findById(id)).thenReturn(Optional.of(t));

        var opt = service.buscarPorId(id);

        assertTrue(opt.isPresent());
        assertEquals(id, opt.get().getId());
    }

    @Test
    void buscarPorId_nullId_deveLancar() {
        assertThrows(IllegalArgumentException.class, () -> service.buscarPorId(null));
    }

    @Test
    void criarTransacao_devePreencherDataEIdQuandoAusente() {
        TransacaoFinanceira t = criarTransacao(null, TipoEntrada.ENTRADA, BigDecimal.valueOf(20), null,
                "d", null, null, false);
        final boolean[] isNewNoSave = new boolean[1];
        when(repository.save(any())).thenAnswer(i -> {
            TransacaoFinanceira transacaoNoSave = i.getArgument(0);
            isNewNoSave[0] = transacaoNoSave.isNew();
            return transacaoNoSave;
        });

        TransacaoFinanceira salvo = service.criarTransacao(t);

        assertNotNull(salvo.getId());
        assertNotNull(salvo.getData());
        assertEquals(BigDecimal.valueOf(20), salvo.getValor());
        assertFalse(salvo.isNew());
        assertTrue(isNewNoSave[0]);
        verify(repository).save(any(TransacaoFinanceira.class));
    }

    @Test
    void criarTransacao_transacaoNull_deveLancar() {
        assertThrows(IllegalArgumentException.class, () -> service.criarTransacao(null));
    }

    @Test
    void criarTransacao_valorZeroOuNegativo_deveLancar() {
        TransacaoFinanceira zero = criarTransacao(null, TipoEntrada.ENTRADA, BigDecimal.ZERO, null,
                null, null, null, false);
        TransacaoFinanceira neg = criarTransacao(null, TipoEntrada.ENTRADA, BigDecimal.valueOf(-1), null,
                null, null, null, false);

        assertThrows(IllegalArgumentException.class, () -> service.criarTransacao(zero));
        assertThrows(IllegalArgumentException.class, () -> service.criarTransacao(neg));
    }

    @Test
    void atualizarTransacao_deveAtualizarCampos() {
        UUID id = UUID.randomUUID();
        TransacaoFinanceira existente = criarTransacao(id, TipoEntrada.ENTRADA, BigDecimal.valueOf(10),
                LocalDateTime.now(), "old", null, null, false);
        TransacaoFinanceira dados = criarTransacao(null, TipoEntrada.SAIDA, BigDecimal.valueOf(30),
                LocalDateTime.of(2026, 4, 13, 12, 0), "new", UUID.randomUUID(), null, false);

        when(repository.findById(id)).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        TransacaoFinanceira atualizado = service.atualizarTransacao(id, dados);

        assertEquals(TipoEntrada.SAIDA, atualizado.getTipo());
        assertEquals(BigDecimal.valueOf(30), atualizado.getValor());
        assertEquals("new", atualizado.getDescricao());
        assertEquals(dados.getData(), atualizado.getData());
        assertEquals(dados.getAgendamentoId(), atualizado.getAgendamentoId());
        assertFalse(atualizado.isNew());
    }

    @Test
    void atualizarTransacao_valorInvalido_deveLancar() {
        UUID id = UUID.randomUUID();
        TransacaoFinanceira existente = criarTransacao(id, TipoEntrada.ENTRADA, BigDecimal.valueOf(10),
                LocalDateTime.now(), null, null, null, false);
        TransacaoFinanceira dados = criarTransacao(null, null, BigDecimal.ZERO, null, null, null, null, false);

        when(repository.findById(id)).thenReturn(Optional.of(existente));

        assertThrows(IllegalArgumentException.class, () -> service.atualizarTransacao(id, dados));
    }

    @Test
    void deletar_deveChamarRepository() {
        UUID id = UUID.randomUUID();
        doNothing().when(repository).deleteById(id);

        service.deletar(id);

        verify(repository).deleteById(id);
    }
}
