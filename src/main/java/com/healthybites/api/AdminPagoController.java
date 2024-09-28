package com.healthybites.api;

import com.healthybites.model.entity.Pago;
import com.healthybites.service.AdminPagoService;
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
@RequestMapping("/admin/pago")
public class AdminPagoController {

    private final AdminPagoService adminPagoService;

    @GetMapping
    public ResponseEntity<List<Pago>> getAllPago() {
        List<Pago> pagos = adminPagoService.getAll();
        return new ResponseEntity<List<Pago>>(pagos, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Pago>> paginatePagos(
            @PageableDefault(size = 5, sort = "estado_pago") Pageable pageable) {
        Page<Pago> pagos = adminPagoService.paginate(pageable);
        return new ResponseEntity<Page<Pago>>(pagos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pago> getPagoById(@PathVariable("id") Integer id) {
        Pago pago = adminPagoService.findById(id);
        return new ResponseEntity<Pago>(pago, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Pago> createPago(@RequestBody Pago pago) {
        Pago newPago = adminPagoService.create(pago);
        return new ResponseEntity<Pago>(newPago, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pago> updatePago(@PathVariable("id") Integer id, @RequestBody Pago pago) {
        Pago updatePago = adminPagoService.update(id, pago);
        return new ResponseEntity<Pago>(updatePago, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Pago> deletePago(@PathVariable("id") Integer id) {
        adminPagoService.delete(id);
        return new ResponseEntity<Pago>(HttpStatus.NO_CONTENT);
    }
}
