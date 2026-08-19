package com.paola.pacientes.controller;

import com.paola.commons.controller.CommonController;
import com.paola.commons.dto.medico.MedicoResponse;
import com.paola.commons.dto.paciente.PacienteRequest;
import com.paola.commons.dto.paciente.PacienteResponse;
import com.paola.pacientes.services.PacienteService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Validated

public class PacienteController extends CommonController<PacienteRequest, PacienteResponse, PacienteService>{

    public PacienteController (PacienteService service){

        super(service);
    }

    @GetMapping("/id-paciente/{id}")
    public ResponseEntity<PacienteResponse> obtenerPacientePorIdSinEstado(
            @PathVariable @Positive(message = "El ID debe ser positivo") Long id)
    {
        return  ResponseEntity.ok(service.obtenerPacientePorIdSinEstado(id));
    }
}
