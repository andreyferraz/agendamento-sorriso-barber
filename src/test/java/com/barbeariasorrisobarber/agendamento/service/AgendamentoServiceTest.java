package com.barbeariasorrisobarber.agendamento.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.barbeariasorrisobarber.agendamento.enuns.StatusAgendamento;
import com.barbeariasorrisobarber.agendamento.model.Agendamento;
import com.barbeariasorrisobarber.agendamento.model.Servico;
import com.barbeariasorrisobarber.agendamento.repository.AgendamentoRepository;
import com.barbeariasorrisobarber.agendamento.repository.ServicoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private AgendamentoService agendamentoService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listarTodos_deveRetornarLista() {
        Agendamento a = new Agendamento(UUID.randomUUID(), "Joao", "9999", UUID.randomUUID(),
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), StatusAgendamento.PENDENTE, null);
        when(agendamentoRepository.findAll()).thenReturn(List.of(a));

        var lista = agendamentoService.listarTodos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals(a.getNomeCliente(), lista.get(0).getNomeCliente());
    }

    @Test
    void buscarPorId_deveRetornarOptionalQuandoExistir() {
        UUID id = UUID.randomUUID();
        Agendamento a = new Agendamento(id, "Maria", "8888", UUID.randomUUID(),
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(45), StatusAgendamento.PENDENTE, null);
        when(agendamentoRepository.findById(id)).thenReturn(Optional.of(a));

        var opt = agendamentoService.buscarPorId(id);

        assertTrue(opt.isPresent());
        assertEquals("Maria", opt.get().getNomeCliente());
    }

    @Test
    void criarAgendamento_deveCalcularDataHoraFimQuandoAusenteESetarStatusEId() {
        UUID servicoId = UUID.randomUUID();
        Servico servico = new Servico(servicoId, "Corte", "Corte de cabelo", null, 30);

        LocalDateTime inicio = LocalDateTime.of(2026, 4, 13, 10, 0);
        Agendamento ag = new Agendamento(null, "Cliente", "7777", servicoId, inicio, null, null, null);

        when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
        when(agendamentoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Agendamento salvo = agendamentoService.criarAgendamento(ag);

        assertNotNull(salvo.getId());
        assertEquals(inicio.plusMinutes(30), salvo.getDataHoraFim());
        assertEquals(StatusAgendamento.PENDENTE, salvo.getStatus());
        verify(agendamentoRepository).save(any(Agendamento.class));
    }

    @Test
    void criarAgendamento_deveLancarQuandoConflitoHorario() {
        UUID servicoId = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.of(2026, 4, 13, 10, 0);
        Agendamento novo = new Agendamento(null, "Cli", "1111", servicoId, inicio,
                inicio.plusMinutes(30), null, null);

        // existente que conflita
        Agendamento existente = new Agendamento(UUID.randomUUID(), "Out", "2222", servicoId,
                inicio.plusMinutes(15), inicio.plusMinutes(45), StatusAgendamento.ACEITO, null);

        when(agendamentoRepository.findAll()).thenReturn(List.of(existente));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                agendamentoService.criarAgendamento(novo));

        assertTrue(ex.getMessage().contains("Conflito de horário"));
    }

    @Test
    void atualizarStatus_deveAtualizar() {
        UUID id = UUID.randomUUID();
        Agendamento a = new Agendamento(id, "Nome", "3333", UUID.randomUUID(),
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(20), StatusAgendamento.PENDENTE, null);

        when(agendamentoRepository.findById(id)).thenReturn(Optional.of(a));
        when(agendamentoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Agendamento atualizado = agendamentoService.atualizarStatus(id, StatusAgendamento.ACEITO);

        assertEquals(StatusAgendamento.ACEITO, atualizado.getStatus());
        verify(agendamentoRepository).save(any(Agendamento.class));
    }

    @Test
    void deletar_deveChamarRepository() {
        UUID id = UUID.randomUUID();

        doNothing().when(agendamentoRepository).deleteById(id);

        agendamentoService.deletar(id);

        verify(agendamentoRepository).deleteById(id);
    }
}
