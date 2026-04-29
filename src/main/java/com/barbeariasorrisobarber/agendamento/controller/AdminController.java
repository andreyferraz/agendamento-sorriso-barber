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
import com.barbeariasorrisobarber.agendamento.service.UsuarioAdminService;
import com.barbeariasorrisobarber.agendamento.service.ProdutoService;
import com.barbeariasorrisobarber.agendamento.model.Produto;
import java.math.BigDecimal;
import java.util.Optional;

@Controller
public class AdminController {

    private final UsuarioAdminService usuarioAdminService;
    private final UsuarioAdminRepository usuarioAdminRepository;
    private final ProdutoService produtoService;

    public AdminController(UsuarioAdminService usuarioAdminService,
            UsuarioAdminRepository usuarioAdminRepository, ProdutoService produtoService) {
        this.usuarioAdminService = usuarioAdminService;
        this.usuarioAdminRepository = usuarioAdminRepository;
        this.produtoService = produtoService;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("usuarios", usuarioAdminRepository.findByUsernameNot("admin"));
        model.addAttribute("produtos", produtoService.listarTodos());
        return "admin/admin";
    }

    @GetMapping("/admin/produtos/{id}/edit")
    public String editarProduto(@PathVariable String id, Model model) {
        model.addAttribute("usuarios", usuarioAdminRepository.findByUsernameNot("admin"));
        model.addAttribute("produtos", produtoService.listarTodos());
        try {
            var uuid = UUID.fromString(id);
            Optional<Produto> p = produtoService.buscarPorId(uuid);
            p.ifPresent(prod -> model.addAttribute("produtoToEdit", prod));
        } catch (Exception e) {
            // ignore invalid id — view will simply not prefill
        }
        return "admin/admin";
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
                redirectAttrs.addFlashAttribute("success", "Produto atualizado com sucesso.");
            } else {
                produtoService.criarProdutoComImagem(dados, imagem);
                redirectAttrs.addFlashAttribute("success", "Produto criado com sucesso.");
            }
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/produtos/{id}/delete")
    public String deletarProduto(@PathVariable String id, RedirectAttributes redirectAttrs) {
        try {
            UUID uuid = UUID.fromString(id);
            produtoService.deletar(uuid);
            redirectAttrs.addFlashAttribute("success", "Produto removido.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
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
            redirectAttrs.addFlashAttribute("success", "Usuário criado com sucesso.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/users/{id}/password")
    public String alterarSenha(@PathVariable String id, @RequestParam("novaSenha") String novaSenha,
            RedirectAttributes redirectAttrs) {
        try {
            UUID uuid = UUID.fromString(id);
            usuarioAdminService.atualizarSenha(uuid, novaSenha);
            redirectAttrs.addFlashAttribute("success", "Senha atualizada com sucesso.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deletarUsuario(@PathVariable String id, RedirectAttributes redirectAttrs) {
        try {
            UUID uuid = UUID.fromString(id);
            usuarioAdminService.deletar(uuid);
            redirectAttrs.addFlashAttribute("success", "Usuário removido.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/me/password")
    public String alterarMinhaSenha(@RequestParam("senhaAtual") String senhaAtual,
            @RequestParam("novaSenha") String novaSenha, RedirectAttributes redirectAttrs) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            var opt = usuarioAdminRepository.findByUsername(username);
            if (opt.isEmpty()) {
                redirectAttrs.addFlashAttribute("error", "Usuário não encontrado.");
                return "redirect:/admin";
            }
            if (!usuarioAdminService.autenticar(username, senhaAtual)) {
                redirectAttrs.addFlashAttribute("error", "Senha atual inválida.");
                return "redirect:/admin";
            }
            usuarioAdminService.atualizarSenha(opt.get().getId(), novaSenha);
            redirectAttrs.addFlashAttribute("success", "Senha atualizada com sucesso.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

}