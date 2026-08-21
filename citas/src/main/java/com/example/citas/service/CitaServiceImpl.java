package com.example.citas.service;

import com.example.citas.dto.CitaRequest;
import com.example.citas.dto.CitaResponse;
import com.example.citas.entity.Cita;
import com.example.citas.enums.EstadoCita;
import com.example.citas.mapper.CitaMapper;
import com.example.citas.repository.CitaRepository;
import com.paola.commons.client.CitaClient;
import com.paola.commons.client.MedicoClient;
import com.paola.commons.client.PacienteClient;
import com.paola.commons.dto.medico.MedicoResponse;
import com.paola.commons.dto.paciente.PacienteResponse;
import com.paola.commons.enums.EstadoRegistro;
import com.paola.commons.exceptions.RecursoNoEncontradoException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class CitaServiceImpl implements CitaService{

    private final CitaRepository citaRepository;
    private final CitaMapper citaMapper;
    private final MedicoClient medicoClient;
    private final CitaClient citaClient;
    private final PacienteClient pacienteClient;

    //Constantes - enum Estado Cita
    private static final Long ID_DISPONIBILIDAD_DISPONIBLE = 1L;
    private static final Long ID_DISPONIBILIDAD_EN_CONSULTA = 2L;
    private static final Long ID_DISPONIBILIDAD_AGENDADO = 5L;


    private static final List<EstadoCita> ESTADOS_PERMITIDOS_PCEC = List.of(
            EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.EN_CURSO
    );

    private static final List<EstadoCita> ESTADOS_PERMITIDOS_CEC =
            List.of(EstadoCita.CONFIRMADA, EstadoCita.EN_CURSO);


    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> listar() {

        log.info("Listando todas las citas con estado ACTIVO");

        return citaRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO)
                .stream()
                .map(cita -> citaMapper.entidadAResponse(
                        cita,
                        obtenerPacienteSinEstado(cita.getIdPaciente()),
                        obtenerMedicoSinEstado(cita.getIdMedico())
                ))
                .toList();
    }

    @Override
    public CitaResponse obtenerPorId(Long id) {

        Cita cita =  obtenerCitaOException(id);

        return citaMapper.entidadAResponse
                (cita,
                        obtenerPacienteSinEstado(cita.getIdPaciente()),
                        obtenerMedicoSinEstado(cita.getIdMedico()));
    }


    @Override
    @Transactional
    public CitaResponse registrar(CitaRequest request) {
        log.info("Registrando nueva cita");

        MedicoResponse medico = obtenerMedicoActivo(request.idMedico());
        PacienteResponse paciente = obtenerPacienteActivo(request.idPaciente());

        validarMedicoDisponible(request.idMedico());
        validarPacienteDisponible(request.idPaciente());

        Cita cita = Cita.crear(
                request.idPaciente(),
                request.idMedico(),
                request.fechaCita(),
                request.sintomas()
        );

        citaRepository.save(cita);

        sincronizarDisponibilidadMedico(cita.getId(), cita.getIdMedico(), cita.getEstadoCita());

        log.info("Cita registrada exitosamente con id: {}", cita.getId());

        return citaMapper.entidadAResponse(
                cita,
                paciente,
                medico
        );
    }

    @Override
    @Transactional
    public CitaResponse actualizar(CitaRequest request, Long idCita) {
        log.info("Iniciando actualización de la cita con id: {}", idCita);

        Cita cita = obtenerCitaOException(idCita);
        Long idMedicoAnterior = cita.getIdMedico();
        Long idNuevoMedico = request.idMedico();

        MedicoResponse nuevoMedico = obtenerMedicoActivo(idNuevoMedico);
        PacienteResponse paciente = obtenerPacienteActivo(request.idPaciente());

        validarPacienteDisponibleParaActualizar(request.idPaciente(), idCita);

        boolean cambioDeMedico = !idMedicoAnterior.equals(idNuevoMedico);
        if (cambioDeMedico) {
            validarMedicoDisponible(idNuevoMedico);

            sincronizarDisponibilidadMedico(cita.getId(), idNuevoMedico, cita.getEstadoCita());
        }

        cita.actualizar(
                request.idPaciente(),
                idNuevoMedico,
                request.fechaCita(),
                request.sintomas()
        );
        citaRepository.saveAndFlush(cita);

        log.info("Cita id: {} actualizada exitosamente.", idCita);
        return citaMapper.entidadAResponse(cita, paciente, nuevoMedico);
    }

    @Override
    @Transactional
    public void actualizarEstadoCita(Long idCita, Long idEstadoCita) {
        log.info("Actualizando estado de la cita con id: {} y estado cita codigo: {}", idCita, idEstadoCita);

        Cita cita = obtenerCitaOException(idCita);
        EstadoCita nuevoEstado = EstadoCita.obtenerEstadoCitaPorCodigo(idEstadoCita);

        cita.actualizarEstadoCita(nuevoEstado);
        citaRepository.save(cita);

        sincronizarDisponibilidadMedico(cita.getId(), cita.getIdMedico(), cita.getEstadoCita());

        log.info("Estado de cita id: {} actualizado con éxito a {}", idCita, nuevoEstado);
    }

    @Override
    public void eliminar(Long id) {

        Cita cita = obtenerCitaOException(id);

        log.info("Eliminando cita con id: {}", id);

        cita.eliminar();

        citaRepository.save(cita);

        if (cita.getEstadoCita() == EstadoCita.PENDIENTE
                && !tieneCitasActivas(cita.getIdMedico(), ESTADOS_PERMITIDOS_PCEC, EstadoRegistro.ACTIVO)) {

            medicoClient.actualizarDisponibilidadMedico(cita.getIdMedico(), ID_DISPONIBILIDAD_DISPONIBLE);
            log.info("Médico id: {} liberado a DISPONIBLE tras eliminación de cita pendiente.", cita.getIdMedico());
        }

        log.info("Cita con id {} ha sido marcada como eliminado", id);
    }

    @Override
    public boolean tieneCitasActivasMedico(Long idMedico) {
        return citaRepository.existsByIdMedicoAndEstadoCitaIn(
                idMedico,
                ESTADOS_PERMITIDOS_CEC
        );
    }

    @Override
    public boolean verificarDisponibilidadMedico(Long idMedico) {
        boolean estadoMedico = citaRepository.existsByIdMedicoAndEstadoCitaIn(
                idMedico, ESTADOS_PERMITIDOS_CEC);
        return !estadoMedico;
    }


    private Cita obtenerCitaOException(Long id){
        log.info("Buscando cita con id {} ...",id);

        return citaRepository.findById(id).orElseThrow(()->
                new RecursoNoEncontradoException("Cita no encontrada con id: "+id));

    }

    private MedicoResponse obtenerMedicoActivo(Long id){
        log.info("Buscando medico activo con id {} en el servicio remoto...",id);

    return medicoClient.obtenerMedicoActivoPorId(id);
    }

    private PacienteResponse obtenerPacienteActivo(Long id){
        log.info("Buscando paciente activo con id {} en el servicio remoto...",id);

        return pacienteClient.obtenerPacienteActivoPorId(id);
    }

    private MedicoResponse obtenerMedicoSinEstado(Long id){
        log.info("Buscando medico sin activo con id {} en el servicio remoto...",id);

    return medicoClient.obtenerMedicoPorIdSinEstado(id);
    }


    private PacienteResponse obtenerPacienteSinEstado(Long id){
        log.info("Buscando paciente sin activo con id {} en el servicio remoto...",id);

        return pacienteClient.obtenerPacientePorIdSinEstado(id);
    }

    public boolean tieneCitasActivasPaciente(Long idPaciente) {
        return citaRepository.existsByIdPacienteAndEstadoCitaIn(
                idPaciente,
                ESTADOS_PERMITIDOS_CEC
        );
    }

    private void validarMedicoDisponible(Long idMedico) {
        if (!verificarDisponibilidadMedico(idMedico)) {
            log.info("El médico {} ya cuenta con una cita activa.", idMedico);
            throw new IllegalStateException("El médico no está disponible. Ya tiene una cita activa (CONFIRMADA o EN_CURSO).");
        }
    }

    private Long obtenerIdDisponibilidadResultante(EstadoCita estadoCita) {
        if (estadoCita == null) {
            throw new IllegalArgumentException("El estado de la cita no puede ser nulo");
        }

        return switch (estadoCita) {
            case PENDIENTE, CONFIRMADA -> ID_DISPONIBILIDAD_AGENDADO;
            case EN_CURSO              -> ID_DISPONIBILIDAD_EN_CONSULTA;
            case FINALIZADA, CANCELADA -> ID_DISPONIBILIDAD_DISPONIBLE;
        };
    }

    private void sincronizarDisponibilidadMedico(Long idCita, Long idMedico, EstadoCita estadoCita) {
        Long idDisponibilidad = obtenerIdDisponibilidadResultante(estadoCita);

        if (ID_DISPONIBILIDAD_DISPONIBLE.equals(idDisponibilidad)
                && tieneOtrasCitasActivas(idMedico, ESTADOS_PERMITIDOS_PCEC, EstadoRegistro.ACTIVO, idCita)) {

            log.info("El médico id: {} aún cuenta con otras citas activas. Permanecerá NO_DISPONIBLE.", idMedico);
            return;
        }

        log.info("Sincronizando disponibilidad del médico id: {} a disponibilidad id: {} por cita en estado: {}",
                idMedico, idDisponibilidad, estadoCita);

        medicoClient.actualizarDisponibilidadMedico(idMedico, idDisponibilidad);
    }



    private void validarPacienteDisponible(Long idPaciente) {
        boolean tieneCitasActivas = citaRepository.existsByIdPacienteAndEstadoCitaInAndEstadoRegistro(
                idPaciente,
                ESTADOS_PERMITIDOS_PCEC,
                EstadoRegistro.ACTIVO
        );

        if (tieneCitasActivas) {
            log.info("El paciente id: {} ya cuenta con una cita activa.", idPaciente);
            throw new IllegalStateException("El paciente ya cuenta con una cita activa (PENDIENTE, CONFIRMADA o EN_CURSO).");
        }
    }

    private void validarPacienteDisponibleParaActualizar(Long idPaciente, Long idCitaActual) {
        boolean tieneOtrasCitasActivas = citaRepository.existsByIdPacienteAndEstadoCitaInAndEstadoRegistroAndIdNot(
                idPaciente,
                ESTADOS_PERMITIDOS_PCEC,
                EstadoRegistro.ACTIVO,
                idCitaActual
        );

        if (tieneOtrasCitasActivas) {
            log.info("El paciente id: {} ya cuenta con OTRA cita activa distinta a la cita actual id: {}", idPaciente, idCitaActual);
            throw new IllegalStateException("El paciente ya cuenta con una cita activa (PENDIENTE, CONFIRMADA o EN_CURSO).");
        }
    }


    private boolean tieneCitasActivas(Long idMedico,
                                      List<EstadoCita> estados,
                                      EstadoRegistro estadoRegistro) {
        return citaRepository.existsByIdMedicoAndEstadoCitaInAndEstadoRegistro(
                idMedico,
                estados,
                estadoRegistro
        );
    }

    private boolean tieneOtrasCitasActivas(Long idMedico,
                                           List<EstadoCita> estados,
                                           EstadoRegistro estadoRegistro,
                                           Long idCita) {
        return citaRepository.existsByIdMedicoAndEstadoCitaInAndEstadoRegistroAndIdNot(
                idMedico,
                estados,
                estadoRegistro,
                idCita
        );
    }



}
