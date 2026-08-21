package com.example.citas.controller;

import com.example.citas.dto.CitaRequest;
import com.example.citas.dto.CitaResponse;
import com.example.citas.service.CitaService;
import com.paola.commons.controller.CommonController;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Validated
public class CitaController extends CommonController<CitaRequest, CitaResponse, CitaService> {

    public  CitaController(CitaService service){
        super(service);
    }


    @GetMapping("/paciente/{idPaciente}/tiene-citas-activas")
    public ResponseEntity<Boolean> tieneCitasActivasPaciente(@PathVariable("idPaciente") Long idPaciente) {
        boolean activas = service.tieneCitasActivasPaciente(idPaciente);
        return ResponseEntity.ok(activas);
    }

    @PatchMapping("/{idCita}/estado/{idEstado}")
    public ResponseEntity<Void> actualizarEstadoCita(
            @PathVariable @Positive (message = "El idCita debe ser positivo") Long idCita,
            @PathVariable @Positive (message = "El idEstado debe ser positivo") Long idEstado){
        service.actualizarEstadoCita(idCita,idEstado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/medico/{idMedico}/disponible")
    public ResponseEntity<Boolean> verificarDisponibilidadMedico(@PathVariable Long idMedico) {
        boolean activas = service.tieneCitasActivasPaciente(idMedico);
        return ResponseEntity.ok(activas);
    }

    @GetMapping("/medico/{idMedico}/tiene-citas-activas")
    public ResponseEntity<Boolean> tieneCitasActivasMedico(@PathVariable Long idMedico) {
        boolean tieneCitas = service.tieneCitasActivasMedico(idMedico);
        return ResponseEntity.ok(tieneCitas);
    }

}
