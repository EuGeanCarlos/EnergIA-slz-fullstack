package br.com.energia.energiaslz.service;

import br.com.energia.energiaslz.dto.RelatorioDTO;
import br.com.energia.energiaslz.model.Consumo;
import br.com.energia.energiaslz.model.Usuario;
import br.com.energia.energiaslz.repository.ConsumoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelatorioService {

    private final ConsumoRepository consumoRepository;
    private final UsuarioService usuarioService;

    // fallback caso o usuário não tenha tarifa cadastrada
    private static final double TARIFA_FALLBACK = 0.75;

    public RelatorioService(ConsumoRepository consumoRepository, UsuarioService usuarioService) {
        this.consumoRepository = consumoRepository;
        this.usuarioService = usuarioService;
    }

    public RelatorioDTO gerarRelatorioPorUsuario(String usuarioId) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);

        double tarifa = (usuario.getTarifa() != null && usuario.getTarifa() > 0)
                ? usuario.getTarifa()
                : TARIFA_FALLBACK;

        List<Consumo> consumos = consumoRepository.findByUsuarioId(usuarioId);

        double consumoMensalTotal = 0.0;

        for (Consumo consumo : consumos) {
            if (consumo.getPotencia() == null ||
                    consumo.getHorasUso() == null ||
                    consumo.getQuantidade() == null) {
                continue;
            }

            double consumoMensalAparelho =
                    (consumo.getPotencia() * consumo.getHorasUso() * consumo.getQuantidade() * 30) / 1000;

            consumoMensalTotal += consumoMensalAparelho;
        }

        double custoEstimado = consumoMensalTotal * tarifa;

        return new RelatorioDTO(consumoMensalTotal, custoEstimado);
    }
}
