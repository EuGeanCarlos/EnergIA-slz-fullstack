package br.com.energia.energiaslz.controller;

import br.com.energia.energiaslz.dto.RelatorioDTO;
import br.com.energia.energiaslz.service.RelatorioService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/relatorios")
@CrossOrigin(origins = "*")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/{usuarioId}")
    public RelatorioDTO gerarRelatorio(@PathVariable String usuarioId) {
        return relatorioService.gerarRelatorioPorUsuario(usuarioId);
    }

    @GetMapping
    public String status() {
        return "Relatórios ativo";
    }
}
