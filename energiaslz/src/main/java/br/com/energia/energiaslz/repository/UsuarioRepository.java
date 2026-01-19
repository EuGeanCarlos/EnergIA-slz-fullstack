package br.com.energia.energiaslz.repository;

import br.com.energia.energiaslz.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
}
