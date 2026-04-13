package com.barbeariasorrisobarber.agendamento.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.barbeariasorrisobarber.agendamento.model.Produto;
import com.barbeariasorrisobarber.agendamento.repository.ProdutoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private ProdutoService produtoService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listarTodos_deveRetornarLista() {
        Produto p = new Produto(UUID.randomUUID(), "Prod", "Desc", BigDecimal.valueOf(10), "img.webp", 5, true);
        when(produtoRepository.findAll()).thenReturn(List.of(p));

        var lista = produtoService.listarTodos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals("Prod", lista.get(0).getNome());
    }

    @Test
    void buscarPorId_deveRetornarOptionalQuandoExistir() {
        UUID id = UUID.randomUUID();
        Produto p = new Produto(id, "X", "D", BigDecimal.valueOf(20), null, 0, true);
        when(produtoRepository.findById(id)).thenReturn(Optional.of(p));

        var opt = produtoService.buscarPorId(id);

        assertTrue(opt.isPresent());
        assertEquals(id, opt.get().getId());
    }

    @Test
    void criarProduto_semImagem_deveSalvarComDefaults() {
        Produto p = new Produto(null, "Novo", "Desc", BigDecimal.valueOf(15), null, null, null);
        when(produtoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Produto salvo = produtoService.criarProduto(p);

        assertNotNull(salvo.getId());
        assertTrue(salvo.getAtivo());
        assertEquals(0, salvo.getEstoque());
        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    void criarProduto_comImagem_deveChamarFileUploadESalvarUrl() {
        Produto p = new Produto(null, "ComImg", "Desc", BigDecimal.valueOf(25), null, null, null);
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileUploadService.salvarImagem(mockFile)).thenReturn("imagem123.webp");
        when(produtoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Produto salvo = produtoService.criarProdutoComImagem(p, mockFile);

        assertEquals("imagem123.webp", salvo.getUrlImagem());
        verify(fileUploadService).salvarImagem(mockFile);
        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    void criarProduto_precoNegativo_deveLancar() {
        Produto p = new Produto(null, "Bad", "Desc", BigDecimal.valueOf(-5), null, null, null);

        assertThrows(IllegalArgumentException.class, () -> produtoService.criarProduto(p));
    }

    @Test
    void atualizarProduto_deveAtualizarCampos() {
        UUID id = UUID.randomUUID();
        Produto existente = new Produto(id, "A", "D", BigDecimal.valueOf(10), null, 1, true);
        Produto dados = new Produto(null, "B", "D2", BigDecimal.valueOf(12), "u.jpg", 2, false);

        when(produtoRepository.findById(id)).thenReturn(Optional.of(existente));
        when(produtoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Produto atualizado = produtoService.atualizarProduto(id, dados);

        assertEquals("B", atualizado.getNome());
        assertEquals("D2", atualizado.getDescricao());
        assertEquals(BigDecimal.valueOf(12), atualizado.getPreco());
        assertEquals("u.jpg", atualizado.getUrlImagem());
        assertEquals(2, atualizado.getEstoque());
        assertFalse(atualizado.getAtivo());
    }

    @Test
    void deletar_deveRemoverEChamarFileUpload() {
        UUID id = UUID.randomUUID();
        Produto existente = new Produto(id, "ToDel", "D", BigDecimal.valueOf(5), "file.webp", 1, true);
        when(produtoRepository.findById(id)).thenReturn(Optional.of(existente));
        doNothing().when(produtoRepository).deleteById(id);
        doNothing().when(fileUploadService).removerImagem("file.webp");

        produtoService.deletar(id);

        verify(produtoRepository).deleteById(id);
        verify(fileUploadService).removerImagem("file.webp");
    }

    @Test
    void deletar_deveSuportarFalhaNaRemocaoDaImagemENaoPropagar() {
        UUID id = UUID.randomUUID();
        Produto existente = new Produto(id, "ToDel", "D", BigDecimal.valueOf(5), "file.webp", 1, true);
        when(produtoRepository.findById(id)).thenReturn(Optional.of(existente));
        doNothing().when(produtoRepository).deleteById(id);
        doThrow(new RuntimeException("io error")).when(fileUploadService).removerImagem("file.webp");

        // não deve lançar
        produtoService.deletar(id);

        verify(produtoRepository).deleteById(id);
        verify(fileUploadService).removerImagem("file.webp");
    }
}
