package com.paola.pacientes.mappers;

import com.paola.commons.dto.paciente.PacienteResponse;
import com.paola.commons.enums.EstadoRegistro;
import com.paola.commons.mappers.CommonMapper;
import com.paola.pacientes.entity.Paciente;
import org.springframework.stereotype.Component;

import com.paola.commons.dto.paciente.PacienteRequest;

@Component
public class PacienteMapper implements CommonMapper<PacienteRequest, PacienteResponse, Paciente >{

    @Override

    public Paciente requestAEntidad(PacienteRequest request) {
        if (request == null) return null;

        return Paciente.builder()
                .nombre(request.nombre().trim())
                .apellidoPaterno(request.apellidoPaterno().trim())
                .apellidoMaterno(request.apellidoMaterno().trim())
                .edad(request.edad())
                .peso(request.peso())
                .estatura(request.estatura())
                .email(request.email().toLowerCase().trim())
                .telefono(request.telefono().trim())
                .direccion(request.direccion().trim())
                .estadoRegistro(EstadoRegistro.ACTIVO)
                .build();

    }

    public PacienteResponse entidadAResponse(Paciente entidad) {
        if (entidad == null) return null;

        return new PacienteResponse(

                entidad.getId(),
                String.join(" ",
                        entidad.getNombre(),
                        entidad.getApellidoPaterno(),
                        entidad.getApellidoMaterno()),
                entidad.getEdad(),
                entidad.getPeso(),
                entidad.getEstatura(),
                entidad.calcularImc(),
                entidad.getEmail(),
                entidad.getTelefono(),
                entidad.getDireccion(),
                entidad.generarNumeroExpediente()

        );


    }
}
