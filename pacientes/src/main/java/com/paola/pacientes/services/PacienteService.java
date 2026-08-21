package com.paola.pacientes.services;

import com.paola.commons.dto.paciente.PacienteRequest;
import com.paola.commons.dto.paciente.PacienteResponse;
import com.paola.commons.services.CrudService;

public interface PacienteService extends CrudService<PacienteRequest, PacienteResponse> {
    PacienteResponse obtenerPacientePorIdSinEstado(Long id);

}
