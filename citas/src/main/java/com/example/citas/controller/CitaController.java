package com.example.citas.controller;

import com.example.citas.dto.CitaRequest;
import com.example.citas.dto.CitaResponse;
import com.example.citas.service.CitaService;
import com.paola.commons.controller.CommonController;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class CitaController extends CommonController<CitaRequest, CitaResponse, CitaService> {

    public  CitaController(CitaService service){
        super(service);
    }

    @PatchMapping("/{idCita}/estado/{idEstado}")
    public ResponseEntity<Void> actualizarEstadoCita(
            @PathVariable @Positive (message = "El idCita debe ser positivo") Long idCita,
            @PathVariable @Positive (message = "El idEstado debe ser positivo") Long idEstado){
        service.actualizarEstadoCita(idCita,idEstado);
        return ResponseEntity.noContent().build();
    }

}
