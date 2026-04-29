package com.barbeariasorrisobarber.agendamento.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.barbeariasorrisobarber.agendamento.model.Produto;
import com.barbeariasorrisobarber.agendamento.repository.ProdutoRepository;
import com.barbeariasorrisobarber.agendamento.utils.ValidationUtils;

@Service
public class ProdutoService {

	private final ProdutoRepository produtoRepository;
	private final FileUploadService fileUploadService;
	private static final Logger logger = LoggerFactory.getLogger(ProdutoService.class);

	public ProdutoService(ProdutoRepository produtoRepository, FileUploadService fileUploadService) {
		this.produtoRepository = produtoRepository;
		this.fileUploadService = fileUploadService;
	}

	public List<Produto> listarTodos() {
		return StreamSupport.stream(produtoRepository.findAll().spliterator(), false)
				.toList();
	}

	public Optional<Produto> buscarPorId(UUID id) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		return produtoRepository.findById(id);
	}

	@Transactional
	public Produto criarProduto(Produto produto) {
		return criarProdutoComImagem(produto, null);
	}

	public Produto criarProdutoComImagem(Produto produto, MultipartFile imagemFile) {
		ValidationUtils.validarCampoStringObrigatorio(produto.getNome(), "nome");
		ValidationUtils.validarCampoObrigatorio(produto.getPreco(), "preco");
		if (imagemFile != null && !imagemFile.isEmpty()) {
			String nomeArquivo = fileUploadService.salvarImagem(imagemFile);
			produto.setUrlImagem(nomeArquivo);
		}

		if (produto.getId() == null) {
			produto.setId(UUID.randomUUID());
		}

		// marcar como nova entidade para que Spring Data faça INSERT
		produto.setNew(true);

		if (produto.getEstoque() == null) {
			produto.setEstoque(0);
		}

		// garantir preco nao negativo
		BigDecimal preco = produto.getPreco();
		if (preco != null && preco.signum() < 0) {
			throw new IllegalArgumentException("Preco do produto nao pode ser negativo.");
		}

		return produtoRepository.save(produto);
	}

	@Transactional
	public Produto atualizarProduto(UUID id, Produto dados) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		ValidationUtils.validarCampoObrigatorio(dados, "dados");

		Produto existente = produtoRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado."));

		if (dados.getNome() != null) {
			ValidationUtils.validarCampoStringObrigatorio(dados.getNome(), "nome");
			existente.setNome(dados.getNome());
		}
		if (dados.getDescricao() != null) {
			existente.setDescricao(dados.getDescricao());
		}
		if (dados.getPreco() != null) {
			if (dados.getPreco().signum() < 0) {
				throw new IllegalArgumentException("Preco do produto nao pode ser negativo.");
			}
			existente.setPreco(dados.getPreco());
		}
		if (dados.getUrlImagem() != null) {
			existente.setUrlImagem(dados.getUrlImagem());
		}
		if (dados.getEstoque() != null) {
			existente.setEstoque(dados.getEstoque());
		}

		return produtoRepository.save(existente);
	}

	@Transactional
	public Produto atualizarProdutoComImagem(UUID id, Produto dados, MultipartFile imagemFile) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		ValidationUtils.validarCampoObrigatorio(dados, "dados");

		Produto existente = produtoRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado."));

		if (dados.getNome() != null) {
			ValidationUtils.validarCampoStringObrigatorio(dados.getNome(), "nome");
			existente.setNome(dados.getNome());
		}
		if (dados.getDescricao() != null) {
			existente.setDescricao(dados.getDescricao());
		}
		if (dados.getPreco() != null) {
			if (dados.getPreco().signum() < 0) {
				throw new IllegalArgumentException("Preco do produto nao pode ser negativo.");
			}
			existente.setPreco(dados.getPreco());
		}
		if (dados.getEstoque() != null) {
			existente.setEstoque(dados.getEstoque());
		}

		if (imagemFile != null && !imagemFile.isEmpty()) {
			String saved = fileUploadService.salvarImagem(imagemFile);
			// remove antiga imagem se existir
			if (existente.getUrlImagem() != null && !existente.getUrlImagem().isBlank()) {
				try { fileUploadService.removerImagem(existente.getUrlImagem()); } catch (Exception ex) { /* ignore */ }
			}
			existente.setUrlImagem(saved);
		}

		return produtoRepository.save(existente);
	}

	@Transactional
	public void deletar(UUID id) {
		ValidationUtils.validarCampoObrigatorio(id, "id");
		// buscar existente antes de deletar para recuperar url da imagem
		Produto existente = produtoRepository.findById(id).orElse(null);

		produtoRepository.deleteById(id);

		if (existente != null && existente.getUrlImagem() != null && !existente.getUrlImagem().isBlank()) {
			try {
				fileUploadService.removerImagem(existente.getUrlImagem());
			} catch (Exception ex) {
				// não reverte a transação por falha na remoção do arquivo; apenas loga
				logger.warn("Falha ao remover imagem do produto ({}): {}", existente.getUrlImagem(), ex.getMessage());
			}
		}
	}

}
