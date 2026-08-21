package com.paola.commons.client;
import com.paola.commons.dto.cita.CitaResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "citas")
public interface CitaClient {

    @GetMapping("/paciente/{idPaciente}/tiene-citas-activas")
    Boolean tieneCitasActivasPaciente(@PathVariable("idPaciente") Long idPaciente);

    @GetMapping("/medico/{idMedico}/tiene-citas-activas")
    Boolean tieneCitasActivasMedico(@PathVariable("idMedico") Long idMedico);


}

