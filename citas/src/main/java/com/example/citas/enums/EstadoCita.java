package com.example.citas.enums;

import com.paola.commons.exceptions.RecursoNoEncontradoException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public enum EstadoCita {

    PENDIENTE(1L,"Pendiente de confirmar",true,true) {
        @Override
        public Set<EstadoCita> puedeCambiar() {
            return EnumSet.of(CONFIRMADA, CANCELADA);
        }
    },

    CONFIRMADA(2L,"Confirmada por el paciente",true,false) {
        @Override
        public Set<EstadoCita> puedeCambiar() {
            return EnumSet.of(EN_CURSO, CANCELADA);
        }
    },

    EN_CURSO(3L, "Paciente llego a su cita",true, false) {
        @Override
        public Set<EstadoCita> puedeCambiar() {
            return EnumSet.of(FINALIZADA);
        }
    },

    FINALIZADA(4L,"Cita finalizada",false, true) {
        @Override
        public Set<EstadoCita> puedeCambiar() {
            return Set.of();
        }
    },

    CANCELADA(5L,"Cita cancelada",false,true) {
        @Override
        public Set<EstadoCita> puedeCambiar() {
            return Set.of();
        }
    };

    private final Long codigo;
    private final String descripcion;
    private final boolean actualizable;
    private final boolean eliminable;

    public abstract Set<EstadoCita> puedeCambiar();

    public boolean puedeCambiarA(EstadoCita nuevoEstado){
        return puedeCambiar().contains(nuevoEstado);
    }

    public Long obtenerIdDisponibilidadResultante() {
        return switch (this) {
            case PENDIENTE, CONFIRMADA -> 5L;
            case EN_CURSO              -> 2L;
            case FINALIZADA, CANCELADA -> 1L;
        };
    }

    public static EstadoCita obtenerEstadoCitaPorCodigo(Long codigo){

        for (EstadoCita ec: values()){
            if (Objects.equals(ec.codigo,codigo))
                return ec;
        }

        throw new RecursoNoEncontradoException("Codigo de cita no valido: "+ codigo);
    }

}
