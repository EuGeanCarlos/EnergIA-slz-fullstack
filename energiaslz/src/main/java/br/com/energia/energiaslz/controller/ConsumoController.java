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
     * Salva um consumo VINCULADO a um usuário
     */
    @PostMapping("/{usuarioId}")
    public Consumo salvar(
            @PathVariable String usuarioId,
            @Valid @RequestBody Consumo consumo
    ) {
        consumo.setUsuarioId(usuarioId); // 🔗 vínculo garantido no backend
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
