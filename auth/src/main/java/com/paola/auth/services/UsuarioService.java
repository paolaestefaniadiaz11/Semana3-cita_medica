package com.paola.auth.services;

import java.util.Set;

import com.paola.auth.dto.UsuarioRequest;
import com.paola.auth.dto.UsuarioResponse;

public interface UsuarioService {

    Set<UsuarioResponse> listar();

    UsuarioResponse registrar(UsuarioRequest request);

    UsuarioResponse eliminar(String username);
}
