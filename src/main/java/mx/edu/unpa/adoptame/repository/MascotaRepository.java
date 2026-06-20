package mx.edu.unpa.adoptame.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mx.edu.unpa.adoptame.domain.Mascota;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Integer> {
	Optional<Mascota> findByNombre(String nombre);

	Optional<Mascota> findFirstByNombreIgnoreCaseOrderByIdMascotaAsc(String nombre);

	@Query("SELECT DISTINCT m FROM Mascota m "
			+ "LEFT JOIN FETCH m.usuarioDonador "
			+ "LEFT JOIN FETCH m.imagenes")
	List<Mascota> findAllWithDonador();

	@Query("SELECT DISTINCT m FROM Mascota m "
			+ "LEFT JOIN FETCH m.usuarioDonador "
			+ "LEFT JOIN FETCH m.imagenes "
			+ "WHERE m.idMascota = :idMascota")
	Optional<Mascota> findByIdWithDetails(@Param("idMascota") Integer idMascota);

	@Query("SELECT DISTINCT m FROM Mascota m "
			+ "LEFT JOIN FETCH m.usuarioDonador "
			+ "LEFT JOIN FETCH m.imagenes "
			+ "WHERE m.usuarioDonador.idUsuario = :idUsuario")
	List<Mascota> findByUsuarioDonadorWithDetails(@Param("idUsuario") Integer idUsuario);

	List<Mascota> findByUsuarioDonador_IdUsuario(Integer idUsuario);
}

