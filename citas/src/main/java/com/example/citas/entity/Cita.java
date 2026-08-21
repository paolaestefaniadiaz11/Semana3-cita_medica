package com.example.citas.entity;

import com.example.citas.enums.EstadoCita;
import com.paola.commons.enums.EstadoRegistro;
import com.paola.commons.utils.StringCustomUtils;
import com.paola.commons.utils.ValoresNumericosUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "CITAS")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor (access = AccessLevel.PROTECTED)
@Getter

public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CITA")
    private Long id;

    @Column(name = "ID_PACIENTE",  nullable = false)
    private Long idPaciente;

    @Column(name = "ID_MEDICO",  nullable = false)
    private Long idMedico;

    @Column(name = "FECHA_CITA",  nullable = false)
    private LocalDateTime fechaCita;

    @Column(name = "SINTOMAS", length = 500, nullable = false)
    private String sintomas;

    @Column(name = "ESTADO_CITA",nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoCita estadoCita;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_REGISTRO", nullable = false)
    private EstadoRegistro estadoRegistro;

    private static void validarId (Long id, String campo){

        ValoresNumericosUtils.validarLongPositivo(id,
                "El id del "+campo +"+es requerido y debe ser positivo");

    }

    private static void validarFecha(LocalDateTime fechaCita){
        if (fechaCita == null || !fechaCita.isAfter(LocalDateTime.now()))
        throw new IllegalArgumentException("La fecha de la cita es requerida y dee ser futura");
    }

    public static void validarDatos(Long idPaciente, Long idMedico,
                 LocalDateTime fechaCita, String sintomas){

        validarId(idPaciente,"paciente");
        validarId(idMedico,"medico");
        validarFecha(fechaCita);

        StringCustomUtils.validarTamanio(sintomas, 20, 500,
                "Los sintomas son requeridos y debe tener entre 20 y 500 caracteres");

    }

    public void validarNoEliminado(){

        if (this.estadoRegistro == EstadoRegistro.ELIMINADO)
            throw new IllegalArgumentException("El medico ya esta eliminado");
    }

    public void validarEliminacionPermitida(){
        validarNoEliminado();
        boolean esEstadoPermitido = List.of(
                EstadoCita.PENDIENTE,
                EstadoCita.CANCELADA,
                EstadoCita.FINALIZADA
        ).contains(this.estadoCita);

        if (!esEstadoPermitido) {
            throw new IllegalStateException(
                    "La cita con estado " + this.estadoCita + " no puede ser eliminada. Solo se permite en PENDIENTE, CANCELADA o FINALIZADA."
            );
        }
        if (!estadoCita.isEliminable())
            throw new IllegalArgumentException("La cita con estado"+estadoCita+"no puede eliminarse");
    }

    public void validarActualizacionPermitida(){
        validarNoEliminado();
        if (!estadoCita.isActualizable())
            throw new IllegalArgumentException("La cita con estado "+estadoCita+" no puede actualizarse");

    }

    public void eliminar(){

        validarEliminacionPermitida();

        this.estadoRegistro = EstadoRegistro.ELIMINADO;

    }

    public void actualizar (Long idPaciente, Long idMedico,
                            LocalDateTime fechaCita, String sintomas){
        validarActualizacionPermitida();

        validarDatos(idPaciente,idMedico,fechaCita,sintomas);

        this.idPaciente = idPaciente;
        this.idMedico = idMedico;
        this.fechaCita = fechaCita;
        this.sintomas = sintomas.trim();
    }

    public void actualizarEstadoCita(EstadoCita nuevoEstado){

        validarNoEliminado();

        if(nuevoEstado == null)
            throw new IllegalArgumentException("El nuevo estado de la cita es requerido");

        if (!estadoCita.puedeCambiarA(nuevoEstado))
            throw new IllegalArgumentException("La cita con estado "+estadoCita+" solo puede camniar a: "+ estadoCita.puedeCambiar());


        this.estadoCita = nuevoEstado;

    }

    public static Cita crear(
            Long idPaciente, Long idMedico,
            LocalDateTime fechaCita, String sintomas){
        validarDatos(idPaciente,idMedico,fechaCita,sintomas);

        return Cita.builder()
        .idPaciente(idPaciente)
        .idMedico(idMedico)
        .fechaCita(fechaCita)
        .sintomas(sintomas.trim())
        .estadoCita(EstadoCita.PENDIENTE)
        .estadoRegistro(EstadoRegistro.ACTIVO)
        .build();
    }

}

