package com.healthybites.service;

import com.healthybites.model.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AdminClienteService {
    List<Cliente> getAll();
    Page<Cliente> paginate(Pageable pageable);
    Cliente findById(Integer id);
    Cliente create(Cliente cliente);
    Cliente update(Integer id, Cliente updateCliente);
    void delete(Integer id);
}
