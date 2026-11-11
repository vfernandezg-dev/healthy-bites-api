package com.healthybites.service;

import com.healthybites.dto.ClienteMetaDTO;

public interface ClienteService {
    // Actualiza los datos del perfil del cliente (y opcionalmente una de sus metas) en una sola operacion.
    // El registro y el login de clientes viven en UsuarioService / AuthController.
    ClienteMetaDTO actualizarClienteConMeta(Integer clienteId, ClienteMetaDTO clienteMetaDTO);
}
