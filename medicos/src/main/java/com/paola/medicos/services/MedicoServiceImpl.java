package com.paola.medicos.services;

import com.paola.commons.client.CitaClient;
import com.paola.commons.dto.medico.MedicoRequest;
import com.paola.commons.dto.medico.MedicoResponse;
import com.paola.commons.enums.DisponibilidadMedico;
import com.paola.commons.enums.EspecialidadMedico;
import com.paola.commons.enums.EstadoRegistro;
import com.paola.commons.exceptions.RecursoNoEncontradoException;
import com.paola.medicos.entity.Medico;
import com.paola.medicos.mappers.MedicoMapper;
import com.paola.medicos.repository.MedicoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@AllArgsConstructor
@Transactional
@Slf4j

public class MedicoServiceImpl implements MedicoService {

    private final MedicoRepository medicoRepository;

    private final MedicoMapper medicoMapper;

    private final CitaClient citaClient;


    @Override
    @Transactional(readOnly = true)
    public List<MedicoResponse> listar() {
        log.info("Listando medicos con estado ACTIVO");
        return medicoRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO)
                .stream()
                .map(medicoMapper::entidadAResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicoResponse obtenerPorId(Long id) {
        return medicoMapper.entidadAResponse(obtenerMedicoActivoOException(id));
    }


    @Override
    @Transactional(readOnly = true)
    public MedicoResponse obtenerMedicoPorIdSinEstado(Long id) {

        log.info("Listando medico sin estado con id:{}",id);

        return medicoMapper.entidadAResponse(medicoRepository.findById(id)
                .orElseThrow(()-> new RecursoNoEncontradoException("Recurso no encontrado con id: "+id)));
    }

    @Override
    public MedicoResponse registrar(MedicoRequest request) {

        log.info("Registrando nuevo medico: {}",request.nombre());

        validarDatosUnicos(request);

        Medico medico =  medicoMapper.requestAEntidad(request);

        medico.actualizarEspecialidad(EspecialidadMedico.obtenerDisponibilidadPorCodigo(request.idEspecialidad()));

        medicoRepository.save(medico);

        return  medicoMapper.entidadAResponse(medico);


    }

    @Override
    public void actualizarDisponibilidadMedico(Long idMedico, Long idDisponibilidad) {

        log.info("Actualizando disponibilidad del medico con id: {}",idMedico);

        Medico medico = obtenerMedicoActivoOException(idMedico);

        DisponibilidadMedico nuevaDisponibilidad = DisponibilidadMedico.obtenerDisponibilidadPorCodigo(idDisponibilidad);

        DisponibilidadMedico disponibilidadAnterior = medico.getDisponibilidad();

        if (disponibilidadAnterior == nuevaDisponibilidad)
            log.info("El médico con id {} ya tiene la disponibilidad {}", idMedico, nuevaDisponibilidad);
            //return;

        if (nuevaDisponibilidad == DisponibilidadMedico.DISPONIBLE) {
            Boolean tieneCitas = citaClient.tieneCitasActivasMedico(idMedico);
            if (Boolean.TRUE.equals(tieneCitas)) {
                log.info("No se puede cambiar a DISPONIBLE el médico {} porque tiene citas activas", idMedico);
                throw new IllegalArgumentException("No se puede cambiar la disponibilidad a DISPONIBLE porque el médico tiene citas en estado CONFIRMADA o EN_CURSO.");
            }
        }

        medico.actualizarDisponibilidad(nuevaDisponibilidad);

        log.info("Disponibilidad del medico con id {} cambio de {} a {}",idMedico,disponibilidadAnterior,nuevaDisponibilidad);

    }

    @Override
    public MedicoResponse actualizar(MedicoRequest request, Long id) {

        Medico medico = obtenerMedicoActivoOException(id);

        log.info("Actualizar medico con id: {}",id);

        validarSinCitasActivas(id);

        validarCambiosUnicos(request,id);

        medico.actualizar(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno(),
                request.edad(),
                request.email(),
                request.telefono(),
                request.cedulaProfesional(),
                EspecialidadMedico.obtenerDisponibilidadPorCodigo(request.idEspecialidad()));

        log.info("Medico actualizado exitosamente");

        return medicoMapper.entidadAResponse(medico);
    }

    @Override
    public void eliminar(Long id) {

        Medico medico = obtenerMedicoActivoOException(id);

        validarSinCitasActivas(id);

        log.info("Eliminando medico con id: {}",id);

        medico.eliminar();

    }

    @Override
    @Transactional
    public void sincronizarDisponibilidadInterna(Long idMedico, Long idDisponibilidad) {
        log.info("Sincronizando disponibilidad interna para médico id: {} a estado: {}", idMedico, idDisponibilidad);

        Medico medico = obtenerMedicoActivoOException(idMedico);
        DisponibilidadMedico nuevaDisponibilidad = DisponibilidadMedico.obtenerDisponibilidadPorCodigo(idDisponibilidad);

        medico.forzarDisponibilidad(nuevaDisponibilidad);

        medicoRepository.save(medico);
    }

    private Medico obtenerMedicoActivoOException(Long id){
        log.info("Listando medicos con estado ACTIVO");

        return medicoRepository.findByIdAndEstadoRegistro(id,EstadoRegistro.ACTIVO)
                .orElseThrow(()-> new RecursoNoEncontradoException("Medico Activo no encontrado con id:"+id));

    }

    private void validarDatosUnicos(MedicoRequest request){
        log.info("Validando email unico...");

        if (medicoRepository.existsByEmailIgnoreCaseAndEstadoRegistro(
                request.email().trim(),EstadoRegistro.ACTIVO))

            throw new IllegalArgumentException("Ya existe un medico activo registrado con el email: "+request.email());

        log.info("Validando telefono unico...");

        if (medicoRepository.existsByTelefonoAndEstadoRegistro(request.telefono().trim(), EstadoRegistro.ACTIVO))

            throw new IllegalArgumentException("Ya existe un medico activo registrado con el telefono"+request.telefono());


        if (medicoRepository.existsByCedulaProfesionalIgnoreCaseAndEstadoRegistro(
                request.cedulaProfesional().trim(),EstadoRegistro.ACTIVO))
            throw new IllegalArgumentException("Ya existe un medico activo registrado con la cedula: "+request.cedulaProfesional());

    }
    private void validarCambiosUnicos(MedicoRequest request,Long id){
        log.info("Validando cambio en email unico...");

        if (medicoRepository.existsByEmailIgnoreCaseAndEstadoRegistroAndIdNot(request.email().trim(),EstadoRegistro.ACTIVO,id))

            throw new IllegalArgumentException("Ya existe un medico activo registrado con el email: "+request.email());

        log.info("Validando cambio en telefono unico...");

        if (medicoRepository.existsByTelefonoAndEstadoRegistroAndIdNot(request.telefono().trim(),EstadoRegistro.ACTIVO,id))

            throw new IllegalArgumentException("Ya existe un medico activo registrado con el email: "+request.telefono());

        log.info("Validando cambio en de cedula profesional unica...");

        if(medicoRepository.existsByCedulaProfesionalIgnoreCaseAndEstadoRegistroAndIdNot(request.cedulaProfesional().trim(),EstadoRegistro.ACTIVO,id))
            throw new IllegalArgumentException("Ya existe un medico activo registrado con la cedula profesional: "+request.cedulaProfesional());
    }

    private void validarSinCitasActivas(Long idMedico) {
        Boolean tieneCitas = citaClient.tieneCitasActivasMedico(idMedico);

        if (Boolean.TRUE.equals(tieneCitas)) {
            log.warn("No se puede modificar/eliminar el médico id: {} porque tiene citas CONFIRMADA o EN_CURSO", idMedico);
            throw new IllegalArgumentException("No se puede actualizar ni eliminar el médico porque tiene citas activas en estado CONFIRMADA o EN_CURSO.");
        }
    }



}
