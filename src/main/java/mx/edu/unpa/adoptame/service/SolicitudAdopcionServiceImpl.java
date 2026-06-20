package mx.edu.unpa.adoptame.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.edu.unpa.adoptame.domain.Mascota;
import mx.edu.unpa.adoptame.domain.SolicitudAdopcion;
import mx.edu.unpa.adoptame.domain.Usuario;
import mx.edu.unpa.adoptame.repository.MascotaRepository;
import mx.edu.unpa.adoptame.repository.SolicitudAdopcionRepository;
import mx.edu.unpa.adoptame.repository.UsuarioRepository;

@Service
public class SolicitudAdopcionServiceImpl implements SolicitudAdopcionService {

	private static final Logger log = LoggerFactory.getLogger(SolicitudAdopcionServiceImpl.class);

	@Autowired
	private SolicitudAdopcionRepository solicitudRepository;

	@Autowired
	private MascotaRepository mascotaRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Override
	@Transactional
	public SolicitudAdopcion crear(Integer idMascota, Integer idUsuarioSolicitante) {
		if (idMascota == null || idMascota <= 0) {
			throw new IllegalArgumentException("idMascota inválido");
		}
		if (idUsuarioSolicitante == null || idUsuarioSolicitante <= 0) {
			throw new IllegalArgumentException("idUsuarioSolicitante inválido");
		}

		Mascota mascota = mascotaRepository.findById(idMascota).orElseThrow(() ->
				new IllegalArgumentException("Mascota no encontrada: " + idMascota));

		Usuario solicitante = usuarioRepository.findById(idUsuarioSolicitante).orElseThrow(() ->
				new IllegalArgumentException("Usuario no encontrado: " + idUsuarioSolicitante));

		Integer ownerId = mascota.getIdUsuarioDonador();
		if (ownerId != null && ownerId.equals(idUsuarioSolicitante)) {
			throw new IllegalStateException("No puedes solicitar la adopción de tu propia mascota");
		}

		if ("Adoptado".equalsIgnoreCase(mascota.getEstadoAdopcion())) {
			throw new IllegalStateException("Esta mascota ya fue adoptada");
		}

		boolean yaPendiente = solicitudRepository.existsByMascota_IdMascotaAndUsuarioSolicitante_IdUsuarioAndEstado(
				idMascota, idUsuarioSolicitante, "Pendiente");
		if (yaPendiente) {
			throw new IllegalStateException("Ya tienes una solicitud pendiente para esta mascota");
		}

		SolicitudAdopcion solicitud = new SolicitudAdopcion();
		solicitud.setMascota(mascota);
		solicitud.setUsuarioSolicitante(solicitante);
		solicitud.setEstado("Pendiente");
		solicitud.setFechaSolicitud(LocalDateTime.now());

		SolicitudAdopcion saved = solicitudRepository.save(solicitud);
		log.info("Solicitud de adopción creada: idSolicitud={} mascota={} solicitante={}",
				saved.getIdSolicitud(), idMascota, idUsuarioSolicitante);
		return saved;
	}

	@Override
	@Transactional(readOnly = true)
	public List<SolicitudAdopcion> findRecibidasPendientes(Integer idUsuario) {
		if (idUsuario == null || idUsuario <= 0) {
			throw new IllegalArgumentException("idUsuario inválido");
		}
		return solicitudRepository.findPendientesRecibidas(idUsuario);
	}

	@Override
	@Transactional(readOnly = true)
	public List<SolicitudAdopcion> findEnviadas(Integer idUsuario) {
		if (idUsuario == null || idUsuario <= 0) {
			throw new IllegalArgumentException("idUsuario inválido");
		}
		return solicitudRepository.findEnviadas(idUsuario);
	}

	@Override
	@Transactional
	public SolicitudAdopcion aprobar(Integer idSolicitud, Integer idUsuarioOwner) {
		if (idSolicitud == null || idSolicitud <= 0) {
			throw new IllegalArgumentException("idSolicitud inválido");
		}
		if (idUsuarioOwner == null || idUsuarioOwner <= 0) {
			throw new IllegalArgumentException("idUsuario inválido");
		}

		SolicitudAdopcion solicitud = solicitudRepository.findByIdSolicitud(idSolicitud).orElseThrow(() ->
				new IllegalArgumentException("Solicitud no encontrada: " + idSolicitud));

		if (!"Pendiente".equalsIgnoreCase(solicitud.getEstado())) {
			throw new IllegalStateException("La solicitud ya fue procesada");
		}

		Mascota mascota = solicitud.getMascotaEntity();
		if (mascota == null) {
			throw new IllegalStateException("Mascota asociada no encontrada");
		}

		Integer ownerId = mascota.getIdUsuarioDonador();
		if (ownerId == null || !ownerId.equals(idUsuarioOwner)) {
			log.warn("Usuario {} intentó aprobar solicitud {} sin ser dueño", idUsuarioOwner, idSolicitud);
			throw new IllegalStateException("Solo el dueño puede aprobar la adopción");
		}

		if ("Adoptado".equalsIgnoreCase(mascota.getEstadoAdopcion())) {
			throw new IllegalStateException("Esta mascota ya fue adoptada");
		}

		solicitud.setEstado("Aprobada");
		solicitudRepository.save(solicitud);

		mascota.setEstadoAdopcion("Adoptado");
		mascotaRepository.save(mascota);

		List<SolicitudAdopcion> otrasPendientes = solicitudRepository
				.findByMascota_IdMascotaAndEstado(mascota.getIdMascota(), "Pendiente");
		for (SolicitudAdopcion otra : otrasPendientes) {
			if (!otra.getIdSolicitud().equals(idSolicitud)) {
				otra.setEstado("Rechazada");
				solicitudRepository.save(otra);
			}
		}

		log.info("Solicitud {} aprobada; mascota {} marcada como Adoptado", idSolicitud, mascota.getIdMascota());
		return solicitud;
	}
}
