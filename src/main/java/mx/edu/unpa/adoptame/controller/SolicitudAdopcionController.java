package mx.edu.unpa.adoptame.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mx.edu.unpa.adoptame.domain.SolicitudAdopcion;
import mx.edu.unpa.adoptame.service.SolicitudAdopcionService;

@RestController
@RequestMapping("/solicitudAdopcion")
public class SolicitudAdopcionController {

	@Autowired
	private SolicitudAdopcionService solicitudService;

	@PostMapping
	public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
		try {
			Integer idMascota = parseInt(body.get("idMascota"));
			Integer idUsuarioSolicitante = parseInt(body.get("idUsuarioSolicitante"));
			SolicitudAdopcion created = solicitudService.crear(idMascota, idUsuarioSolicitante);
			return ResponseEntity.status(HttpStatus.CREATED).body(created);
		} catch (IllegalStateException ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}

	@GetMapping("/recibidas/{idUsuario}")
	public ResponseEntity<List<SolicitudAdopcion>> recibidas(@PathVariable("idUsuario") Integer idUsuario) {
		try {
			return ResponseEntity.ok(solicitudService.findRecibidasPendientes(idUsuario));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/enviadas/{idUsuario}")
	public ResponseEntity<List<SolicitudAdopcion>> enviadas(@PathVariable("idUsuario") Integer idUsuario) {
		try {
			return ResponseEntity.ok(solicitudService.findEnviadas(idUsuario));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PatchMapping("/{idSolicitud}/aprobar")
	public ResponseEntity<?> aprobar(
			@PathVariable("idSolicitud") Integer idSolicitud,
			@RequestBody Map<String, Object> body) {
		try {
			Integer idUsuario = parseInt(body.get("idUsuario"));
			SolicitudAdopcion updated = solicitudService.aprobar(idSolicitud, idUsuario);
			return ResponseEntity.ok(updated);
		} catch (IllegalStateException ex) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}

	private Integer parseInt(Object value) {
		if (value instanceof Number number) {
			return number.intValue();
		}
		if (value instanceof String text && !text.isBlank()) {
			return Integer.parseInt(text.trim());
		}
		return null;
	}
}
