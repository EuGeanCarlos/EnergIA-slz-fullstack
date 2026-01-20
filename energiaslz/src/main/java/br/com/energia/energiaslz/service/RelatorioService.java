package br.com.energia.energiaslz.service;

import br.com.energia.energiaslz.dto.RelatorioDTO;
import br.com.energia.energiaslz.model.Consumo;
import br.com.energia.energiaslz.repository.ConsumoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelatorioService {

    private final ConsumoRepository consumoRepository;

    // Tarifa média simulada (R$/kWh)
    private static final double TARIFA_MEDIA = 0.75;

    public RelatorioService(ConsumoRepository consumoRepository) {
        this.consumoRepository = consumoRepository;
    }

    public RelatorioDTO gerarRelatorioPorUsuario(String usuarioId) {

        List<Consumo> consumos = consumoRepository.findByUsuarioId(usuarioId);

        double consumoMensalTotal = 0.0;

        for (Consumo consumo : consumos) {

            // Fórmula:
            // (potência em W * horas/dia * quantidade * 30) / 1000 = kWh/mês
            double consumoMensalAparelho =
                    (consumo.getPotencia() * consumo.getHorasUso() * consumo.getQuantidade() * 30) / 1000;

            consumoMensalTotal += consumoMensalAparelho;
        }

        double custoEstimado = consumoMensalTotal * TARIFA_MEDIA;

        return new RelatorioDTO(consumoMensalTotal, custoEstimado);
    }
}
