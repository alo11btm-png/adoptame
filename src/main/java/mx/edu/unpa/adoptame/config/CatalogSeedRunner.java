package mx.edu.unpa.adoptame.config;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mx.edu.unpa.adoptame.domain.ImagenMascota;
import mx.edu.unpa.adoptame.domain.Mascota;
import mx.edu.unpa.adoptame.domain.Usuario;
import mx.edu.unpa.adoptame.repository.ImagenMascotaRepository;
import mx.edu.unpa.adoptame.repository.MascotaRepository;
import mx.edu.unpa.adoptame.repository.UsuarioRepository;

/**
 * Inserta los 6 gatos del catálogo (Mishi…Kira) si aún no existen en adopciones_db.
 * Sin borrar mascotas ya registradas por los usuarios.
 */
@Component
public class CatalogSeedRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(CatalogSeedRunner.class);

	private static final String SEED_EMAIL = "alo11btm@gmail.com";

	private static final List<CatalogPet> CATALOG = List.of(
			new CatalogPet("Mishi", "Siamés", "Hembra", "En proceso"),
			new CatalogPet("Luna", "Persa", "Hembra", "Disponible"),
			new CatalogPet("Simba", "Maine Coon", "Macho", "En proceso"),
			new CatalogPet("Nina", "Bengalí", "Hembra", "Disponible"),
			new CatalogPet("Oreo", "Doméstico", "Macho", "En proceso"),
			new CatalogPet("Kira", "Azul Ruso", "Hembra", "En proceso")
	);

	private final MascotaRepository mascotaRepository;
	private final UsuarioRepository usuarioRepository;
	private final ImagenMascotaRepository imagenMascotaRepository;

	public CatalogSeedRunner(
			MascotaRepository mascotaRepository,
			UsuarioRepository usuarioRepository,
			ImagenMascotaRepository imagenMascotaRepository) {
		this.mascotaRepository = mascotaRepository;
		this.usuarioRepository = usuarioRepository;
		this.imagenMascotaRepository = imagenMascotaRepository;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		Optional<Usuario> donadorOpt = usuarioRepository.findByEmail(SEED_EMAIL);
		if (donadorOpt.isEmpty()) {
			donadorOpt = usuarioRepository.findAll().stream().findFirst();
		}
		if (donadorOpt.isEmpty()) {
			log.warn("Catálogo: no hay usuarios en BD; omitiendo seed de gatos demo");
			return;
		}
		Usuario donador = donadorOpt.get();

		int creados = 0;
		for (CatalogPet spec : CATALOG) {
			Optional<Mascota> existing = mascotaRepository.findFirstByNombreIgnoreCaseOrderByIdMascotaAsc(spec.nombre());
			if (existing.isPresent()) {
				seedCatalogImagesIfMissing(existing.get(), spec.nombre());
				continue;
			}

			Mascota mascota = new Mascota();
			mascota.setUsuarioDonador(donador);
			mascota.setNombre(spec.nombre());
			mascota.setTipo("Gato");
			mascota.setRaza(spec.raza());
			mascota.setSexo(spec.sexo());
			mascota.setEstadoAdopcion(spec.estadoAdopcion());
			mascota.setActivo(true);
			mascota.setFechaPublicacion(LocalDateTime.now());
			Mascota saved = mascotaRepository.save(mascota);
			seedCatalogImagesIfMissing(saved, spec.nombre());
			creados++;
			log.info("Catálogo: mascota '{}' creada con idMascota={}", spec.nombre(), saved.getIdMascota());
		}

		if (creados > 0) {
			log.info("Catálogo: {} gatos demo insertados en adopciones_db", creados);
		}
	}

	private void seedCatalogImagesIfMissing(Mascota mascota, String nombre) {
		Integer idMascota = mascota.getIdMascota();
		if (idMascota == null) {
			return;
		}
		long count = imagenMascotaRepository.countByMascota_IdMascota(idMascota);
		if (count > 0) {
			return;
		}
		String url = "/images/mascotas/catalog/" + nombre.toLowerCase() + ".png";
		for (int i = 1; i <= 16; i++) {
			ImagenMascota img = new ImagenMascota();
			img.setMascota(mascota);
			img.setUrlImagen(url);
			img.setImagenPrincipal(i == 1);
			imagenMascotaRepository.save(img);
		}
		log.info("Catálogo: 16 imágenes creadas para '{}'", nombre);
	}

	private record CatalogPet(String nombre, String raza, String sexo, String estadoAdopcion) {
	}
}
