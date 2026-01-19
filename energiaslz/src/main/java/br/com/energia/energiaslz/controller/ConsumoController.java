package br.com.energia.energiaslz.controller;

import br.com.energia.energiaslz.model.Consumo;
import br.com.energia.energiaslz.service.ConsumoService;
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

    @PostMapping
    public Consumo salvarConsumo(@RequestBody Consumo consumo) {
        return consumoService.salvarConsumo(consumo);
    }

    @GetMapping
    public List<Consumo> listarConsumos() {
        return consumoService.listarConsumos();
    }
}
