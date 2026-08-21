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
        }

        cita.actualizar(
                request.idPaciente(),
                idNuevoMedico,
                request.fechaCita(),
                request.sintomas()
        );
        citaRepository.saveAndFlush(cita);

        if (cambioDeMedico) {

            sincronizarDisponibilidadMedico(cita.getId(), idNuevoMedico, cita.getEstadoCita());

            sincronizarDisponibilidadMedico(cita.getId(), idMedicoAnterior, EstadoCita.FINALIZADA);
        }

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

        if (cita.getEstadoCita() == EstadoCita.PENDIENTE) {
            boolean tieneOtrasCitasActivas = citaRepository.existsByIdMedicoAndEstadoCitaInAndEstadoRegistro(
                    cita.getIdMedico(),
                    List.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.EN_CURSO),
                    EstadoRegistro.ACTIVO
            );

            if (!tieneOtrasCitasActivas) {
                medicoClient.actualizarDisponibilidadMedico(cita.getIdMedico(), 1L);
                log.info("Médico id: {} liberado a DISPONIBLE tras eliminación de cita pendiente.", cita.getIdMedico());
            } else
                log.info("Médico id: {} conserva estado NO_DISPONIBLE porque aún tiene otras citas activas.", cita.getIdMedico());

        }

        log.info("Cita con id {} ha sido marcada como eliminado", id);
    }

    @Override
    public boolean tieneCitasActivasMedico(Long idMedico) {
        return citaRepository.existsByIdMedicoAndEstadoCitaIn(
                idMedico,
                List.of(EstadoCita.CONFIRMADA, EstadoCita.EN_CURSO)
        );
    }

    @Override
    public boolean verificarDisponibilidadMedico(Long idMedico) {
        boolean estadoMedico = citaRepository.existsByIdMedicoAndEstadoCitaIn(
                idMedico, List.of(EstadoCita.CONFIRMADA, EstadoCita.EN_CURSO));
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
                List.of(EstadoCita.CONFIRMADA, EstadoCita.EN_CURSO)
        );
    }

    private void validarMedicoDisponible(Long idMedico) {
        if (!verificarDisponibilidadMedico(idMedico)) {
            log.info("El médico {} ya cuenta con una cita activa.", idMedico);
            throw new IllegalStateException("El médico no está disponible. Ya tiene una cita activa (CONFIRMADA o EN_CURSO).");
        }
    }

    private void sincronizarDisponibilidadMedico(Long idCita, Long idMedico, EstadoCita estadoCita) {
        Long idDisponibilidad = estadoCita.obtenerIdDisponibilidadResultante();

        if (idDisponibilidad.equals(1L)) {
            boolean tieneOtrasCitasActivas = citaRepository.existsByIdMedicoAndEstadoCitaInAndEstadoRegistroAndIdNot(
                    idMedico,
                    List.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.EN_CURSO),
                    EstadoRegistro.ACTIVO,
                    idCita
            );

            if (tieneOtrasCitasActivas) {
                log.info("El médico id: {} aún cuenta con otras citas activas. Permanecerá NO_DISPONIBLE.", idMedico);
                return;
            }
        }

        log.info("Sincronizando disponibilidad del médico id: {} a disponibilidad id: {} por cita en estado: {}",
                idMedico, idDisponibilidad, estadoCita);

        medicoClient.actualizarDisponibilidadMedico(idMedico, idDisponibilidad);
    }



    private void validarPacienteDisponible(Long idPaciente) {
        boolean tieneCitasActivas = citaRepository.existsByIdPacienteAndEstadoCitaInAndEstadoRegistro(
                idPaciente,
                List.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.EN_CURSO),
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
                List.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.EN_CURSO),
                EstadoRegistro.ACTIVO,
                idCitaActual
        );

        if (tieneOtrasCitasActivas) {
            log.info("El paciente id: {} ya cuenta con OTRA cita activa distinta a la cita actual id: {}", idPaciente, idCitaActual);
            throw new IllegalStateException("El paciente ya cuenta con una cita activa (PENDIENTE, CONFIRMADA o EN_CURSO).");
        }
    }




}
