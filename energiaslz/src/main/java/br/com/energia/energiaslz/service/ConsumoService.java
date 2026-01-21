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

    // ✅ Mantém o nome que seu Controller está chamando
    public List<Consumo> listarConsumos() {
        return consumoRepository.findAll();
    }

    // ✅ Mantém também o nome antigo (caso outro lugar use)
    public List<Consumo> listarTodos() {
        return consumoRepository.findAll();
    }

    public List<Consumo> listarPorUsuario(String usuarioId) {
        return consumoRepository.findByUsuarioId(usuarioId);
    }
}
