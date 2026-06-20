-- Ejecutar en adopciones_db si la tabla aún no existe
USE adopciones_db;

CREATE TABLE IF NOT EXISTS SolicitudAdopcion (
    idSolicitud INT AUTO_INCREMENT PRIMARY KEY,
    idMascota INT NOT NULL,
    idUsuarioSolicitante INT NOT NULL,
    estado ENUM('Pendiente', 'Aprobada', 'Rechazada') DEFAULT 'Pendiente',
    fechaSolicitud DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_solicitud_mascota
        FOREIGN KEY (idMascota)
        REFERENCES Mascota(idMascota)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_solicitud_usuario
        FOREIGN KEY (idUsuarioSolicitante)
        REFERENCES Usuario(idUsuario)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
