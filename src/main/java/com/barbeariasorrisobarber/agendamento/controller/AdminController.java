package com.barbeariasorrisobarber.agendamento.controller;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.barbeariasorrisobarber.agendamento.repository.UsuarioAdminRepository;
import com.barbeariasorrisobarber.agendamento.service.UsuarioAdminService;

@Controller
public class AdminController {

    private final UsuarioAdminService usuarioAdminService;
    private final UsuarioAdminRepository usuarioAdminRepository;

    public AdminController(UsuarioAdminService usuarioAdminService,
            UsuarioAdminRepository usuarioAdminRepository) {
        this.usuarioAdminService = usuarioAdminService;
        this.usuarioAdminRepository = usuarioAdminRepository;
    }

    @PostMapping("/admin/users")
    public String criarUsuario(@RequestParam String username, @RequestParam String password,
            RedirectAttributes redirectAttrs) {
        try {
            usuarioAdminService.criarAdmin(username, password);
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
