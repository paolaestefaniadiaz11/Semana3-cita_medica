package com.example.citas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.paola.commons.dto.datos.DatosMedico;
import com.paola.commons.dto.datos.DatosPaciente;

import java.time.LocalDateTime;


public record CitaResponse(
        Long id,
        DatosPaciente paciente,
        DatosMedico medico,
        @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime fechaCita,
        String sintomas,
        String estadoCita
) {
}
