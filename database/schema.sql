CREATE DATABASE IF NOT EXISTS gimnasio_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gimnasio_db;

CREATE DATABASE IF NOT EXISTS gimnasio_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gimnasio_db;

-- ============================================================
-- TABLA: usuarios
-- Mapea: Persona (nombre, edad, correo, telefono) + Usuario
-- ============================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario        INT AUTO_INCREMENT PRIMARY KEY,      -- PK real, se genera sola
    numero_socio      INT NOT NULL UNIQUE,                 -- el número que elige el socio (101, 102...)
    nombre_completo   VARCHAR(100) NOT NULL,
    edad              INT NOT NULL,
    correo_electronico VARCHAR(100) NOT NULL,
    telefono          INT NOT NULL,                        -- se mantiene INT porque tu clase usa int
    contacto_emergencia VARCHAR(100) NULL,
    condiciones_medicas VARCHAR(200) NULL,
    pago_al_dia       BOOLEAN NOT NULL DEFAULT FALSE,      -- sincronizado desde la app (ver nota 2)
    CONSTRAINT chk_edad CHECK (edad >= 15 AND edad <= 100)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: planes   (opcional pero recomendada)
-- Mapea: PlanBase (nombre, precioBase, beneficios, duracionDias)
-- ============================================================
CREATE TABLE IF NOT EXISTS planes (
    id_plan       INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(50) NOT NULL UNIQUE,             -- 'Mensual', 'Anual', 'VIP'
    precio_base   DECIMAL(10,2) NOT NULL,
    beneficios    VARCHAR(255) NOT NULL,
    duracion_dias INT NOT NULL
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: membresias   (FK a usuarios y planes)
-- Mapea: Membresia (usuario, plan, fechaVencimiento)
-- ============================================================
CREATE TABLE IF NOT EXISTS membresias (
    id_membresia       INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario         INT NOT NULL UNIQUE,                -- UNIQUE = 1 membresía por socio (nota 3)
    id_plan            INT NOT NULL,
    fecha_vencimiento  DATE NOT NULL,
    CONSTRAINT fk_membresia_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_membresia_plan
        FOREIGN KEY (id_plan) REFERENCES planes(id_plan)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: pagos   (FK a membresias)
-- Mapea: Pago (fecha, monto) + el historialPagos de Membresia
-- ============================================================
CREATE TABLE IF NOT EXISTS pagos (
    id_pago       INT AUTO_INCREMENT PRIMARY KEY,
    id_membresia  INT NOT NULL,
    fecha_pago    DATE NOT NULL,
    monto         DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_pago_membresia
        FOREIGN KEY (id_membresia) REFERENCES membresias(id_membresia)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- Datos de prueba (los mismos del Proyecto 1)
-- ============================================================
-- Los precios coinciden con el cálculo de las clases Java:
-- PlanMensual: 25000.0, 30 dias
-- PlanAnual: 25000*12*(1-0.15) = 255000.0, 365 dias
-- PlanVIP: 25000+15000 = 40000.0, 30 dias
INSERT INTO planes (nombre, precio_base, beneficios, duracion_dias) VALUES
    ('Mensual', 25000.00, 'Acceso a sala de pesas y cardio', 30),
    ('Anual',  255000.00, 'Acceso a sala de pesas, cardio y clases grupales', 365),
    ('VIP',    40000.00,  'Acceso a clases especiales, casillero y entrenador personal', 30);

INSERT INTO usuarios (numero_socio, nombre_completo, edad, correo_electronico, telefono, pago_al_dia)
VALUES (101, 'María',  28, 'maria@email.com',  88881111, TRUE),
       (102, 'Carlos', 35, 'carlos@email.com', 88882222, FALSE),
       (103, 'Lucía',  22, 'lucia@email.com',  88883333, TRUE),
       (104, 'Jorge',  41, 'jorge@email.com',  88884444, FALSE);

-- María tiene plan Mensual, Lucía plan Anual (Carlos y Jorge sin membresía, como en el Proyecto 1)
INSERT INTO membresias (id_usuario, id_plan, fecha_vencimiento)
VALUES ((SELECT id_usuario FROM usuarios WHERE numero_socio = 101), 1, DATE_ADD(CURDATE(), INTERVAL 20 DAY)),
       ((SELECT id_usuario FROM usuarios WHERE numero_socio = 103), 2, DATE_ADD(CURDATE(), INTERVAL 300 DAY));

INSERT INTO pagos (id_membresia, fecha_pago, monto)
VALUES (1, CURDATE(), 15000.00),
       (2, CURDATE(), 150000.00);
       
       
SHOW DATABASES;          -- debe aparecer gimnasio_db
USE gimnasio_db;
SHOW TABLES;             -- debe listar usuarios, planes, membresias, pagos
SELECT * FROM usuarios;  -- deben salir María, Carlos, Lucía, Jorge

SELECT numero_socio, nombre_completo FROM usuarios;  -- deben estar 101 y 103