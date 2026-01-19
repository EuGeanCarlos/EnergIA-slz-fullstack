package br.com.energia.energiaslz.repository;

import br.com.energia.energiaslz.model.Consumo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConsumoRepository extends MongoRepository<Consumo, String> {
}
