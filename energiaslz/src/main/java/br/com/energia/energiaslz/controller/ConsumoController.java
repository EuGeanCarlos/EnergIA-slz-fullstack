package br.com.energia.energiaslz.controller;

import br.com.energia.energiaslz.model.Consumo;
import br.com.energia.energiaslz.service.ConsumoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consumos")
@CrossOrigin(origins = "*")
public class ConsumoController {

    private final ConsumoService consumoService;

    public ConsumoController(ConsumoService consumoService) {
        this.consumoService = consumoService;
    }

    /**
     * ✅ ROTA COMPATÍVEL COM O FRONT ATUAL
     * Front envia JSON com usuarioId no body:
     * { usuarioId: "...", nomeAparelho: "...", potencia: ..., horasUso: ..., quantidade: ... }
     */
    @PostMapping
    public Consumo salvar(@Valid @RequestBody Consumo consumo) {
        // garante que usuário existe no payload
        if (consumo.getUsuarioId() == null || consumo.getUsuarioId().isBlank()) {
            throw new IllegalArgumentException("usuarioId é obrigatório no corpo da requisição.");
        }
        return consumoService.salvarConsumo(consumo);
    }

    /**
     * ✅ Mantém sua rota antiga também (útil para debug / postman)
     * POST /api/consumos/{usuarioId}
     */
    @PostMapping("/{usuarioId}")
    public Consumo salvarPorPath(
            @PathVariable String usuarioId,
            @Valid @RequestBody Consumo consumo
    ) {
        consumo.setUsuarioId(usuarioId);
        return consumoService.salvarConsumo(consumo);
    }

    /**
     * Lista todos os consumos
     */
    @GetMapping
    public List<Consumo> listarConsumos() {
        return consumoService.listarConsumos();
    }

    /**
     * Lista consumos por usuário (útil para debug)
     */
    @GetMapping("/usuario/{usuarioId}")
    public List<Consumo> listarPorUsuario(@PathVariable String usuarioId) {
        return consumoService.listarPorUsuario(usuarioId);
    }
}
