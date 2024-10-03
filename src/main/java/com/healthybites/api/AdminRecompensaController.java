package com.healthybites.api;

import com.healthybites.model.entity.Recompensa;
import com.healthybites.service.AdminRecompensaService;
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
@RequestMapping("/admin/recompensa")
public class AdminRecompensaController {

    private final AdminRecompensaService adminRecompensaService;

    @GetMapping
    public ResponseEntity<List<Recompensa>> getAllRecompensa() {
        List<Recompensa> recompensas = adminRecompensaService.getAll();
        return new ResponseEntity<List<Recompensa>>(recompensas, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Recompensa>> paginateRecompensas(
            @PageableDefault(size = 5, sort = "nombre") Pageable pageable) {
        Page<Recompensa> recompensas = adminRecompensaService.paginate(pageable);
        return new ResponseEntity<Page<Recompensa>>(recompensas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recompensa> getRecompensaById(@PathVariable("id") Integer id) {
        Recompensa recompensa = adminRecompensaService.findById(id);
        return new ResponseEntity<Recompensa>(recompensa, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Recompensa> createRecompensa(@RequestBody Recompensa recompensa) {
        Recompensa newRecompensa = adminRecompensaService.create(recompensa);
        return new ResponseEntity<Recompensa>(newRecompensa, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recompensa> updateRecompensa(@PathVariable("id") Integer id, @RequestBody Recompensa recompensa) {
        Recompensa updateRecompensa = adminRecompensaService.update(id, recompensa);
        return new ResponseEntity<Recompensa>(updateRecompensa, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Recompensa> deleteRecompensa(@PathVariable("id") Integer id) {
        adminRecompensaService.delete(id);
        return new ResponseEntity<Recompensa>(HttpStatus.NO_CONTENT);
    }
}
