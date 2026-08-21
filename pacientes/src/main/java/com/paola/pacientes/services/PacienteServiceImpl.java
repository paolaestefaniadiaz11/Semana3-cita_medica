package com.paola.pacientes.services;

import com.paola.commons.client.CitaClient;
import com.paola.commons.dto.paciente.PacienteRequest;
import com.paola.commons.dto.paciente.PacienteResponse;
import com.paola.commons.enums.EstadoRegistro;
import com.paola.commons.exceptions.RecursoNoEncontradoException;
import com.paola.pacientes.entity.Paciente;
import com.paola.pacientes.mappers.PacienteMapper;
import com.paola.pacientes.repository.PacienteRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional
@Slf4j

public class PacienteServiceImpl implements PacienteService{

    private final PacienteRepository pacienteRepository;

    private final PacienteMapper pacienteMapper;

    private final CitaClient citaClient;


    @Override
    public List<PacienteResponse> listar() {
        log.info("Listando pcientes con estado ACTIVO");
        return pacienteRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO)
                .stream()
                .map(pacienteMapper::entidadAResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponse obtenerPorId(Long id) {
        return pacienteMapper.entidadAResponse(obtenerPacienteActivoOException(id));
    }

    @Override
    public PacienteResponse registrar(PacienteRequest request) {
        log.info("Registrando nuevo paciente: {}", request.nombre());

        validarDatosUnicos(request);

        Paciente paciente = pacienteMapper.requestAEntidad(request);

        paciente.calcularImc();
        paciente.generarNumeroExpediente();

        paciente = pacienteRepository.save(paciente);

        return pacienteMapper.entidadAResponse(paciente);
    }

    @Override
    @Transactional
    public PacienteResponse actualizar(PacienteRequest request, Long id) {
        log.info("Actualizar paciente con id: {}", id);

        Paciente paciente = obtenerPacienteActivoOException(id);

        validarSinCitasActivas(id);

        validarCambiosUnicos(request, id);

        paciente.actualizar(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno(),
                request.edad(),
                request.peso(),
                request.estatura(),
                request.email(),
                request.telefono(),
                request.direccion()
        );

        log.info("Paciente con id: {} actualizado exitosamente", id);
        return pacienteMapper.entidadAResponse(paciente);

    }

    @Override
    public void eliminar(Long id) {
        Paciente paciente = obtenerPacienteActivoOException(id);

        validarSinCitasActivas(id);

        log.info("Eliminando paciente con id: {}",id);

        paciente.eliminar();
    }

    @Override
    public PacienteResponse obtenerPacientePorIdSinEstado(Long id) {
        log.info("Listando paciente sin estado con id:{}",id);

        return pacienteMapper.entidadAResponse(pacienteRepository.findById(id)
                .orElseThrow(()-> new RecursoNoEncontradoException("Recurso no encontrado con id: "+id)));

    }


    private Paciente obtenerPacienteActivoOException(Long id){
        log.info("Listando pacientes con estado ACTIVO");

        return pacienteRepository.findByIdAndEstadoRegistro(id,EstadoRegistro.ACTIVO)
                .orElseThrow(()-> new RecursoNoEncontradoException("Paciente Activo no encontrado con id:"+id));

    }

    private void validarDatosUnicos(PacienteRequest request){
        log.info("Validando email unico...");

        if (pacienteRepository.existsByEmailIgnoreCaseAndEstadoRegistro(
                request.email().trim(),EstadoRegistro.ACTIVO))

            throw new IllegalArgumentException("Ya existe un paciente activo registrado con el email: "+request.email());

        log.info("Validando telefono unico...");

        if (pacienteRepository.existsByTelefonoAndEstadoRegistro(request.telefono().trim(), EstadoRegistro.ACTIVO))

            throw new IllegalArgumentException("Ya existe un paciente activo registrado con el telefono"+request.telefono());

    }

    private void validarCambiosUnicos(PacienteRequest request,Long id){
        log.info("Validando cambio en email unico...");

        if (pacienteRepository.existsByEmailIgnoreCaseAndEstadoRegistroAndIdNot(request.email().trim(),EstadoRegistro.ACTIVO,id))

            throw new IllegalArgumentException("Ya existe un paciente activo registrado con el email: "+request.email());

        log.info("Validando cambio en telefono unico...");

        if (pacienteRepository.existsByTelefonoAndEstadoRegistroAndIdNot(request.telefono().trim(),EstadoRegistro.ACTIVO,id))

            throw new IllegalArgumentException("Ya existe un medico activo registrado con el email: "+request.telefono());

    }

    private void validarSinCitasActivas(Long idPaciente) {
        log.info("Validando que el paciente tenga citas en CONFIRMADA O EN_CURSO...");
        Boolean tieneCitas = citaClient.tieneCitasActivasPaciente(idPaciente);

        if (Boolean.TRUE.equals(tieneCitas))
            throw new IllegalArgumentException("No se puede actualizar ni eliminar el paciente porque tiene citas en estado CONFIRMADA o EN_CURSO.");

    }

}
