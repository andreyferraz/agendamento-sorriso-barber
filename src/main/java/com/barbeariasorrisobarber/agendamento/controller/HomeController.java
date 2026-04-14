package com.barbeariasorrisobarber.agendamento.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class HomeController {

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

}
