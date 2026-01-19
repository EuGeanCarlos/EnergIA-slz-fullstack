package br.com.energia.energiaslz.service;

import br.com.energia.energiaslz.model.Consumo;
import br.com.energia.energiaslz.repository.ConsumoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsumoService {

    private final ConsumoRepository consumoRepository;

    public ConsumoService(ConsumoRepository consumoRepository) {
        this.consumoRepository = consumoRepository;
    }

    public Consumo salvarConsumo(Consumo consumo) {
        return consumoRepository.save(consumo);
    }

    public List<Consumo> listarConsumos() {
        return consumoRepository.findAll();
    }
}
