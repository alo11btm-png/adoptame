package mx.edu.unpa.adoptame.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mx.edu.unpa.adoptame.domain.SolicitudAdopcion;

@Repository
public interface SolicitudAdopcionRepository extends JpaRepository<SolicitudAdopcion, Integer> {

	boolean existsByMascota_IdMascotaAndUsuarioSolicitante_IdUsuarioAndEstado(
			Integer idMascota, Integer idUsuarioSolicitante, String estado);

	@Query("SELECT s FROM SolicitudAdopcion s "
			+ "JOIN FETCH s.mascota m "
			+ "JOIN FETCH s.usuarioSolicitante u "
			+ "WHERE m.usuarioDonador.idUsuario = :idUsuario "
			+ "AND s.estado = 'Pendiente' "
			+ "ORDER BY s.fechaSolicitud DESC")
	List<SolicitudAdopcion> findPendientesRecibidas(@Param("idUsuario") Integer idUsuario);

	@Query("SELECT s FROM SolicitudAdopcion s "
			+ "JOIN FETCH s.mascota m "
			+ "JOIN FETCH s.usuarioSolicitante u "
			+ "WHERE s.usuarioSolicitante.idUsuario = :idUsuario "
			+ "ORDER BY s.fechaSolicitud DESC")
	List<SolicitudAdopcion> findEnviadas(@Param("idUsuario") Integer idUsuario);

	List<SolicitudAdopcion> findByMascota_IdMascotaAndEstado(Integer idMascota, String estado);

	Optional<SolicitudAdopcion> findByIdSolicitud(Integer idSolicitud);
}
