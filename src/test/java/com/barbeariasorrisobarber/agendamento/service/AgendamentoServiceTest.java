package com.barbeariasorrisobarber.agendamento.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.barbeariasorrisobarber.agendamento.enuns.StatusAgendamento;
import com.barbeariasorrisobarber.agendamento.enuns.TipoEntrada;
import com.barbeariasorrisobarber.agendamento.model.Agendamento;
import com.barbeariasorrisobarber.agendamento.model.Barbeiro;
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

    @Mock
    private BarbeiroService barbeiroService;

    @Mock
    private TransacaoFinanceiraService transacaoFinanceiraService;

    @InjectMocks
    private AgendamentoService agendamentoService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private Servico criarServico(UUID id, String nome, String descricao, BigDecimal preco, Integer duracao) {
        Servico servico = new Servico();
        servico.setId(id);
        servico.setNome(nome);
        servico.setDescricao(descricao);
        servico.setPreco(preco);
        servico.setDuracao(duracao);
        return servico;
    }

    @Test
    void listarTodos_deveRetornarLista() {
        Agendamento a = new Agendamento();
        a.setId(UUID.randomUUID());
        a.setNomeCliente("Joao");
        a.setTelefoneCliente("9999");
        a.setServicoId(UUID.randomUUID());
        a.setDataHoraInicio(LocalDateTime.now());
        a.setDataHoraFim(LocalDateTime.now().plusMinutes(30));
        a.setStatus(StatusAgendamento.PENDENTE);
        when(agendamentoRepository.findAll()).thenReturn(List.of(a));

        var lista = agendamentoService.listarTodos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals(a.getNomeCliente(), lista.get(0).getNomeCliente());
    }

    @Test
    void buscarPorId_deveRetornarOptionalQuandoExistir() {
        UUID id = UUID.randomUUID();
        Agendamento a = new Agendamento();
        a.setId(id);
        a.setNomeCliente("Maria");
        a.setTelefoneCliente("8888");
        a.setServicoId(UUID.randomUUID());
        a.setDataHoraInicio(LocalDateTime.now());
        a.setDataHoraFim(LocalDateTime.now().plusMinutes(45));
        a.setStatus(StatusAgendamento.PENDENTE);
        when(agendamentoRepository.findById(id)).thenReturn(Optional.of(a));

        var opt = agendamentoService.buscarPorId(id);

        assertTrue(opt.isPresent());
        assertEquals("Maria", opt.get().getNomeCliente());
    }

    @Test
    void criarAgendamento_deveCalcularDataHoraFimQuandoAusenteESetarStatusEId() {
        UUID servicoId = UUID.randomUUID();
        Servico servico = criarServico(servicoId, "Corte", "Corte de cabelo", null, 30);

        LocalDateTime inicio = LocalDateTime.of(2026, 4, 13, 10, 0);
        Agendamento ag = new Agendamento();
        ag.setNomeCliente("Cliente");
        ag.setTelefoneCliente("7777");
        ag.setServicoId(servicoId);
        ag.setDataHoraInicio(inicio);

        when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
        when(agendamentoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Agendamento salvo = agendamentoService.criarAgendamento(ag);

        assertNotNull(salvo.getId());
        assertEquals(inicio.plusMinutes(30), salvo.getDataHoraFim());
        assertEquals(StatusAgendamento.PENDENTE, salvo.getStatus());
        assertTrue(salvo.isNew());
        verify(agendamentoRepository).save(any(Agendamento.class));
    }

    @Test
    void atualizarAgendamento_devePreservarRegistroExistenteESalvarComoUpdate() {
        UUID agendamentoId = UUID.randomUUID();
        UUID servicoId = UUID.randomUUID();
        Servico servico = criarServico(servicoId, "Corte", "Corte de cabelo", null, 30);

        LocalDateTime inicio = LocalDateTime.of(2026, 4, 13, 11, 0);
        Agendamento existente = new Agendamento();
        existente.setId(agendamentoId);
        existente.setNomeCliente("Cliente Antigo");
        existente.setTelefoneCliente("1111");
        existente.setServicoId(servicoId);
        existente.setDataHoraInicio(LocalDateTime.of(2026, 4, 13, 10, 0));
        existente.setDataHoraFim(LocalDateTime.of(2026, 4, 13, 10, 30));
        existente.setStatus(StatusAgendamento.ACEITO);

        Agendamento dados = new Agendamento();
        dados.setNomeCliente("Cliente Novo");
        dados.setTelefoneCliente("9999");
        dados.setServicoId(servicoId);
        dados.setDataHoraInicio(inicio);

        when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(existente));
        when(agendamentoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Agendamento atualizado = agendamentoService.atualizarAgendamento(agendamentoId, dados);

        assertEquals(agendamentoId, atualizado.getId());
        assertEquals("Cliente Novo", atualizado.getNomeCliente());
        assertEquals("9999", atualizado.getTelefoneCliente());
        assertEquals(inicio.plusMinutes(30), atualizado.getDataHoraFim());
        assertEquals(StatusAgendamento.ACEITO, atualizado.getStatus());
        assertFalse(atualizado.isNew());
        verify(agendamentoRepository).save(any(Agendamento.class));
    }

    @Test
    void criarAgendamento_deveLancarQuandoConflitoHorario() {
        UUID servicoId = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.of(2026, 4, 13, 10, 0);
        Agendamento novo = new Agendamento();
        novo.setNomeCliente("Cli");
        novo.setTelefoneCliente("1111");
        novo.setServicoId(servicoId);
        novo.setDataHoraInicio(inicio);
        novo.setDataHoraFim(inicio.plusMinutes(30));

        // existente que conflita
        Agendamento existente = new Agendamento();
        existente.setId(UUID.randomUUID());
        existente.setNomeCliente("Out");
        existente.setTelefoneCliente("2222");
        existente.setServicoId(servicoId);
        existente.setDataHoraInicio(inicio.plusMinutes(15));
        existente.setDataHoraFim(inicio.plusMinutes(45));
        existente.setStatus(StatusAgendamento.ACEITO);

        when(agendamentoRepository.findAll()).thenReturn(List.of(existente));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                agendamentoService.criarAgendamento(novo));

        assertTrue(ex.getMessage().contains("Conflito de horário"));
    }

    @Test
    void atualizarStatus_deveAtualizar() {
        UUID id = UUID.randomUUID();
        Agendamento a = new Agendamento();
        a.setId(id);
        a.setNomeCliente("Nome");
        a.setTelefoneCliente("3333");
        a.setServicoId(UUID.randomUUID());
        a.setDataHoraInicio(LocalDateTime.now());
        a.setDataHoraFim(LocalDateTime.now().plusMinutes(20));
        a.setStatus(StatusAgendamento.PENDENTE);

        when(agendamentoRepository.findById(id)).thenReturn(Optional.of(a));
        when(agendamentoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Agendamento atualizado = agendamentoService.atualizarStatus(id, StatusAgendamento.ACEITO);

        assertEquals(StatusAgendamento.ACEITO, atualizado.getStatus());
        verify(agendamentoRepository).save(any(Agendamento.class));
    }

    @Test
    void atualizarStatus_paraPago_deveRegistrarEntradaDoAgendamento() {
        UUID agendamentoId = UUID.randomUUID();
        UUID servicoId = UUID.randomUUID();
        UUID barbeiroId = UUID.randomUUID();

        Servico servico = criarServico(servicoId, "Corte", "Corte de cabelo", BigDecimal.valueOf(100), 30);
        Barbeiro barbeiro = new Barbeiro();
        barbeiro.setId(barbeiroId);
        barbeiro.setNome("Barbeiro");
        barbeiro.setComissaoPercentual(BigDecimal.valueOf(30));

        Agendamento agendamento = new Agendamento();
        agendamento.setId(agendamentoId);
        agendamento.setNomeCliente("Nome");
        agendamento.setTelefoneCliente("3333");
        agendamento.setServicoId(servicoId);
        agendamento.setBarbeiroId(barbeiroId);
        agendamento.setDataHoraInicio(LocalDateTime.now());
        agendamento.setDataHoraFim(LocalDateTime.now().plusMinutes(20));
        agendamento.setStatus(StatusAgendamento.ACEITO);

        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
        when(barbeiroService.buscarPorId(barbeiroId)).thenReturn(Optional.of(barbeiro));
        when(agendamentoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transacaoFinanceiraService.criarTransacao(any())).thenAnswer(i -> i.getArgument(0));

        Agendamento atualizado = agendamentoService.atualizarStatus(agendamentoId, StatusAgendamento.PAGO);

        assertEquals(StatusAgendamento.PAGO, atualizado.getStatus());
        verify(transacaoFinanceiraService).criarTransacao(argThat(transacao ->
                transacao != null
                        && TipoEntrada.ENTRADA.equals(transacao.getTipo())
                        && BigDecimal.valueOf(100).compareTo(transacao.getValor()) == 0
                        && agendamentoId.equals(transacao.getAgendamentoId())
                        && barbeiroId.equals(transacao.getBarbeiroId())));
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
