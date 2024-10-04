
package com.healthybites.service;

import com.healthybites.dto.ClienteDTO;
import com.healthybites.model.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AdminClienteService {
    List<ClienteDTO> getAll();
    Page<ClienteDTO> paginate(Pageable pageable);
    ClienteDTO findById(Integer id);
    ClienteDTO create(ClienteDTO cliente);
    ClienteDTO update(Integer id, ClienteDTO updateClienteDTO);
    void delete(Integer id);

    interface AdminRecompensaService {
    }
}
