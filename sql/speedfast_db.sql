-- =====================================================
-- SpeedFast - Script de base de datos (Semana 8)
-- Crea la base speedfast_db, sus tablas y datos de ejemplo
-- =====================================================

CREATE DATABASE IF NOT EXISTS speedfast_db;
USE speedfast_db;

-- Se eliminan en orden inverso por las claves foraneas
DROP TABLE IF EXISTS entregas;
DROP TABLE IF EXISTS pedidos;
DROP TABLE IF EXISTS repartidores;

CREATE TABLE repartidores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE pedidos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    direccion VARCHAR(100) NOT NULL,
    tipo ENUM('COMIDA','ENCOMIENDA','EXPRESS'),
    estado ENUM('PENDIENTE','EN_REPARTO','ENTREGADO')
);

CREATE TABLE entregas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido INT,
    id_repartidor INT,
    fecha DATE,
    hora TIME,
    FOREIGN KEY (id_pedido) REFERENCES pedidos(id),
    FOREIGN KEY (id_repartidor) REFERENCES repartidores(id)
);

-- Datos de ejemplo
INSERT INTO repartidores (nombre) VALUES
    ('Camila Rojas'),
    ('Diego Fuentes'),
    ('Valentina Soto');

INSERT INTO pedidos (direccion, tipo, estado) VALUES
    ('Av. Argentina 120, Los Andes', 'COMIDA', 'ENTREGADO'),
    ('Esmeralda 455, San Felipe', 'ENCOMIENDA', 'EN_REPARTO'),
    ('Maipu 88, Los Andes', 'EXPRESS', 'PENDIENTE'),
    ('Santa Teresa 1020, Los Andes', 'COMIDA', 'PENDIENTE');

INSERT INTO entregas (id_pedido, id_repartidor, fecha, hora) VALUES
    (1, 1, '2026-09-30', '13:45:00');
