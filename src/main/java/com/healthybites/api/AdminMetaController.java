package com.healthybites.api;

import com.healthybites.dto.ClienteDetailsDTO;
import com.healthybites.dto.MetaCreateDTO;
import com.healthybites.dto.MetaDTO;
import com.healthybites.model.enums.EstadoMeta;
import com.healthybites.service.AdminMetaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/meta")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMetaController {

    private final AdminMetaService adminMetaService;

    @GetMapping
    public ResponseEntity<List<MetaDTO>> getAllMeta() {
        return new ResponseEntity<>(adminMetaService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<MetaDTO>> paginateMetas(
            @PageableDefault(size = 5, sort = "nombre") Pageable pageable) {
        return new ResponseEntity<>(adminMetaService.paginate(pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetaDTO> getMetaById(@PathVariable("id") Integer id) {
        return new ResponseEntity<>(adminMetaService.findById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<MetaDTO> createMeta(@Valid @RequestBody MetaCreateDTO metaCreateDTO) {
        return new ResponseEntity<>(adminMetaService.create(metaCreateDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetaDTO> updateMeta(@PathVariable("id") Integer id,
                                              @Valid @RequestBody MetaCreateDTO metaCreateDTO) {
        return new ResponseEntity<>(adminMetaService.update(id, metaCreateDTO), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeta(@PathVariable("id") Integer id) {
        adminMetaService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/recomendacion")
    public ResponseEntity<String> calcularRecomendacion(@Valid @RequestBody MetaCreateDTO metaCreateDTO) {
        return new ResponseEntity<>(adminMetaService.calcularRecomendacion(metaCreateDTO), HttpStatus.OK);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<MetaDTO>> getMetasByClienteId(@PathVariable("clienteId") Integer clienteId) {
        return new ResponseEntity<>(adminMetaService.findMetasByClienteId(clienteId), HttpStatus.OK);
    }

    @PutMapping("/{id}/objetivos")
    public ResponseEntity<MetaDTO> actualizarObjetivosSalud(@PathVariable("id") Integer id,
                                                            @Valid @RequestBody MetaCreateDTO metaCreateDTO) {
        return new ResponseEntity<>(adminMetaService.actualizarObjetivosSalud(id, metaCreateDTO), HttpStatus.OK);
    }

    // Body: "ACTIVE" o "COMPLETED"
    @PutMapping("/{id}/estado")
    public ResponseEntity<MetaDTO> actualizarEstadoMeta(@PathVariable("id") Integer id,
                                                        @RequestBody EstadoMeta nuevoEstado) {
        return new ResponseEntity<>(adminMetaService.actualizacionEstadoMeta(id, nuevoEstado), HttpStatus.OK);
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<EstadoMeta>> getHistorialById(@PathVariable("id") Integer id) {
        return new ResponseEntity<>(adminMetaService.getHistorialById(id), HttpStatus.OK);
    }

    @GetMapping("/{id}/perfil")
    public ResponseEntity<ClienteDetailsDTO> getPerfilCliente(@PathVariable("id") Integer id) {
        return new ResponseEntity<>(adminMetaService.getPerfilClienteByMetaId(id), HttpStatus.OK);
    }
}
