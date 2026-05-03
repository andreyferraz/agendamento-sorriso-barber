package com.barbeariasorrisobarber.agendamento.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.barbeariasorrisobarber.agendamento.service.ProdutoService;

@Controller
public class HomeController {

    private final ProdutoService produtoService;

    public HomeController(ProdutoService produtoService) {
        this.produtoService = produtoService;
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
        model.addAttribute("produtos", produtoService.listarTodos());
        return "produtos";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

}