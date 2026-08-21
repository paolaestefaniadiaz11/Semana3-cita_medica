package com.example.citas.repository;

import com.example.citas.entity.Cita;
import com.example.citas.enums.EstadoCita;
import com.paola.commons.enums.EstadoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    boolean existsByIdPacienteAndEstadoCitaIn(Long idPaciente, List<EstadoCita> estados);

    List<Cita> findByEstadoRegistro(EstadoRegistro estadoRegistro);

    boolean existsByIdMedicoAndEstadoCitaIn(Long idMedico, List<EstadoCita> estados);

    boolean existsByIdMedicoAndEstadoCitaInAndEstadoRegistro(
            Long idMedico,
            List<EstadoCita> estados,
            EstadoRegistro estadoRegistro
    );


    boolean existsByIdMedicoAndEstadoCitaInAndEstadoRegistroAndIdNot(
            Long idMedico,
            List<EstadoCita> estados,
            EstadoRegistro estadoRegistro,
            Long idCita
    );


    boolean existsByIdPacienteAndEstadoCitaInAndEstadoRegistro(
            Long idPaciente,
            List<EstadoCita> estados,
            EstadoRegistro estadoRegistro
    );

    @Query("""
        SELECT COUNT(c) > 0 
        FROM Cita c 
        WHERE c.idPaciente = :idPaciente 
          AND c.estadoCita IN :estados 
          AND c.estadoRegistro = :estadoRegistro 
          AND c.id <> :idCita
    """)
    boolean existsByIdPacienteAndEstadoCitaInAndEstadoRegistroAndIdNot(
            @Param("idPaciente") Long idPaciente,
            @Param("estados") List<EstadoCita> estados,
            @Param("estadoRegistro") EstadoRegistro estadoRegistro,
            @Param("idCita") Long idCita
    );


}
