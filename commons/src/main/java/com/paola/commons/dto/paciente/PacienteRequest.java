package com.paola.commons.dto.paciente;

import jakarta.validation.constraints.*;

public record PacienteRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 1,max = 50, message = "El nombre del alumno debe tener de 1-50 caracteres")
        String nombre,

        @NotBlank(message = "El apellido paterno es obligatorio")
        @Size(min = 1,max = 50, message = "El apellido paterno del alumno debe tener de 1-50 caracteres")
        String apellidoPaterno,

        @NotBlank(message = "El apellido materno es obligatorio")
        @Size(min = 1,max = 50, message = "El apellido materno del alumno debe tener de 1-50 caracteres")
        String apellidoMaterno,

        @NotNull(message = "La edad es obligatoria")
        @Min(value = 1, message = "La edad mínima permitida es 1 año")
        @Max(value = 100, message = "La edad máxima permitida es 100 años")
        Short edad,

        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "0.1", message = "El peso mínimo es 0.1 kg")
        @DecimalMax(value = "200.0", message = "El peso máximo es 200.0 kg")
        Double peso,

        @NotNull(message = "La estatura es obligatoria")
        @DecimalMin(value = "1.0", message = "La estatura mínima es 1.0 m")
        @DecimalMax(value = "2.0", message = "La estatura máxima es 2.0 m")
        Double estatura,

        @NotNull(message = "El imc es obligatorio")
        Double imc,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        @Size(min = 1,max = 100, message = "El email no puede exceder 100 caracteres")
        String email,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe contener exactamente 10 dígitos numéricos")
        String telefono,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(min = 1,max = 150, message = "La direccion del paciente debe tener de 1-150 caracteres")
        String direccion
){
}
