package com.healthybites.api;

import com.healthybites.dto.NutricionistaDTO;
import com.healthybites.dto.RachaDTO;
import com.healthybites.model.entity.Racha;
import com.healthybites.service.AdminRachaService;
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
@RequestMapping("/admin/racha")
public class AdminRachaController {

    private final AdminRachaService adminRachaService;

    @GetMapping
    public ResponseEntity<List<RachaDTO>> list() {
        List<RachaDTO> rachas = adminRachaService.getAll();
        return new ResponseEntity<>(rachas, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<RachaDTO>> paginateRachas(
            @PageableDefault(size = 5, sort = "diasConsecutivos") Pageable pageable) {
        Page<RachaDTO> rachas = adminRachaService.paginate(pageable);
        return new ResponseEntity<>(rachas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RachaDTO> getRachaById(@PathVariable("id") Integer id) {
      RachaDTO rachas = adminRachaService.findById(id);
        return new ResponseEntity<>(rachas, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<RachaDTO> create(@Valid @RequestBody RachaDTO rachaDTO) {
        RachaDTO newRacha = adminRachaService.create(rachaDTO);
        return new ResponseEntity<>(newRacha, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RachaDTO> updateRacha(@PathVariable("id") Integer id, @Valid @RequestBody RachaDTO rachaDTO) {
        RachaDTO updateRacha = adminRachaService.update(id, rachaDTO);
        return new ResponseEntity<>(updateRacha, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRacha(@PathVariable("id") Integer id) {
        adminRachaService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @PostMapping("/{rachaId}/asignar/{clienteId}")
    public ResponseEntity<RachaDTO> asignarRacha(
            @PathVariable Integer rachaId,
            @PathVariable Integer clienteId) {
        RachaDTO rachaAsignada = adminRachaService.asignarRachaACliente(rachaId, clienteId);
        return ResponseEntity.ok(rachaAsignada);
    }


}
