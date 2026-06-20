package mx.edu.unpa.adoptame.controller;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mx.edu.unpa.adoptame.domain.Mascota;
import mx.edu.unpa.adoptame.dto.MascotaRegistroRequest;
import mx.edu.unpa.adoptame.service.MascotaService;

@RestController
@RequestMapping("/mascota")
public class MascotaController {

	@Autowired
	private MascotaService mascotaService;

	@GetMapping
	public Iterable<Mascota> queryAll() {
		return mascotaService.findAll();
	}

	@GetMapping("/usuario/{idUsuario}")
	public ResponseEntity<Iterable<Mascota>> queryByUsuario(@PathVariable("idUsuario") Integer idUsuario) {
		try {
			return ResponseEntity.ok(mascotaService.findByUsuarioId(idUsuario));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/{idMascota}")
	public ResponseEntity<Mascota> queryById(@PathVariable(value = "idMascota") Integer id) {
		Optional<Mascota> oMascota = mascotaService.findById(id);
		if (oMascota.isPresent()) {
			return ResponseEntity.ok(oMascota.get());
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	@GetMapping("/name")
	public ResponseEntity<Mascota> queryByName(@RequestParam(value = "name") String name) {
		Optional<Mascota> oMascota = mascotaService.findByName(name);
		if (oMascota.isPresent()) {
			return ResponseEntity.ok(oMascota.get());
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	@PostMapping
	public ResponseEntity<?> insert(@RequestBody MascotaRegistroRequest request) {
		try {
			Mascota savedMascota = mascotaService.save(request.toEntity());
			return ResponseEntity.status(HttpStatus.CREATED).body(savedMascota);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}

	@PatchMapping("/{idMascota}/estado")
	public ResponseEntity<?> updateEstado(
			@PathVariable(value = "idMascota") Integer idMascota,
			@RequestBody Map<String, Object> body) {
		try {
			String estado = body.get("estadoAdopcion") instanceof String s ? s : null;
			Integer idUsuario = parseIdUsuario(body.get("idUsuario"));
			Mascota updated = mascotaService.updateEstadoAdopcion(idMascota, estado, idUsuario);
			return ResponseEntity.ok(updated);
		} catch (IllegalStateException ex) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}

	private Integer parseIdUsuario(Object value) {
		if (value instanceof Number number) {
			return number.intValue();
		}
		if (value instanceof String text && !text.isBlank()) {
			return Integer.parseInt(text.trim());
		}
		return null;
	}

	@PutMapping("/{idMascota}")
	public ResponseEntity<Mascota> update(@PathVariable(value = "idMascota") Integer id, @RequestBody Mascota mascota) {
		Optional<Mascota> oMascota = mascotaService.findById(id);
		if (oMascota.isPresent()) {
			mascota.setIdMascota(id);
			Mascota updated = mascotaService.save(mascota);
			return ResponseEntity.ok(updated);
		}
		return ResponseEntity.notFound().build();
	}

	@DeleteMapping("/{idMascota}")
	public ResponseEntity<Void> delete(@PathVariable(value = "idMascota") Integer id) {
		Optional<Mascota> oMascota = mascotaService.findById(id);
		if (oMascota.isPresent()) {
			mascotaService.deleteById(id);
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}
}
