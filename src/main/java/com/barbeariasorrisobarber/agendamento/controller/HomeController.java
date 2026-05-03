package com.barbeariasorrisobarber.agendamento.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.barbeariasorrisobarber.agendamento.service.ProdutoService;
import com.barbeariasorrisobarber.agendamento.service.ServicoService;

@Controller
public class HomeController {

    private final ProdutoService produtoService;
    private final ServicoService servicoService;

    public HomeController(ProdutoService produtoService, ServicoService servicoService) {
        this.produtoService = produtoService;
        this.servicoService = servicoService;
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
        model.addAttribute("servicos", servicoService.listarTodos());
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