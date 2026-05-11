package com.barbeariasorrisobarber.agendamento.api.mobile;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barbeariasorrisobarber.agendamento.enuns.StatusAgendamento;

@RestController
@RequestMapping("/api/barbeiro")
public class MobileBarbeiroController {

    private final MobileBarbeiroApiService mobileBarbeiroApiService;

    public MobileBarbeiroController(MobileBarbeiroApiService mobileBarbeiroApiService) {
        this.mobileBarbeiroApiService = mobileBarbeiroApiService;
    }

    @GetMapping("/me")
    public MobileBarbeiroResponse me(Authentication authentication) {
        return mobileBarbeiroApiService.obterPerfil(authentication.getName());
    }

    @GetMapping("/agendamentos")
    public List<MobileAgendamentoResponse> agendamentos(Authentication authentication,
            @RequestParam(name = "status", required = false) StatusAgendamento status) {
        return mobileBarbeiroApiService.listarAgendamentos(authentication.getName(), status);
    }

    @GetMapping("/agendamentos/pendentes")
    public List<MobileAgendamentoResponse> agendamentosPendentes(Authentication authentication) {
        return mobileBarbeiroApiService.listarPendentes(authentication.getName());
    }

    @GetMapping("/comissoes")
    public MobileComissaoResumoResponse comissoes(Authentication authentication) {
        return mobileBarbeiroApiService.obterComissao(authentication.getName());
    }

    @GetMapping("/dashboard")
    public MobileDashboardResponse dashboard(Authentication authentication) {
        return mobileBarbeiroApiService.obterDashboard(authentication.getName());
    }
}
