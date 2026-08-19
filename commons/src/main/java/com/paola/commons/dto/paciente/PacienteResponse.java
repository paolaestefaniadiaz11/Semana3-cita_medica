package com.paola.commons.dto.paciente;

public record PacienteResponse(
        Long id,
        String nombre,
        Short edad,
        Double peso,
        Double estatura,
        Double imc,
        String email,
        String telefono,
        String direccion,
        String numeroExpediente

) {
}
