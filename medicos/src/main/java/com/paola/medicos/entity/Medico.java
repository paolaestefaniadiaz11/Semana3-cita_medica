package com.paola.medicos.entity;

import com.paola.commons.enums.DisponibilidadMedico;
import com.paola.commons.enums.EspecialidadMedico;
import com.paola.commons.enums.EstadoRegistro;
import com.paola.commons.utils.StringCustomUtils;
import com.paola.commons.utils.ValoresNumericosUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MEDICOS")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter

public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MEDICO")
    private Long id;

    @Column(name = "NOMBRE", length = 50, nullable = false)
    private String nombre;

    @Column(name = "APELLIDO_PATERNO", length = 50, nullable = false)
    private String apellidoPaterno;

    @Column(name = "APELLIDO_MATERNO", length = 50, nullable = false)
    private String apellidoMaterno;

    @Column(name = "EDAD", nullable = false)
    private Short edad;

    @Column(name = "EMAIL", length = 100, nullable = false)
    private String email;

    @Column(name = "TELEFONO", length = 10, nullable = false)
    private String telefono;

    @Column(name = "CEDULA_PROFESIONAL", length = 12, nullable = false)
    private String cedulaProfesional;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESPECIALIDAD",nullable = false)
    private EspecialidadMedico especialidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "DISPONIBILIDAD", nullable = false)
    private DisponibilidadMedico disponibilidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_REGISTRO", nullable = false)
    private EstadoRegistro estadoRegistro;

    private void validarDatos(String nombre, String apellidoPaterno,
                              String apellidoMaterno, Short edad,
                              String email, String telefono, String cedulaProfesional,
                              EspecialidadMedico especialidad) {
//nombre
        StringCustomUtils.validarTamanio(nombre, 1, 50,
                "El nombre es requerido y debe tener entre 1 y 50 caracteres");

        //apellidoP
        StringCustomUtils.validarTamanio(apellidoPaterno, 1, 50,
                "El apellido paterno es requerido y debe tener entre 1 y 50 caracteres");

        //apellidoM
        StringCustomUtils.validarTamanio(apellidoMaterno, 1, 50,
                "El apellido materno es requerido y debe tener entre 1 y 50 caracteres");

        //email
        StringCustomUtils.validarTamanio(email, 1, 100,
                "El email del medico es requerido y debe tener entre 1 y 100 caracteres");

        //telefono
        StringCustomUtils.validarTamanio(telefono, 10, 10,
                "El telefono del medico es requerido y debe tener exactamente 10 digitos (0-9)");

        StringCustomUtils.validarTamanio(cedulaProfesional, 12, 12,
                "La cedula profesional del medico del medico es requerida y debe tener exactamente 12 caracteres");

        ValoresNumericosUtils.validarRangoShort(edad,(short) 18, (short)100,
                "La edad del medico es requerido y debe tener entre 18 años y 100 años");

        if (especialidad == null)
            throw new IllegalArgumentException("La especialidad es requeridaentiti");

    }


    public void validarNoEliminado(){

        if (this.estadoRegistro == EstadoRegistro.ELIMINADO)
            throw new IllegalArgumentException("El medico ya esta eliminado");
    }

    public void actualizarEspecialidad(EspecialidadMedico especialidad){

        validarNoEliminado();
        if (especialidad == null)
            throw new IllegalArgumentException("La especialidad es requeridaact  especialidad");

        this.especialidad= especialidad;
    }

    public void actualizarDisponibilidad(DisponibilidadMedico disponibilidad){

        validarNoEliminado();
        if (this.especialidad == null)
            throw new IllegalArgumentException("La disponibilidad es requerida");

        this.disponibilidad= disponibilidad;
    }

    public void eliminar(){

        validarNoEliminado();

        this.estadoRegistro = EstadoRegistro.ELIMINADO;

    }


    public void actualizar (String nombre, String apellidoPaterno,
                            String apellidoMaterno, Short edad,
                            String email, String telefono, String cedulaProfesional,
                            EspecialidadMedico especialidad){
        validarNoEliminado();

        validarDatos(nombre,apellidoPaterno,apellidoMaterno,edad,email,telefono,cedulaProfesional,especialidad);

        actualizarEspecialidad(especialidad);

        this.nombre = nombre.trim();
        this.apellidoPaterno = apellidoPaterno.trim();
        this.apellidoMaterno = apellidoMaterno.trim();
        this.edad = edad;
        this.email = email.trim();
        this.telefono = telefono.trim();
        this.cedulaProfesional = cedulaProfesional.trim();

    }

    public void forzarDisponibilidad(DisponibilidadMedico nuevaDisponibilidad) {
        this.disponibilidad = nuevaDisponibilidad;
    }



}
