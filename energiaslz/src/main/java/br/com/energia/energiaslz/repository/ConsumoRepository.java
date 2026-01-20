package br.com.energia.energiaslz.repository;

import br.com.energia.energiaslz.model.Consumo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConsumoRepository extends MongoRepository<Consumo, String> {

    List<Consumo> findByUsuarioId(String usuarioId);

}
