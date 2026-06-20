package mx.edu.unpa.adoptame.service;

import java.util.List;

import mx.edu.unpa.adoptame.domain.SolicitudAdopcion;

public interface SolicitudAdopcionService {

	SolicitudAdopcion crear(Integer idMascota, Integer idUsuarioSolicitante);

	List<SolicitudAdopcion> findRecibidasPendientes(Integer idUsuario);

	List<SolicitudAdopcion> findEnviadas(Integer idUsuario);

	SolicitudAdopcion aprobar(Integer idSolicitud, Integer idUsuarioOwner);
}
