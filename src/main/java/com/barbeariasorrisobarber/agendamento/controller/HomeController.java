package com.barbeariasorrisobarber.agendamento.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import com.barbeariasorrisobarber.agendamento.repository.UsuarioAdminRepository;

@Controller
public class HomeController {

    private final UsuarioAdminRepository usuarioAdminRepository;

    public HomeController(UsuarioAdminRepository usuarioAdminRepository) {
        this.usuarioAdminRepository = usuarioAdminRepository;
    }

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/sobre")
    public String sobre(Model model) {
        return "sobre";
    }

    @GetMapping("/servicos")
    public String services(Model model) {
        return "servicos";
    }

    @GetMapping("/produtos")
    public String products(Model model) {
        return "produtos";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("usuarios", usuarioAdminRepository.findAll());
        return "admin/admin";
    }

}
