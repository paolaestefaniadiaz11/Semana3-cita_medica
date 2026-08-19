package com.paola.commons.dto.medico;

import jakarta.validation.constraints.*;

public record MedicoRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 1,max = 50, message = "El nombre del medico debe tener de 1-50 caracteres")
        String nombre,

        @NotBlank(message = "El apellido paterno es obligatorio")
        @Size(min = 1,max = 50, message = "El apellido paterno del medico debe tener de 1-50 caracteres")
        String apellidoPaterno,

        @NotBlank(message = "El apellido materno es obligatorio")
        @Size(min = 1,max = 50, message = "El apellido materno del medico debe tener de 1-50 caracteres")
        String apellidoMaterno,

        @NotNull(message = "La edad es obligatoria")
        @Min(value = 19, message = "La edad mínima permitida es 18 años")
        @Max(value = 100, message = "La edad máxima permitida es 100 años")
        Short edad,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        @Size(min = 1,max = 100, message = "El email no puede exceder 100 caracteres")
        String email,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe contener exactamente 10 dígitos numéricos")
        String telefono,

        @NotBlank(message = "El numero de cedula profesional del medigo es obligatorio")
        @Size(min = 12,max = 12, message = "El numero de expediente debe contener exactamente 12 caracteres")
        String cedulaProfesional,

        @NotNull(message = "La estatura es obligatoria")
        @Positive(message = "El id de la especialidad debe ser positivo")
        Long idEspecialidad

        ) {
}
