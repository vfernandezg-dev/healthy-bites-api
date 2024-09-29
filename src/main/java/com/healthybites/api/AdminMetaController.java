package com.healthybites.api;

import com.healthybites.model.entity.Meta;
import com.healthybites.service.AdminMetaService;
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
    public ResponseEntity<List<Meta>> getAllMeta() {
        List<Meta> metas = adminMetaService.getAll();
        return new ResponseEntity<List<Meta>>(metas, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Meta>> paginateMetas(
            @PageableDefault(size = 5, sort = "nombre") Pageable pageable) {
        Page<Meta> metas = adminMetaService.paginate(pageable);
        return new ResponseEntity<Page<Meta>>(metas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Meta> getMetaById(@PathVariable("id") Integer id) {
        Meta meta = adminMetaService.findById(id);
        return new ResponseEntity<Meta>(meta, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Meta> createMeta(@RequestBody Meta meta) {
        Meta newMeta = adminMetaService.create(meta);
        return new ResponseEntity<Meta>(newMeta, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Meta> updateMeta(@PathVariable("id") Integer id, @RequestBody Meta meta) {
        Meta updateMeta = adminMetaService.update(id, meta);
        return new ResponseEntity<Meta>(updateMeta, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Meta> deleteMeta(@PathVariable("id") Integer id) {
        adminMetaService.delete(id);
        return new ResponseEntity<Meta>(HttpStatus.NO_CONTENT);
    }

}
