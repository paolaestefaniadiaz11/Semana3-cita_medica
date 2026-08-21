package com.paola.medicos.services;

import com.paola.commons.dto.medico.MedicoRequest;
import com.paola.commons.dto.medico.MedicoResponse;
import com.paola.commons.services.CrudService;

public interface MedicoService extends CrudService<MedicoRequest, MedicoResponse> {

    MedicoResponse obtenerMedicoPorIdSinEstado(Long id);

    void actualizarDisponibilidadMedico(Long idMedico, Long idDisponibilidad);

    void sincronizarDisponibilidadInterna(Long idMedico, Long idDisponibilidad);
}
