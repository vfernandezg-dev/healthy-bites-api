package com.healthybites.api;

import com.healthybites.dto.MetaDTO;
import com.healthybites.model.entity.Meta;
import com.healthybites.service.AdminMetaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/meta")
public class AdminMetaController {

    private final AdminMetaService adminMetaService;

    @GetMapping
    public ResponseEntity<List<MetaDTO>> getAllMeta() {
        List<MetaDTO> metas = adminMetaService.getAll();
        return new ResponseEntity<>(metas, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<MetaDTO>> paginateMetas(
            @PageableDefault(size = 5, sort = "nombre") Pageable pageable) {
        Page<MetaDTO> metas = adminMetaService.paginate(pageable);
        return new ResponseEntity<>(metas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetaDTO> getMetaById(@PathVariable("id") Integer id) {
        MetaDTO meta = adminMetaService.findById(id);
        return new ResponseEntity<>(meta, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<MetaDTO> createMeta(@Valid @RequestBody MetaDTO metaDTO) {
        MetaDTO newMeta = adminMetaService.create(metaDTO);
        return new ResponseEntity<>(newMeta, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetaDTO> updateMeta(@PathVariable("id") Integer id, @Valid @RequestBody MetaDTO metaDTO) {
        MetaDTO updateMeta = adminMetaService.update(id, metaDTO);
        return new ResponseEntity<MetaDTO>(updateMeta, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeta(@PathVariable("id") Integer id) {
        adminMetaService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/recomendacion")
    public ResponseEntity<String> calcularRecomendacion(@Valid @RequestBody MetaDTO metaDTO) {
        String recomendacion = adminMetaService.calcularRecomendacion(metaDTO);
        return new ResponseEntity<>(recomendacion, HttpStatus.OK);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<MetaDTO>> getMetasByClienteId(@PathVariable("clienteId") Integer clienteId) {
        List<MetaDTO> metas = adminMetaService.findMetasByClienteId(clienteId);
        return new ResponseEntity<>(metas, HttpStatus.OK);
    }
}
