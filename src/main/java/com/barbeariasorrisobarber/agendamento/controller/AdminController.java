package com.barbeariasorrisobarber.agendamento.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.UUID;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.barbeariasorrisobarber.agendamento.enuns.StatusAgendamento;
import com.barbeariasorrisobarber.agendamento.model.Agendamento;
import com.barbeariasorrisobarber.agendamento.model.Barbeiro;
import com.barbeariasorrisobarber.agendamento.repository.UsuarioAdminRepository;
import com.barbeariasorrisobarber.agendamento.service.AgendamentoService;
import com.barbeariasorrisobarber.agendamento.service.BarbeiroService;
import com.barbeariasorrisobarber.agendamento.service.ServicoService;
import com.barbeariasorrisobarber.agendamento.service.UsuarioAdminService;
import com.barbeariasorrisobarber.agendamento.service.ProdutoService;
import com.barbeariasorrisobarber.agendamento.model.Servico;
import com.barbeariasorrisobarber.agendamento.model.Produto;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.math.RoundingMode;

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
    private static final String ATTR_BARBEIROS = "barbeiros";
    private static final String ATTR_BARBEIRO_TO_EDIT = "barbeiroToEdit";
    private static final String ATTR_AGENDAMENTOS = "agendamentos";
    private static final String ATTR_AGENDAMENTOS_RESUMO = "agendamentosResumo";
    private static final String ATTR_AGENDAMENTOS_PENDENTES = "agendamentosPendentes";
    private static final String ATTR_CALENDARIO = "calendarioAgendamentos";
    private static final String ATTR_MES_CALENDARIO = "mesCalendario";
    private static final String TAB_AGENDAMENTOS = "agendamentos";
    private static final String TAB_QUERY = "?tab=";
    private static final java.util.Locale PT_BR = java.util.Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String PRICE_CLEANUP_REGEX = "[^0-9,\\.]";

    private final UsuarioAdminService usuarioAdminService;
    private final UsuarioAdminRepository usuarioAdminRepository;
    private final ProdutoService produtoService;
    private final ServicoService servicoService;
    private final AgendamentoService agendamentoService;
    private final BarbeiroService barbeiroService;

    public AdminController(UsuarioAdminService usuarioAdminService,
            UsuarioAdminRepository usuarioAdminRepository, ProdutoService produtoService,
            ServicoService servicoService, AgendamentoService agendamentoService,
            BarbeiroService barbeiroService) {
        this.usuarioAdminService = usuarioAdminService;
        this.usuarioAdminRepository = usuarioAdminRepository;
        this.produtoService = produtoService;
        this.servicoService = servicoService;
        this.agendamentoService = agendamentoService;
        this.barbeiroService = barbeiroService;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        carregarDadosBaseAdmin(model);
        return VIEW_ADMIN;
    }

    @GetMapping("/admin/produtos/{id}/edit")
    public String editarProduto(@PathVariable String id, Model model) {
        carregarDadosBaseAdmin(model);
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
        carregarDadosBaseAdmin(model);
        try {
            var uuid = UUID.fromString(id);
            Optional<Servico> servico = servicoService.buscarPorId(uuid);
            servico.ifPresent(item -> model.addAttribute("servicoToEdit", item));
        } catch (Exception e) {
            // ignore invalid id — view will simply not prefill
        }
        return VIEW_ADMIN;
    }

    @GetMapping("/admin/barbeiros/{id}/edit")
    public String editarBarbeiro(@PathVariable String id, Model model) {
        carregarDadosBaseAdmin(model);
        try {
            var uuid = UUID.fromString(id);
            Optional<Barbeiro> barbeiro = barbeiroService.buscarPorId(uuid);
            barbeiro.ifPresent(item -> model.addAttribute(ATTR_BARBEIRO_TO_EDIT, item));
        } catch (Exception e) {
            // ignore invalid id — view will simply not prefill
        }
        return VIEW_ADMIN;
    }

    @GetMapping("/admin/agendamentos/{id}/edit")
    public String editarAgendamento(@PathVariable String id, Model model) {
        carregarDadosBaseAdmin(model);
        try {
            var uuid = UUID.fromString(id);
            Optional<Agendamento> agendamento = agendamentoService.buscarPorId(uuid);
            agendamento.ifPresent(item -> model.addAttribute("agendamentoToEdit", item));
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
            String cleaned = preco == null ? "" : preco.replaceAll(PRICE_CLEANUP_REGEX, "").replace(',', '.');
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
            String cleaned = preco == null ? "" : preco.replaceAll(PRICE_CLEANUP_REGEX, "").replace(',', '.');
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

    @PostMapping("/admin/barbeiros")
    public String criarOuAtualizarBarbeiro(@RequestParam(name = "id", required = false) String id,
            @RequestParam("nome") String nome,
            @RequestParam("username") String username,
            @RequestParam(name = "password", required = false) String password,
            @RequestParam("comissaoPercentual") String comissaoPercentual,
            @RequestParam(name = "horarioInicioAtendimento", required = false) String horarioInicioAtendimento,
            @RequestParam(name = "horarioFimAtendimento", required = false) String horarioFimAtendimento,
            @RequestParam(name = "horariosSegunda", required = false) String horariosSegunda,
            @RequestParam(name = "horariosTerca", required = false) String horariosTerca,
            @RequestParam(name = "horariosQuarta", required = false) String horariosQuarta,
            @RequestParam(name = "horariosQuinta", required = false) String horariosQuinta,
            @RequestParam(name = "horariosSexta", required = false) String horariosSexta,
            @RequestParam(name = "horariosSabado", required = false) String horariosSabado,
            @RequestParam(name = "horariosDomingo", required = false) String horariosDomingo,
            @RequestParam(name = "telefone", required = false) String telefone,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "foto", required = false) MultipartFile foto,
            RedirectAttributes redirectAttrs) {
        try {
            Barbeiro dados = new Barbeiro();
            dados.setNome(nome);
            dados.setUsername(username);
            dados.setTelefone(telefone);
            dados.setEmail(email);
            dados.setComissaoPercentual(parseComissao(comissaoPercentual));
            dados.setHorarioInicioAtendimento(parseHora(horarioInicioAtendimento));
            dados.setHorarioFimAtendimento(parseHora(horarioFimAtendimento));
            dados.setHorariosSegunda(normalizarHorarios(horariosSegunda));
            dados.setHorariosTerca(normalizarHorarios(horariosTerca));
            dados.setHorariosQuarta(normalizarHorarios(horariosQuarta));
            dados.setHorariosQuinta(normalizarHorarios(horariosQuinta));
            dados.setHorariosSexta(normalizarHorarios(horariosSexta));
            dados.setHorariosSabado(normalizarHorarios(horariosSabado));
            dados.setHorariosDomingo(normalizarHorarios(horariosDomingo));
            dados.setHorarioSegundaInicio(null);
            dados.setHorarioSegundaFim(null);
            dados.setHorarioTercaInicio(null);
            dados.setHorarioTercaFim(null);
            dados.setHorarioQuartaInicio(null);
            dados.setHorarioQuartaFim(null);
            dados.setHorarioQuintaInicio(null);
            dados.setHorarioQuintaFim(null);
            dados.setHorarioSextaInicio(null);
            dados.setHorarioSextaFim(null);
            dados.setHorarioSabadoInicio(null);
            dados.setHorarioSabadoFim(null);
            dados.setHorarioDomingoInicio(null);
            dados.setHorarioDomingoFim(null);

            if (id != null && !id.isBlank()) {
                UUID uuid = UUID.fromString(id);
                barbeiroService.atualizarBarbeiro(uuid, dados, password, foto);
                redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Barbeiro atualizado com sucesso.");
            } else {
                barbeiroService.criarBarbeiro(dados, password, foto);
                redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Barbeiro criado com sucesso.");
            }
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(FLASH_ERROR, e.getMessage());
        }
        return REDIRECT_ADMIN;
    }

    @PostMapping("/admin/agendamentos")
    public String criarAgendamentoAdmin(@RequestParam(name = "id", required = false) String id,
            @RequestParam("nomeCliente") String nomeCliente,
            @RequestParam("telefoneCliente") String telefoneCliente,
            @RequestParam("barbeiroId") UUID barbeiroId,
            @RequestParam("servicoId") UUID servicoId,
            @RequestParam("dataAgendamento") String dataAgendamento,
            @RequestParam("horaAgendamento") String horaAgendamento,
            RedirectAttributes redirectAttrs) {
        try {
            Agendamento agendamento = new Agendamento();
            agendamento.setNomeCliente(nomeCliente);
            agendamento.setTelefoneCliente(telefoneCliente);
            agendamento.setBarbeiroId(barbeiroId);
            agendamento.setServicoId(servicoId);
            agendamento.setDataHoraInicio(LocalDateTime.of(LocalDate.parse(dataAgendamento), LocalTime.parse(horaAgendamento)));
            agendamento.setStatus(StatusAgendamento.PENDENTE);

            if (id != null && !id.isBlank()) {
                agendamento.setId(UUID.fromString(id));
            }

            agendamentoService.criarAgendamento(agendamento);
            redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Agendamento criado com sucesso.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(FLASH_ERROR, e.getMessage());
        }
        return REDIRECT_ADMIN + TAB_QUERY + TAB_AGENDAMENTOS;
    }

    @PostMapping("/admin/agendamentos/{id}/aceitar")
    public String aceitarAgendamento(@PathVariable String id, RedirectAttributes redirectAttrs) {
        try {
            UUID uuid = UUID.fromString(id);
            agendamentoService.atualizarStatus(uuid, StatusAgendamento.ACEITO);
            redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Agendamento aceito com sucesso.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(FLASH_ERROR, e.getMessage());
        }
        return REDIRECT_ADMIN + TAB_QUERY + TAB_AGENDAMENTOS;
    }

    @PostMapping("/admin/agendamentos/{id}/recusar")
    public String recusarAgendamento(@PathVariable String id, RedirectAttributes redirectAttrs) {
        try {
            UUID uuid = UUID.fromString(id);
            agendamentoService.atualizarStatus(uuid, StatusAgendamento.RECUSADO);
            redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Agendamento recusado.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(FLASH_ERROR, e.getMessage());
        }
        return REDIRECT_ADMIN + TAB_QUERY + TAB_AGENDAMENTOS;
    }

    @PostMapping("/admin/agendamentos/{id}/delete")
    public String deletarAgendamento(@PathVariable String id, RedirectAttributes redirectAttrs) {
        try {
            UUID uuid = UUID.fromString(id);
            agendamentoService.deletar(uuid);
            redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Agendamento removido.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute(FLASH_ERROR, e.getMessage());
        }
        return REDIRECT_ADMIN + TAB_QUERY + TAB_AGENDAMENTOS;
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

    @PostMapping("/admin/barbeiros/{id}/delete")
    public String deletarBarbeiro(@PathVariable String id, RedirectAttributes redirectAttrs) {
        try {
            UUID uuid = UUID.fromString(id);
            barbeiroService.deletar(uuid);
            redirectAttrs.addFlashAttribute(FLASH_SUCCESS, "Barbeiro removido.");
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

    private void carregarDadosBaseAdmin(Model model) {
        List<Barbeiro> barbeiros = barbeiroService.listarTodos().stream()
            .sorted(Comparator.comparing(Barbeiro::getNome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
        List<Servico> servicos = servicoService.listarTodos().stream()
            .sorted(Comparator.comparing(Servico::getNome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
        List<Agendamento> agendamentos = agendamentoService.listarTodos().stream()
            .sorted(Comparator.comparing(Agendamento::getDataHoraInicio, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        List<Agendamento> agendamentosPendentes = agendamentos.stream()
            .filter(agendamento -> agendamento.getStatus() == StatusAgendamento.PENDENTE)
            .toList();

        Map<UUID, String> barbeiroNomes = montarMapaNomesBarbeiros(barbeiros);
        Map<UUID, String> servicoNomes = montarMapaNomesServicos(servicos);

        model.addAttribute(ATTR_USUARIOS, usuarioAdminRepository.findByUsernameNot(ADMIN_USERNAME));
        model.addAttribute(ATTR_PRODUTOS, produtoService.listarTodos());
        model.addAttribute(ATTR_SERVICOS, servicos);
        model.addAttribute(ATTR_BARBEIROS, barbeiros);
        model.addAttribute(ATTR_AGENDAMENTOS, agendamentos);
        model.addAttribute(ATTR_AGENDAMENTOS_PENDENTES, montarResumoAgendamentos(agendamentosPendentes, barbeiroNomes, servicoNomes));
        model.addAttribute(ATTR_AGENDAMENTOS_RESUMO, montarResumoAgendamentos(agendamentos, barbeiroNomes, servicoNomes));
        model.addAttribute(ATTR_CALENDARIO, montarCalendarioMensal(agendamentos, barbeiroNomes, servicoNomes));
        model.addAttribute(ATTR_MES_CALENDARIO, formatarMesAtual());
    }

    private BigDecimal parseComissao(String valor) {
        String cleaned = valor == null ? "" : valor.replaceAll(PRICE_CLEANUP_REGEX, "").replace(',', '.');
        return new BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP);
    }

    private String parseHora(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }

    private String normalizarHorarios(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        java.util.List<String> horarios = new java.util.ArrayList<>();
        for (String item : valor.split("[\\n,;]+")) {
            String token = item == null ? "" : item.trim();
            if (token.isBlank()) {
                continue;
            }

            horarios.add(token);
        }

        if (horarios.isEmpty()) {
            return null;
        }

        return String.join("\n", horarios.stream().distinct().toList());
    }

    private Map<UUID, String> montarMapaNomesBarbeiros(List<Barbeiro> barbeiros) {
        Map<UUID, String> nomes = new LinkedHashMap<>();
        for (Barbeiro barbeiro : barbeiros) {
            if (barbeiro != null && barbeiro.getId() != null) {
                nomes.put(barbeiro.getId(), barbeiro.getNome() != null ? barbeiro.getNome() : "Barbeiro");
            }
        }
        return nomes;
    }

    private Map<UUID, String> montarMapaNomesServicos(List<Servico> servicos) {
        Map<UUID, String> nomes = new LinkedHashMap<>();
        for (Servico servico : servicos) {
            if (servico != null && servico.getId() != null) {
                nomes.put(servico.getId(), servico.getNome() != null ? servico.getNome() : "Serviço");
            }
        }
        return nomes;
    }

    private List<AgendamentoResumoView> montarResumoAgendamentos(List<Agendamento> agendamentos,
            Map<UUID, String> barbeiroNomes, Map<UUID, String> servicoNomes) {
        return agendamentos.stream()
            .map(agendamento -> toResumoView(agendamento, barbeiroNomes, servicoNomes))
            .toList();
    }

    private List<CalendarDayView> montarCalendarioMensal(List<Agendamento> agendamentos,
            Map<UUID, String> barbeiroNomes, Map<UUID, String> servicoNomes) {
        YearMonth mesAtual = YearMonth.from(LocalDate.now());
        LocalDate inicio = mesAtual.atDay(1)
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate fim = mesAtual.atEndOfMonth()
                .with(java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        Map<LocalDate, List<AgendamentoResumoView>> porDia = agendamentos.stream()
                .collect(Collectors.groupingBy(
                        agendamento -> agendamento.getDataHoraInicio().toLocalDate(),
                        Collectors.mapping(
                                agendamento -> toResumoView(agendamento, barbeiroNomes, servicoNomes),
                                Collectors.toList())));

        List<CalendarDayView> dias = new java.util.ArrayList<>();
        for (LocalDate cursor = inicio; !cursor.isAfter(fim); cursor = cursor.plusDays(1)) {
            List<AgendamentoResumoView> itens = porDia.getOrDefault(cursor, List.of()).stream()
                    .sorted(Comparator.comparing(AgendamentoResumoView::hora))
                    .toList();
            dias.add(new CalendarDayView(cursor, YearMonth.from(cursor).equals(mesAtual), itens));
        }
        return dias;
    }

    private AgendamentoResumoView toResumoView(Agendamento agendamento,
            Map<UUID, String> barbeiroNomes, Map<UUID, String> servicoNomes) {
        String data = agendamento.getDataHoraInicio() != null ? agendamento.getDataHoraInicio().toLocalDate().format(DATE_FORMAT) : "-";
        String hora = agendamento.getDataHoraInicio() != null ? agendamento.getDataHoraInicio().toLocalTime().format(TIME_FORMAT) : "-";
        String cliente = safeText(agendamento.getNomeCliente(), "Cliente");
        String telefoneCliente = safeText(agendamento.getTelefoneCliente(), "");
        String servico = safeText(servicoNomes.get(agendamento.getServicoId()), "Serviço");
        String barbeiro = safeText(barbeiroNomes.get(agendamento.getBarbeiroId()), "Barbeiro");
        String status = agendamento.getStatus() != null ? agendamento.getStatus().name() : StatusAgendamento.PENDENTE.name();
        String whatsappUrl = montarWhatsAppUrl(telefoneCliente, agendamento);
        return new AgendamentoResumoView(agendamento.getId(), data, hora, cliente, servico, barbeiro, status, telefoneCliente, whatsappUrl);
    }

    private String montarWhatsAppUrl(String telefone, Agendamento agendamento) {
        String digits = telefone == null ? "" : telefone.replaceAll("\\D+", "");
        if (digits.isBlank()) {
            return null;
        }

        if (digits.length() <= 11 && !digits.startsWith("55")) {
            digits = "55" + digits;
        }

        String cliente = safeText(agendamento.getNomeCliente(), "cliente");
        String data = agendamento.getDataHoraInicio() != null ? agendamento.getDataHoraInicio().toLocalDate().format(DATE_FORMAT) : "";
        String hora = agendamento.getDataHoraInicio() != null ? agendamento.getDataHoraInicio().toLocalTime().format(TIME_FORMAT) : "";
        String mensagem = "Olá, " + cliente + ". Seu agendamento de " + data + " às " + hora + " foi recusado. Se quiser, responda esta mensagem para combinarmos uma nova opção.";
        return "https://wa.me/" + digits + "?text=" + URLEncoder.encode(mensagem, StandardCharsets.UTF_8);
    }

    private String formatarMesAtual() {
        Month mes = YearMonth.from(LocalDate.now()).getMonth();
        String nomeMes = mes.getDisplayName(TextStyle.FULL, PT_BR);
        return Character.toUpperCase(nomeMes.charAt(0)) + nomeMes.substring(1) + " de " + YearMonth.now().getYear();
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private record CalendarDayView(LocalDate date, boolean currentMonth, List<AgendamentoResumoView> items) {
    }

    private record AgendamentoResumoView(UUID id, String data, String hora, String cliente, String servico, String barbeiro, String status, String telefoneCliente, String whatsappUrl) {
    }

}