package com.paola.commons.dto.medico;

public record MedicoResponse(

        Long id,
        String nombre,
        Short edad,
        String email,
        String telefono,
        String cedulaProfesional,
        String especialidad,
        String disponibilidad,
        Long idDisponibilidad

) {}
