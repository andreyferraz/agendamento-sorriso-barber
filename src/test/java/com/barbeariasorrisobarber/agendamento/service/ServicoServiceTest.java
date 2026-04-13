package com.barbeariasorrisobarber.agendamento.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.barbeariasorrisobarber.agendamento.model.Servico;
import com.barbeariasorrisobarber.agendamento.repository.ServicoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoService servicoService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listarTodos_deveRetornarLista() {
        Servico s = new Servico(UUID.randomUUID(), "Corte", "Desc", BigDecimal.valueOf(30), 30);
        when(servicoRepository.findAll()).thenReturn(List.of(s));

        var lista = servicoService.listarTodos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals("Corte", lista.get(0).getNome());
    }

    @Test
    void buscarPorId_deveRetornarOptionalQuandoExistir() {
        UUID id = UUID.randomUUID();
        Servico s = new Servico(id, "Barba", "Desc", BigDecimal.valueOf(20), 20);
        when(servicoRepository.findById(id)).thenReturn(Optional.of(s));

        var opt = servicoService.buscarPorId(id);

        assertTrue(opt.isPresent());
        assertEquals(id, opt.get().getId());
    }

    @Test
    void criarServico_deveSalvarComId() {
        Servico s = new Servico(null, "Novo", "D", BigDecimal.valueOf(50), 60);
        when(servicoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Servico salvo = servicoService.criarServico(s);

        assertNotNull(salvo.getId());
        assertEquals(60, salvo.getDuracao());
        verify(servicoRepository).save(any(Servico.class));
    }

    @Test
    void criarServico_semNome_deveLancar() {
        Servico s = new Servico(null, null, "D", BigDecimal.valueOf(10), 10);
        assertThrows(IllegalArgumentException.class, () -> servicoService.criarServico(s));
    }

    @Test
    void criarServico_precoNegativo_deveLancar() {
        Servico s = new Servico(null, "X", "D", BigDecimal.valueOf(-1), 10);
        assertThrows(IllegalArgumentException.class, () -> servicoService.criarServico(s));
    }

    @Test
    void criarServico_semDuracao_deveLancar() {
        Servico s = new Servico(null, "Y", "D", BigDecimal.valueOf(10), null);
        assertThrows(IllegalArgumentException.class, () -> servicoService.criarServico(s));
    }

    @Test
    void atualizarServico_deveAtualizarCampos() {
        UUID id = UUID.randomUUID();
        Servico existente = new Servico(id, "A", "D", BigDecimal.valueOf(10), 15);
        Servico dados = new Servico(null, "B", "D2", BigDecimal.valueOf(12), 20);

        when(servicoRepository.findById(id)).thenReturn(Optional.of(existente));
        when(servicoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Servico atualizado = servicoService.atualizarServico(id, dados);

        assertEquals("B", atualizado.getNome());
        assertEquals("D2", atualizado.getDescricao());
        assertEquals(BigDecimal.valueOf(12), atualizado.getPreco());
        assertEquals(20, atualizado.getDuracao());
    }

    @Test
    void deletar_deveChamarRepository() {
        UUID id = UUID.randomUUID();
        doNothing().when(servicoRepository).deleteById(id);

        servicoService.deletar(id);

        verify(servicoRepository).deleteById(id);
    }
}
