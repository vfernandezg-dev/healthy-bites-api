package com.healthybites.api;

import com.healthybites.dto.GrupoDTO;
import com.healthybites.service.UnirseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/unirse")
@PreAuthorize("hasRole('CLIENTE')")
public class UnirseController {

    private final UnirseService unirseService;

    // Method to add a client to a group
    //http://localhost:8080/unirse/1/add-grupo?groupId=1
    @PostMapping("/{clientId}/add-grupo")
    public ResponseEntity<GrupoDTO> addClientToGroup(@PathVariable Integer clientId,
                                                     @RequestParam Integer groupId) {
        GrupoDTO grupoDTO = unirseService.addClientToGroup(clientId, groupId);
        return new ResponseEntity<>(grupoDTO, HttpStatus.CREATED);
    }

    // Method to remove a client from a group
    //http://localhost:8080/unirse/1/remove-grupo/1
    @DeleteMapping("/{clientId}/remove-grupo/{groupId}")
    public ResponseEntity<Void> removeClientFromGroup(@PathVariable Integer clientId,
                                                      @PathVariable Integer groupId) {
        unirseService.removeClientFromGroup(clientId, groupId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
