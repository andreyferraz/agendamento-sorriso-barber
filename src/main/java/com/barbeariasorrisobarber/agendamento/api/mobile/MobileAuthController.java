package com.barbeariasorrisobarber.agendamento.api.mobile;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barbeariasorrisobarber.agendamento.api.mobile.security.MobileTokenService;
import com.barbeariasorrisobarber.agendamento.model.Barbeiro;
import com.barbeariasorrisobarber.agendamento.service.BarbeiroService;

@RestController
@RequestMapping("/api/auth")
public class MobileAuthController {

    private final BarbeiroService barbeiroService;
    private final PasswordEncoder passwordEncoder;
    private final MobileTokenService tokenService;

    public MobileAuthController(BarbeiroService barbeiroService, PasswordEncoder passwordEncoder,
            MobileTokenService tokenService) {
        this.barbeiroService = barbeiroService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody MobileAuthRequest request) {
        try {
            if (request == null || request.username() == null || request.username().isBlank()
                    || request.password() == null || request.password().isBlank()) {
                return ResponseEntity.badRequest().body(new MobileErrorResponse("Informe usuário e senha."));
            }

            Barbeiro barbeiro = barbeiroService.buscarPorUsername(request.username().trim())
                    .orElse(null);
            if (barbeiro == null || barbeiro.getSenhaHash() == null
                    || !passwordEncoder.matches(request.password(), barbeiro.getSenhaHash())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MobileErrorResponse("Usuário ou senha inválidos."));
            }

            var token = tokenService.gerarToken(barbeiro.getUsername());
            MobileAuthResponse response = new MobileAuthResponse(token.token(), "Bearer", token.expiresAt(),
                    new MobileBarbeiroResponse(barbeiro.getId(), barbeiro.getNome(), barbeiro.getUsername(),
                            barbeiro.getTelefone(), barbeiro.getEmail(), barbeiro.getFotoUrl(),
                            barbeiro.getComissaoPercentual()));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new MobileErrorResponse(ex.getMessage()));
        }
    }
}
