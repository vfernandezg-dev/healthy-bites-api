package com.healthybites.api;

import com.healthybites.model.entity.Racha;
import com.healthybites.service.AdminRachaService;
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
        public ResponseEntity<List<Racha>> list() {
            List<Racha> rachas = adminRachaService.getAll();
            return new ResponseEntity<>(rachas, HttpStatus.OK);
        }

        @GetMapping("/page")
        public ResponseEntity<Page<Racha>> paginateRachas(
                @PageableDefault(size = 5, sort = "diasConsecutivos") Pageable pageable) {
            Page<Racha> rachas = adminRachaService.paginate(pageable);
            return new ResponseEntity<Page<Racha>>(rachas, HttpStatus.OK);
        }

        @GetMapping("/{id}")
        public ResponseEntity<Racha> getRachaById(@PathVariable("id") Integer id) {
            Racha racha = adminRachaService.findById(id);
            return new ResponseEntity<>(racha, HttpStatus.OK);
        }

        @PostMapping
        public ResponseEntity<Racha> create(@RequestBody Racha racha) {
            Racha newRacha = adminRachaService.create(racha);
            return new ResponseEntity<Racha>(newRacha, HttpStatus.CREATED);
        }

        @PutMapping("/{id}")
        public ResponseEntity<Racha> updateRacha(@PathVariable("id") Integer id, @RequestBody Racha racha) {
            Racha updateRacha = adminRachaService.update(id, racha);
            return new ResponseEntity<Racha>(updateRacha, HttpStatus.OK);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Racha> deleteRacha(@PathVariable("id") Integer id) {
            adminRachaService.delete(id);
            return new ResponseEntity<Racha>(HttpStatus.NO_CONTENT);
        }
}
