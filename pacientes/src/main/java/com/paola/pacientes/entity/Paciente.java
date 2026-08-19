package com.paola.pacientes.entity;

import com.paola.commons.enums.EstadoRegistro;
import com.paola.commons.utils.ValoresNumericosUtils;
import jakarta.persistence.*;
import com.paola.commons.utils.StringCustomUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "PACIENTES")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter

public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PACIENTE")
    private Long id;

    @Column(name = "NOMBRE", length = 50, nullable = false)
    private String nombre;

    @Column(name = "APELLIDO_PATERNO", length = 50, nullable = false)
    private String apellidoPaterno;

    @Column(name = "APELLIDO_MATERNO", length = 50, nullable = false)
    private String apellidoMaterno;

    @Column(name = "EDAD", nullable = false)
    private Short edad;

    @Column(name = "PESO", nullable = false)
    private Double peso;

    @Column(name = "ESTATURA", nullable = false)
    private Double estatura;

    @Column(name = "IMC", nullable = false)
    private Double imc;

    @Column(name = "EMAIL", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "NUM_EXPEDIENTE", length = 20, nullable = false,unique = true)
    private String numExpediente;

    @Column(name = "TELEFONO", length = 10, nullable = false,unique = true)
    private String telefono;

    @Column(name = "DIRECCION", length = 150, nullable = false)
    private String direccion;

    @Enumerated (EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "ESTADO_REGISTRO",nullable = false)
    private EstadoRegistro estadoRegistro;

    private void validarDatos(String nombre,
                              String apellidoPaterno,
                              String apellidoMaterno,
                              Short edad,
                              Double peso,
                              Double estatura,
                              String email,
                              String telefono,
                              String direccion
    ){
        //nombre
        StringCustomUtils.validarTamanio(nombre, 1, 50,
                "El nombre es requerido y debe tener entre 1 y 50 caracteres");

        //apellidoP
        StringCustomUtils.validarTamanio(apellidoPaterno, 1, 50,
                "El apellido paterno es requerido y debe tener entre 1 y 50 caracteres");

        //apellidoM
        StringCustomUtils.validarTamanio(apellidoMaterno, 1, 50,
                "El apellido materno es requerido y debe tener entre 1 y 50 caracteres");

        //edad
        ValoresNumericosUtils.validarRangoShort(edad,(short) 1, (short)100,
                "La edad del paciente es requerido y debe tener entre 1 año y 100 años");

        //
        ValoresNumericosUtils.validarRangoDouble(peso,(double) 0.1, (double) 200.0,
                "El peso del paciente es requerido y debe tener entre 0.1 kg y 200.0kg");

        ValoresNumericosUtils.validarRangoDouble(estatura,(double) 1, (double) 100,
                "La estatura del paciente es requerida y debe tener entre 1.0m y 2.0m");

        //email
        StringCustomUtils.validarTamanio(email, 1, 100,
                "El email del paciente es requerido y debe tener entre 1 y 100 caracteres");

        //telefono
        StringCustomUtils.validarTamanio(telefono, 10, 10,
                "El telefono del paciente es requerido y debe tener exactamente 10 caracteres");

        //direccion

        StringCustomUtils.validarTamanio(direccion, 1, 150,
                "La direccion del paciente es requerido y debe tener entre 1 y 150 caracteres");

    }

    public void validarNoEliminado(){

        if (this.estadoRegistro == EstadoRegistro.ELIMINADO)
            throw new IllegalArgumentException("El paciente ya esta eliminado");
    }

    public void eliminar(){

        validarNoEliminado();

        this.estadoRegistro = EstadoRegistro.ELIMINADO;

    }

    public void actualizar (String nombre, String apellidoPaterno,
                            String apellidoMaterno, Short edad,
                            Double peso, Double estatura,
                            String email, String telefono,
                            String direccion){

        validarNoEliminado();

        validarDatos(nombre, apellidoPaterno, apellidoMaterno, edad, peso, estatura, email, telefono, direccion);

        this.nombre =nombre.trim();
        this.apellidoPaterno =apellidoPaterno.trim();
        this.apellidoMaterno =apellidoMaterno.trim();
        this.edad =edad;
        this.peso =peso;
        this.estatura =estatura;
        this.email =email.trim();
        this.telefono =telefono.trim();
        this.direccion =direccion.trim();
    }

    public Double calcularImc() {
        if (this.peso == null || this.estatura == null || this.estatura <= 0) {
            return null;
        }
        double resultado = this.peso / Math.pow(this.estatura, 2);

        return BigDecimal.valueOf(resultado)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public String generarNumeroExpediente() {
        if (this.telefono == null || this.telefono.isBlank()) {
            return "";
        }
        StringBuilder expediente = new StringBuilder();
        for (char digito : this.telefono.trim().toCharArray()) {
            expediente.append(digito).append('X');
        }
        return expediente.toString();
    }




}
