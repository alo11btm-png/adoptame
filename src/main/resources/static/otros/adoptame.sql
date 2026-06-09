-- ============================================================
-- RECREAR adopciones_db con el esquema oficial (Usuario, Mascota, ImagenMascota)
-- ATENCIÓN: borra todas las tablas y datos actuales (usuario, mascota, imagenmascota)
-- Ejecutar en MySQL Workbench
-- ============================================================
DROP database adopciones_db;
CREATE DATABASE IF NOT EXISTS adopciones_db;
USE adopciones_db;

SET FOREIGN_KEY_CHECKS = 0;

-- Tablas viejas creadas por Hibernate (minúsculas)
DROP TABLE IF EXISTS imagenmascota;
DROP TABLE IF EXISTS mascota;
DROP TABLE IF EXISTS usuario;

-- Por si existieran con mayúsculas
DROP TABLE IF EXISTS ImagenMascota;
DROP TABLE IF EXISTS Mascota;
DROP TABLE IF EXISTS Usuario;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================
-- TABLA: Usuario
-- =========================================
CREATE TABLE Usuario (
    idUsuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellidoPaterno VARCHAR(100) NOT NULL,
    apellidoMaterno VARCHAR(100),
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    activo TINYINT(1) DEFAULT 1,
    fechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =========================================
-- TABLA: Mascota
-- =========================================
CREATE TABLE Mascota (
    idMascota INT AUTO_INCREMENT PRIMARY KEY,
    idUsuarioDonador INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    raza VARCHAR(100),
    sexo ENUM('Macho', 'Hembra') NOT NULL,
    edadAproximada VARCHAR(50),
    descripcion TEXT,
    estadoAdopcion ENUM('Disponible', 'Adoptado', 'En proceso') DEFAULT 'Disponible',
    activo TINYINT(1) DEFAULT 1,
    fechaPublicacion DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mascota_usuario
        FOREIGN KEY (idUsuarioDonador)
        REFERENCES Usuario(idUsuario)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- =========================================
-- TABLA: ImagenMascota
-- =========================================
CREATE TABLE ImagenMascota (
    idImagen INT AUTO_INCREMENT PRIMARY KEY,
    idMascota INT NOT NULL,
    urlImagen VARCHAR(255) NOT NULL,
    imagenPrincipal TINYINT(1) DEFAULT 0,
    fechaSubida DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_imagen_mascota
        FOREIGN KEY (idMascota)
        REFERENCES Mascota(idMascota)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- =========================================
-- Usuario y gatos de ejemplo
-- =========================================
INSERT INTO Usuario (nombre, apellidoPaterno, email, password, activo)
VALUES ('AdoptaMe', 'Alondra', 'alo11btm@gmail.com', 'Alo', 1);

SET @donador_id = LAST_INSERT_ID();

INSERT INTO Mascota (idUsuarioDonador, nombre, tipo, raza, sexo, estadoAdopcion, activo) VALUES
(@donador_id, 'Mishi', 'Gato', 'Siamés', 'Hembra', 'En proceso', 1),
(@donador_id, 'Luna', 'Gato', 'Persa', 'Hembra', 'Disponible', 1),
(@donador_id, 'Simba', 'Gato', 'Maine Coon', 'Macho', 'En proceso', 1),
(@donador_id, 'Nina', 'Gato', 'Bengalí', 'Hembra', 'Disponible', 1),
(@donador_id, 'Oreo', 'Gato', 'Doméstico', 'Macho', 'En proceso', 1),
(@donador_id, 'Kira', 'Gato', 'Azul Ruso', 'Hembra', 'En proceso', 1);

-- 16 imágenes del gatito por mascota de catálogo (misma URL, como en la maqueta)
INSERT INTO ImagenMascota (idMascota, urlImagen, imagenPrincipal)
SELECT m.idMascota,
       CONCAT('/images/mascotas/catalog/', LOWER(m.nombre), '.png'),
       IF(n.num = 1, 1, 0)
FROM Mascota m
CROSS JOIN (
    SELECT 1 AS num UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
    SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
    SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL
    SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16
) n
WHERE m.nombre IN ('Mishi', 'Luna', 'Simba', 'Nina', 'Oreo', 'Kira');

-- Verificación
SHOW TABLES;
DESCRIBE Mascota;
SELECT idMascota, idUsuarioDonador, nombre, tipo FROM Mascota;
SELECT m.nombre, i.urlImagen, i.imagenPrincipal FROM Mascota m
LEFT JOIN ImagenMascota i ON i.idMascota = m.idMascota
ORDER BY m.idMascota, i.idImagen;