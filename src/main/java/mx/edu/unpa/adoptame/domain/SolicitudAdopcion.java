package mx.edu.unpa.adoptame.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "SolicitudAdopcion")
public class SolicitudAdopcion implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idSolicitud")
	@JsonProperty("idSolicitud")
	private Integer idSolicitud;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idMascota", nullable = false)
	@JsonIgnore
	private Mascota mascota;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idUsuarioSolicitante", nullable = false)
	@JsonIgnore
	private Usuario usuarioSolicitante;

	@Column(name = "estado", columnDefinition = "ENUM('Pendiente', 'Aprobada', 'Rechazada')")
	private String estado;

	@Column(name = "fechaSolicitud")
	private LocalDateTime fechaSolicitud;

	@JsonProperty("idMascota")
	public Integer getIdMascota() {
		return mascota != null ? mascota.getIdMascota() : null;
	}

	@JsonProperty("nombreMascota")
	public String getNombreMascota() {
		return mascota != null ? mascota.getNombre() : null;
	}

	@JsonProperty("idUsuarioSolicitante")
	public Integer getIdUsuarioSolicitante() {
		return usuarioSolicitante != null ? usuarioSolicitante.getIdUsuario() : null;
	}

	@JsonProperty("nombreSolicitante")
	public String getNombreSolicitante() {
		if (usuarioSolicitante == null) {
			return null;
		}
		String apellido = usuarioSolicitante.getApellidoPaterno() != null
				? usuarioSolicitante.getApellidoPaterno()
				: "";
		return (usuarioSolicitante.getNombre() + " " + apellido).trim();
	}

	@JsonProperty("emailSolicitante")
	public String getEmailSolicitante() {
		return usuarioSolicitante != null ? usuarioSolicitante.getEmail() : null;
	}

	@JsonIgnore
	public Mascota getMascotaEntity() {
		return mascota;
	}

	@JsonIgnore
	public Usuario getUsuarioSolicitanteEntity() {
		return usuarioSolicitante;
	}
}
