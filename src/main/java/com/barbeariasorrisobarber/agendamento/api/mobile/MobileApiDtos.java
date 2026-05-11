package com.barbeariasorrisobarber.agendamento.api.mobile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.barbeariasorrisobarber.agendamento.enuns.StatusAgendamento;

record MobileAuthRequest(String username, String password) {
}

record MobileAuthResponse(String token, String tokenType, Instant expiresAt, MobileBarbeiroResponse barbeiro) {
}

record MobileBarbeiroResponse(UUID id, String nome, String username, String telefone, String email, String fotoUrl,
        BigDecimal comissaoPercentual) {
}

record MobileAgendamentoResponse(UUID id, LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, String cliente,
        String telefoneCliente, UUID servicoId, String servico, UUID barbeiroId, String barbeiro,
        StatusAgendamento status) {
}

record MobileComissaoResumoResponse(UUID barbeiroId, String barbeiro, BigDecimal percentual, BigDecimal gerado,
        BigDecimal pago, BigDecimal pendente) {
}

record MobileDashboardResponse(MobileBarbeiroResponse barbeiro, long totalAgendamentos, long pendentes,
        long aceitos, long pagos, List<MobileAgendamentoResponse> agendamentosPendentes,
        MobileComissaoResumoResponse comissao) {
}

record MobileErrorResponse(String message) {
}
