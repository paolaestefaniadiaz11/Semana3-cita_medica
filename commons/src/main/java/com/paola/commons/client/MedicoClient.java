package com.paola.commons.client;

import com.paola.commons.dto.medico.MedicoResponse;
import com.paola.commons.dto.paciente.PacienteResponse;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "medicos")
public interface MedicoClient {

    @GetMapping("/{id}")
    MedicoResponse obtenerMedicoActivoPorId(@PathVariable Long id);

    @GetMapping("/id-medico/{id}")
    MedicoResponse obtenerMedicoPorIdSinEstado(@PathVariable Long id);

    @PutMapping("/{idMedico}/disponibilidad-interna/{idDisponibilidad}")
    void actualizarDisponibilidadMedico(
            @PathVariable("idMedico") Long idMedico,
            @PathVariable("idDisponibilidad") Long idDisponibilidad
    );

    @PutMapping("/{idMedico}/disponibilidad-interna/{idDisponibilidad}")
    public ResponseEntity<Void> sincronizarDisponibilidadInterna(
            @PathVariable @Positive Long idMedico,
            @PathVariable @Positive Long idDisponibilidad
    );
}
