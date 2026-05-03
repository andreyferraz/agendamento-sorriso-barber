package com.barbeariasorrisobarber.agendamento.controller;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.barbeariasorrisobarber.agendamento.repository.UsuarioAdminRepository;
import com.barbeariasorrisobarber.agendamento.service.ServicoService;
import com.barbeariasorrisobarber.agendamento.service.UsuarioAdminService;
import com.barbeariasorrisobarber.agendamento.service.ProdutoService;
import com.barbeariasorrisobarber.agendamento.model.Servico;
import com.barbeariasorrisobarber.agendamento.model.Produto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Controller
public class AdminController {

    private static final String ADMIN_USERNAME = "admin";
    private static final String VIEW_ADMIN = "admin/admin";
    private static final String REDIRECT_ADMIN = "redirect:/admin";
    private static final String FLASH_SUCCESS = "success";
    private static final String FLASH_ERROR = "error";
    private static final String ATTR_USUARIOS = "usuarios";
    private static final String ATTR_PRODUTOS = "produtos";
    private static final String ATTR_SERVICOS = "servicos";

    private final UsuarioAdminService usuarioAdminService;
    private final UsuarioAdminRepository usuarioAdminRepository;
    private final ProdutoService produtoService;
    private final ServicoService servicoService;

    public AdminController(UsuarioAdminService usuarioAdminService,
            UsuarioAdminRepository usuarioAdminRepository, ProdutoService produtoService,
            ServicoService servicoService) {
        this.usuarioAdminService = usuarioAdminService;
        this.usuarioAdminRepository = usuarioAdminRepository;
        this.produtoService = produtoService;
        this.servicoService = servicoService;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute(ATTR_USUARIOS, usuarioAdminRepository.findByUsernameNot(ADMIN_USERNAME));
        model.addAttribute(ATTR_PRODUTOS, produtoService.listarTodos());
        model.addAttribute(ATTR_SERVICOS, servicoService.listarTodos());
        return VIEW_ADMIN;
    }

    @GetMapping("/admin/produtos/{id}/edit")
    public String editarProduto(@PathVariable String id, Model model) {
        model.addAttribute(ATTR_USUARIOS, usuarioAdminRepository.findByUsernameNot(ADMIN_USERNAME));
        model.addAttribute(ATTR_PRODUTOS, produtoService.listarTodos());
        model.addAttribute(ATTR_SERVICOS, servicoService.listarTodos());
        try {
            var uuid = UUID.fromString(id);
            Optional<Produto> p = produtoService.buscarPorId(uuid);
            p.ifPresent(prod -> model.addAttribute("produtoToEdit", prod));
        } catch (Exception e) {
            // ignore invalid id — view will simply not prefill
        }
        return VIEW_ADMIN;
    }

    @GetMapping("/admin/servicos/{id}/edit")
    public String editarServico(@PathVariable String id, Model model) {
        model.addAttribute(ATTR_USUARIOS, usuarioAdminRepository.findByUsernameNot(ADMIN_USERNAME));
        model.addAttribute(ATTR_PRODUTOS, produtoService.listarTodos());
        model.addAttribute(ATTR_SERVICOS, servicoService.listarTodos());
        try {
            var uuid = UUID.fromString(id);
            Optional<Servico> servico = servicoService.buscarPorId(uuid);
            servico.ifPresent(item -> model.addAttribute("servicoToEdit", item));
        } catch (Exception e) {
            // ignore invalid id — view will simply not prefill
        }
        return VIEW_ADMIN;
    }

    @PostMapping("/admin/produtos")
    public String criarOuAtualizarProduto(@RequestParam(name = "id", required = false) String id,
            @RequestParam("nome") String nome, @RequestParam("preco") String preco,
            @RequestParam(name = "descricao", required = false) String descricao,
            @RequestParam(name = "estoque", required = false) Integer estoque,
            @RequestParam(name = "imagem", required = false) MultipartFile imagem,
            RedirectAttributes redirectAttrs) {
        try {
            // normalizar preco (aceita vírgula)
            String cleaned = preco == null ? "" : preco.replaceAll("[^0-9,\\.]", "").replace(',', '.');
            BigDecimal precoVal = new BigDecimal(cleaned);

            Produto dados = new Produto();
            dados.setNome(nome);
            dados.setDescricao(descricao);
            dados.setPreco(precoVal);
            dados.setEstoque(estoque != null ? estoque : 0);

            if (id != null && !id.isBlank()) {
                UUID uuid = UUID.fromString(id);
                if (imagem != null && !imagem.isEmpty()) {
                    produtoService.atualizarProdutoComImagem(uuid, dados, imagem);
                } else {
                    produtoService.atualizarProduto(uuid, dados);
                }
                redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Produto atualizado com sucesso.");
            } else {
                produtoService.criarProdutoComImagem(dados, imagem);
                redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Produto criado com sucesso.");
            }
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(FLASH_ERROR, e.getMessage());
        }
        return REDIRECT_ADMIN;
    }

    @PostMapping("/admin/servicos")
    public String criarOuAtualizarServico(@RequestParam(name = "id", required = false) String id,
            @RequestParam("nome") String nome,
            @RequestParam("preco") String preco,
            @RequestParam("duracao") Integer duracao,
            @RequestParam(name = "descricao", required = false) String descricao,
            RedirectAttributes redirectAttrs) {
        try {
            String cleaned = preco == null ? "" : preco.replaceAll("[^0-9,\\.]", "").replace(',', '.');
            BigDecimal precoVal = new BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP);

            Servico dados = new Servico();
            dados.setNome(nome);
            dados.setDescricao(descricao);
            dados.setPreco(precoVal);
            dados.setDuracao(duracao);

            if (id != null && !id.isBlank()) {
                UUID uuid = UUID.fromString(id);
                servicoService.atualizarServico(uuid, dados);
                redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Serviço atualizado com sucesso.");
            } else {
                servicoService.criarServico(dados);
                redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Serviço criado com sucesso.");
            }
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(FLASH_ERROR, e.getMessage());
        }
        return REDIRECT_ADMIN;
    }

    @PostMapping("/admin/servicos/{id}/delete")
    public String deletarServico(@PathVariable String id, RedirectAttributes redirectAttrs) {
        try {
            UUID uuid = UUID.fromString(id);
            servicoService.deletar(uuid);
            redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Serviço removido.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(FLASH_ERROR, e.getMessage());
        }
        return REDIRECT_ADMIN;
    }

    @PostMapping("/admin/produtos/{id}/delete")
    public String deletarProduto(@PathVariable String id, RedirectAttributes redirectAttrs) {
        try {
            UUID uuid = UUID.fromString(id);
            produtoService.deletar(uuid);
            redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Produto removido.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(FLASH_ERROR, e.getMessage());
        }
        return REDIRECT_ADMIN;
    }

    @PostMapping("/admin/users")
    public String criarUsuario(@RequestParam String username, @RequestParam String password,
            @RequestParam(name = "foto", required = false) MultipartFile foto, RedirectAttributes redirectAttrs) {
        try {
            if (foto != null && !foto.isEmpty()) {
                usuarioAdminService.criarAdmin(username, password, foto);
            } else {
                usuarioAdminService.criarAdmin(username, password);
            }
            redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Usuário criado com sucesso.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(FLASH_ERROR, e.getMessage());
        }
        return REDIRECT_ADMIN;
    }

    @PostMapping("/admin/users/{id}/password")
    public String alterarSenha(@PathVariable String id, @RequestParam("novaSenha") String novaSenha,
            RedirectAttributes redirectAttrs) {
        try {
            UUID uuid = UUID.fromString(id);
            usuarioAdminService.atualizarSenha(uuid, novaSenha);
            redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Senha atualizada com sucesso.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(FLASH_ERROR, e.getMessage());
        }
        return REDIRECT_ADMIN;
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deletarUsuario(@PathVariable String id, RedirectAttributes redirectAttrs) {
        try {
            UUID uuid = UUID.fromString(id);
            usuarioAdminService.deletar(uuid);
            redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Usuário removido.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(FLASH_ERROR, e.getMessage());
        }
        return REDIRECT_ADMIN;
    }

    @PostMapping("/admin/me/password")
    public String alterarMinhaSenha(@RequestParam("senhaAtual") String senhaAtual,
            @RequestParam("novaSenha") String novaSenha, RedirectAttributes redirectAttrs) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                redirectAttrs.addFlashAttribute(FLASH_ERROR, "Sessão inválida.");
                return REDIRECT_ADMIN;
            }

            String username = authentication.getName();
            var opt = usuarioAdminRepository.findByUsername(username);
            if (opt.isEmpty()) {
                redirectAttrs.addFlashAttribute(FLASH_ERROR, "Usuário não encontrado.");
                return REDIRECT_ADMIN;
            }
            if (!usuarioAdminService.autenticar(username, senhaAtual)) {
                redirectAttrs.addFlashAttribute(FLASH_ERROR, "Senha atual inválida.");
                return REDIRECT_ADMIN;
            }
            usuarioAdminService.atualizarSenha(opt.get().getId(), novaSenha);
            redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Senha atualizada com sucesso.");
            return REDIRECT_ADMIN + "?tab=senha";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(FLASH_ERROR, e.getMessage());
        }
        return REDIRECT_ADMIN;
    }

}