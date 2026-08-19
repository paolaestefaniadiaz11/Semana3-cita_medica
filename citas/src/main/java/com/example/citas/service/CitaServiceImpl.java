package com.example.citas.service;

import com.example.citas.dto.CitaRequest;
import com.example.citas.dto.CitaResponse;
import com.example.citas.entity.Cita;
import com.example.citas.enums.EstadoCita;
import com.example.citas.mapper.CitaMapper;
import com.example.citas.repository.CitaRepository;
import com.paola.commons.client.MedicoClient;
import com.paola.commons.dto.medico.MedicoResponse;
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

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> listar() {

        log.info("Listando todas las citas con estado ACTIVO");

        return citaRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO)
                .stream()
                .map(cita -> citaMapper.entidadAResponse(
                        cita,
                        null,
                        obtenerMedicoSinEstado(cita.getIdMedico())
                ))
                .toList();
    }

    @Override
    public CitaResponse obtenerPorId(Long id) {

        Cita cita =  obtenerCitaOException(id);

        return citaMapper.entidadAResponse
                (cita,
                        null,
                        obtenerMedicoSinEstado(cita.getIdMedico()));
    }






    @Override
    public CitaResponse registrar(CitaRequest request) {
        log.info("Registrando nueva cita");

        MedicoResponse medico =obtenerMedicoActivo(request.idMedico());

        Cita cita =citaMapper.requestAEntidad(request);



        citaRepository.save(cita);

        log.info("Cita registrada exitosamente");

        return citaMapper.entidadAResponse(
                cita,
                null,
                medico
        );
    }

    @Override
    public CitaResponse actualizar(CitaRequest request, Long id) {
        Cita cita = obtenerCitaOException(id);

        MedicoResponse medico = obtenerMedicoActivo(request.idMedico());

        //log

        cita.actualizar(

                request.idPaciente(),
                request.idMedico(),
                request.fechaCita(),
                request.sintomas()
        );

        //log

        return citaMapper.entidadAResponse(
                cita,
                null,
                medico
        );



    }

    @Override
    public void actualizarEstadoCita(Long idCita, Long idEstadoCita) {
        Cita cita = obtenerCitaOException(idCita);

        //log
        log.info("Actualizando estado de la cita con id: {}",idCita);

        cita.actualizarEstadoCita(EstadoCita.obtenerEstadoCitaPorCodigo(idEstadoCita));

        //log

    }

    @Override
    public void eliminar(Long id) {

        Cita cita = obtenerCitaOException(id);

        log.info("Eliminando cita con id: {}",id);

        cita.eliminar();

        log.info("Cita con id {} ha sido marcada como eliminado",id);


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

    private MedicoResponse obtenerMedicoSinEstado(Long id){
        log.info("Buscando medico sin activo con id {} en el servicio remoto...",id);

    return medicoClient.obtenerMedicoPorIdSinEstado(id);
    }



}
