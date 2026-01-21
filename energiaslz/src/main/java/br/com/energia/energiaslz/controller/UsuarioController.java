package br.com.energia.energiaslz.controller;

import br.com.energia.energiaslz.model.Usuario;
import br.com.energia.energiaslz.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    public Usuario criar(@RequestBody Usuario usuario) {
        return service.salvar(usuario);
    }

    @GetMapping
    public List<Usuario> listar() {
        return service.listar();
    }

    // ✅ NOVO: buscar por ID (IA vai usar)
    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable String id) {
        return service.buscarPorId(id);
    }
}
