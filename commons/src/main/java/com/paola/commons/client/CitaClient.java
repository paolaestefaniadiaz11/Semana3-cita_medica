package com.paola.commons.client;

import com.paola.commons.dto.paciente.PacienteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "citas")
public interface CitaClient {

    @GetMapping("/paciente/{idPaciente}/tiene-citas-activas")
    Boolean tieneCitasActivas(@PathVariable("idPaciente") Long idPaciente);
}
