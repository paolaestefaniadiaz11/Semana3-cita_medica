package com.paola.medicos.controller;

import com.paola.commons.controller.CommonController;
import com.paola.commons.dto.medico.MedicoRequest;
import com.paola.commons.dto.medico.MedicoResponse;
import com.paola.medicos.services.MedicoService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Validated
public class MedicoController extends CommonController<MedicoRequest, MedicoResponse, MedicoService> {

    public MedicoController (MedicoService service){

        super(service);
    }

    @GetMapping("/id-medico/{id}")
    public ResponseEntity<MedicoResponse> obtenerMedicoPorIdSinEstado(
        @PathVariable @Positive (message = "El ID debe ser positivo") Long id)
        {
            return  ResponseEntity.ok(service.obtenerMedicoPorIdSinEstado(id));
        }


    @PutMapping("/{idMedico}/disponibilidad/{idDisponibilidad}")
    public ResponseEntity<Void> actualizarDisponibilidadMedico(
            @PathVariable("idMedico") Long idMedico,
            @PathVariable("idDisponibilidad") Long idDisponibilidad) {
        service.actualizarDisponibilidadMedico(idMedico, idDisponibilidad);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{idMedico}/disponibilidad-interna/{idDisponibilidad}")
    public ResponseEntity<Void> sincronizarDisponibilidadInterna(
            @PathVariable("idMedico") Long idMedico,
            @PathVariable("idDisponibilidad") Long idDisponibilidad) {
        service.sincronizarDisponibilidadInterna(idMedico, idDisponibilidad);
        return ResponseEntity.noContent().build();
    }




}


