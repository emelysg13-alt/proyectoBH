DROP DATABASE IF EXISTS p_backhome;

CREATE DATABASE p_backhome
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE p_backhome;


CREATE TABLE tipo_documento (
    id_t_doc VARCHAR(3) PRIMARY KEY,
    n_doc VARCHAR(25) NOT NULL
);


CREATE TABLE localidades (
    id_localidad TINYINT UNSIGNED PRIMARY KEY,
    n_localidad VARCHAR(100) NOT NULL,
    poblacion INT UNSIGNED NULL
);


CREATE TABLE lugares (
    id_lugar INT PRIMARY KEY AUTO_INCREMENT,
    direccion VARCHAR(255) NOT NULL,
    localidad_id TINYINT UNSIGNED NOT NULL,

    CONSTRAINT fk_lugar_localidad
        FOREIGN KEY (localidad_id)
        REFERENCES localidades(id_localidad)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);


CREATE TABLE personas (
    id_persona INT AUTO_INCREMENT PRIMARY KEY,

    t_documento_id VARCHAR(3) NOT NULL,
    n_documento VARCHAR(20) NOT NULL UNIQUE,

    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),

    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),

    email VARCHAR(150) NOT NULL UNIQUE,
    email_verified_at TIMESTAMP NULL,

    numero_tel VARCHAR(20) NOT NULL,

    estrato TINYINT UNSIGNED NULL,

    password VARCHAR(255) NOT NULL,

    estado ENUM(
        'activo',
        'bloqueado',
        'suspendido'
    ) DEFAULT 'activo',

    remember_token VARCHAR(100) NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    foto_perfil VARCHAR(255),

    CONSTRAINT fk_persona_tipo_documento
        FOREIGN KEY (t_documento_id)
        REFERENCES tipo_documento(id_t_doc)
        ON UPDATE CASCADE
);


CREATE TABLE administrador (
    id_admin INT AUTO_INCREMENT PRIMARY KEY,

    persona_id INT NOT NULL UNIQUE,

    CONSTRAINT fk_administrador_persona
        FOREIGN KEY (persona_id)
        REFERENCES personas(id_persona)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,

    persona_id INT NOT NULL UNIQUE,

    CONSTRAINT fk_cliente_persona
        FOREIGN KEY (persona_id)
        REFERENCES personas(id_persona)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE animal (
    id_animal INT PRIMARY KEY AUTO_INCREMENT,

    nombre VARCHAR(100) NULL,

    sexo ENUM(
        'macho',
        'hembra',
        'desconocido'
    ) NOT NULL,

    color VARCHAR(50) NOT NULL,

    tamano ENUM(
        'pequeño',
        'mediano',
        'grande'
    ) NULL,

    descripcion TEXT NOT NULL
);


CREATE TABLE animal_domestico (
    id_animal_d INT AUTO_INCREMENT PRIMARY KEY,

    animal_id INT NOT NULL UNIQUE,

    especie VARCHAR(100) NOT NULL,
    raza VARCHAR(100) NOT NULL,

    CONSTRAINT fk_domestico_animal
        FOREIGN KEY (animal_id)
        REFERENCES animal(id_animal)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE animal_exotico (
    id_animal_e INT AUTO_INCREMENT PRIMARY KEY,

    animal_id INT NOT NULL UNIQUE,

    especie VARCHAR(100) NOT NULL,

    CONSTRAINT fk_exotico_animal
        FOREIGN KEY (animal_id)
        REFERENCES animal(id_animal)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE prioridades (
    id_prioridad INT AUTO_INCREMENT PRIMARY KEY,

    nombre VARCHAR(50) NOT NULL UNIQUE,

    descripcion TEXT,

    nivel TINYINT UNSIGNED NOT NULL,

    estado ENUM(
        'activo',
        'inactivo'
    ) DEFAULT 'activo'
);


INSERT INTO prioridades (
    nombre,
    descripcion,
    nivel
)
VALUES
(
    'Muy alta',
    'Animal vulnerable, pequeño, adulto mayor o desaparecido durante mucho tiempo.',
    1
),
(
    'Alta',
    'Animal que requiere atención prioritaria.',
    2
),
(
    'Media',
    'Animal que requiere seguimiento normal.',
    3
),
(
    'Normal',
    'Animal sin condiciones especiales.',
    4
);


CREATE TABLE seguimiento (
    id_seguimiento INT PRIMARY KEY AUTO_INCREMENT,

    titulo VARCHAR(150) NOT NULL,

    descripcion TEXT NOT NULL,

    fecha_publicacion DATETIME
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,

    estado_seguimiento ENUM(
        'perdido',
        'encontrado',
        'en_busqueda',
        'en_refugio',
        'reunido',
        'adoptado',
        'cerrado',
        'cancelado'
    ) NOT NULL,

    estado_moderacion ENUM(
        'pendiente',
        'aprobado',
        'rechazado'
    )
    DEFAULT 'pendiente'
    NOT NULL,

    animal_id INT NOT NULL,

    lugar_id INT NOT NULL,

    cliente_id INT NOT NULL,

    prioridad_id INT NULL,

    CONSTRAINT fk_seguimiento_animal
        FOREIGN KEY (animal_id)
        REFERENCES animal(id_animal)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_seguimiento_lugar
        FOREIGN KEY (lugar_id)
        REFERENCES lugares(id_lugar)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_seguimiento_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES cliente(id_cliente)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_seguimiento_prioridad
        FOREIGN KEY (prioridad_id)
        REFERENCES prioridades(id_prioridad)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);


CREATE TABLE seguimiento_perdido (
    id_seguimiento_perdido INT AUTO_INCREMENT PRIMARY KEY,

    seguimiento_id INT NOT NULL UNIQUE,

    fecha_perdida DATETIME NOT NULL,

    ultima_fecha_visto DATETIME NULL,

    descripcion_ultima_ubicacion TEXT,

    CONSTRAINT fk_seguimiento_perdido
        FOREIGN KEY (seguimiento_id)
        REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE seguimiento_encontrado (
    id_seguimiento_encontrado INT AUTO_INCREMENT PRIMARY KEY,

    seguimiento_id INT NOT NULL UNIQUE,

    fecha_encontrado DATETIME NOT NULL,

    descripcion_lugar_encontrado TEXT,

    necesita_refugio BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_seguimiento_encontrado
        FOREIGN KEY (seguimiento_id)
        REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE refugios (
    id_refugio INT AUTO_INCREMENT PRIMARY KEY,

    nombre VARCHAR(150) NOT NULL,

    descripcion TEXT,

    direccion VARCHAR(255) NOT NULL,

    telefono VARCHAR(20),

    email VARCHAR(150),

    localidad_id TINYINT UNSIGNED NOT NULL,

    estado ENUM(
        'activo',
        'inactivo'
    ) DEFAULT 'activo',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_refugio_localidad
        FOREIGN KEY (localidad_id)
        REFERENCES localidades(id_localidad)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);


ALTER TABLE seguimiento_encontrado

ADD COLUMN refugio_id INT NULL,

ADD CONSTRAINT fk_seguimiento_encontrado_refugio

FOREIGN KEY (refugio_id)
REFERENCES refugios(id_refugio)
ON DELETE SET NULL
ON UPDATE CASCADE;


CREATE TABLE historial_estado_seguimiento (
    id_historial INT PRIMARY KEY AUTO_INCREMENT,

    seguimiento_id INT NOT NULL,

    estado_anterior VARCHAR(50),

    estado_nuevo VARCHAR(50),

    fecha_cambio DATETIME
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_historial_seguimiento
        FOREIGN KEY (seguimiento_id)
        REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE actualizaciones_seguimiento (
    id_actualizacion INT PRIMARY KEY AUTO_INCREMENT,

    seguimiento_id INT NOT NULL,

    mensaje TEXT NOT NULL,

    created_at TIMESTAMP
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_actualizacion_seguimiento
        FOREIGN KEY (seguimiento_id)
        REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE imagenes_seguimiento (
    id_imagen INT PRIMARY KEY AUTO_INCREMENT,

    seguimiento_id INT NOT NULL,

    ruta_imagen VARCHAR(255) NOT NULL,

    imagen_principal BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_imagen_seguimiento
        FOREIGN KEY (seguimiento_id)
        REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE gestion_seguimiento (
    id_gestion INT PRIMARY KEY AUTO_INCREMENT,

    seguimiento_id INT NOT NULL,

    fecha_seguimiento DATETIME NOT NULL,

    accion VARCHAR(50) NOT NULL,

    observacion TEXT,

    administrador_id INT NOT NULL,

    CONSTRAINT fk_gestion_seguimiento
        FOREIGN KEY (seguimiento_id)
        REFERENCES seguimiento(id_seguimiento)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_gestion_administrador
        FOREIGN KEY (administrador_id)
        REFERENCES administrador(id_admin)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);


CREATE TABLE mensajes_de_soporte (
    id_mensaje INT PRIMARY KEY AUTO_INCREMENT,

    cliente_id INT NOT NULL,

    mensaje_cliente TEXT NOT NULL,

    fecha_mensaje DATETIME
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_soporte_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES cliente(id_cliente)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE consejos (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    titulo VARCHAR(255) NOT NULL,

    descripcion TEXT NOT NULL,

    created_at TIMESTAMP
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);


INSERT INTO consejos (
    titulo,
    descripcion
)
VALUES
(
    'Hidratación',
    'Mantén siempre agua fresca disponible para tu mascota.'
),
(
    'Vacunación',
    'Lleva a tu mascota al veterinario para mantener sus vacunas al día.'
),
(
    'Alimentación',
    'Proporciona una dieta equilibrada adecuada para su edad.'
),
(
    'Ejercicio',
    'Realiza paseos diarios y actividades físicas con tu mascota.'
);


CREATE TABLE control_acciones (
    id_control INT AUTO_INCREMENT PRIMARY KEY,

    descripcion TEXT NOT NULL,

    created_at TIMESTAMP
        DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE ingresos_refugio (
    id_ingreso INT AUTO_INCREMENT PRIMARY KEY,

    refugio_id INT NOT NULL,

    animal_id INT NOT NULL,

    seguimiento_id INT NOT NULL,

    fecha_ingreso DATETIME
        DEFAULT CURRENT_TIMESTAMP,

    observacion TEXT,

    estado ENUM(
        'pendiente',
        'confirmado',
        'rechazado'
    )
    DEFAULT 'pendiente',

    administrador_id INT NULL,

    fecha_verificacion DATETIME NULL,

    CONSTRAINT fk_ingreso_refugio
        FOREIGN KEY (refugio_id)
        REFERENCES refugios(id_refugio)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_ingreso_animal
        FOREIGN KEY (animal_id)
        REFERENCES animal(id_animal)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_ingreso_seguimiento
        FOREIGN KEY (seguimiento_id)
        REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_ingreso_administrador
        FOREIGN KEY (administrador_id)
        REFERENCES administrador(id_admin)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);


CREATE TABLE historial_refugio (
    id_historial_refugio INT AUTO_INCREMENT PRIMARY KEY,

    ingreso_id INT NOT NULL,

    estado_anterior VARCHAR(30),

    estado_nuevo VARCHAR(30),

    administrador_id INT NULL,

    observacion TEXT,

    fecha_cambio DATETIME
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_historial_ingreso
        FOREIGN KEY (ingreso_id)
        REFERENCES ingresos_refugio(id_ingreso)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_historial_admin
        FOREIGN KEY (administrador_id)
        REFERENCES administrador(id_admin)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);


CREATE TABLE donaciones (
    id_donacion INT AUTO_INCREMENT PRIMARY KEY,

    cliente_id INT NOT NULL,

    refugio_id INT NULL,

    monto DECIMAL(10,2) NOT NULL,

    mensaje VARCHAR(255),

    fecha_donacion DATETIME
        DEFAULT CURRENT_TIMESTAMP,

    estado ENUM(
        'pendiente',
        'confirmada',
        'cancelada'
    )
    DEFAULT 'pendiente',

    CONSTRAINT fk_donacion_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES cliente(id_cliente)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_donacion_refugio
        FOREIGN KEY (refugio_id)
        REFERENCES refugios(id_refugio)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);


CREATE TABLE coincidencias (
    id_coincidencia INT AUTO_INCREMENT PRIMARY KEY,

    seguimiento_perdido_id INT NOT NULL,

    seguimiento_encontrado_id INT NOT NULL,

    porcentaje_coincidencia DECIMAL(5,2) NULL,

    observacion TEXT,

    estado ENUM(
        'pendiente',
        'confirmada',
        'descartada'
    )
    DEFAULT 'pendiente',

    fecha_coincidencia DATETIME
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_coincidencia_perdido
        FOREIGN KEY (seguimiento_perdido_id)
        REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_coincidencia_encontrado
        FOREIGN KEY (seguimiento_encontrado_id)
        REFERENCES seguimiento(id_seguimiento)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT uq_coincidencia
        UNIQUE (
            seguimiento_perdido_id,
            seguimiento_encontrado_id
        )
);


CREATE VIEW estadisticas_localidades AS

SELECT
    l.id_localidad,
    l.n_localidad,
    l.poblacion,

    COUNT(s.id_seguimiento) AS total_seguimientos,

    CASE
        WHEN l.poblacion IS NULL
        OR l.poblacion = 0

        THEN 0

        ELSE ROUND(
            (
                COUNT(s.id_seguimiento)
                / l.poblacion
            ) * 100000,
            2
        )
    END AS seguimientos_por_100000_habitantes

FROM localidades l

LEFT JOIN lugares lu
    ON lu.localidad_id = l.id_localidad

LEFT JOIN seguimiento s
    ON s.lugar_id = lu.id_lugar

GROUP BY
    l.id_localidad,
    l.n_localidad,
    l.poblacion;


DELIMITER //

CREATE TRIGGER tr_historial_estado

AFTER UPDATE ON seguimiento

FOR EACH ROW

BEGIN

    IF NOT (OLD.estado_seguimiento <=> NEW.estado_seguimiento) THEN

        INSERT INTO historial_estado_seguimiento (
            seguimiento_id,
            estado_anterior,
            estado_nuevo,
            fecha_cambio
        )

        VALUES (
            NEW.id_seguimiento,
            OLD.estado_seguimiento,
            NEW.estado_seguimiento,
            CURRENT_TIMESTAMP
        );

    END IF;

END //

DELIMITER ;


DELIMITER //

CREATE TRIGGER tr_seguimiento_eliminado

BEFORE DELETE ON seguimiento

FOR EACH ROW

BEGIN

    INSERT INTO control_acciones (
        descripcion
    )

    VALUES (
        CONCAT(
            'Se eliminó el seguimiento ID: ',
            OLD.id_seguimiento,
            ' - Título: ',
            OLD.titulo
        )
    );

END //

DELIMITER ;


DELIMITER //

CREATE TRIGGER tr_historial_ingreso_refugio

AFTER UPDATE ON ingresos_refugio

FOR EACH ROW

BEGIN

    IF NOT (OLD.estado <=> NEW.estado) THEN

        INSERT INTO historial_refugio (
            ingreso_id,
            estado_anterior,
            estado_nuevo,
            administrador_id,
            observacion
        )

        VALUES (
            NEW.id_ingreso,
            OLD.estado,
            NEW.estado,
            NEW.administrador_id,
            NEW.observacion
        );

    END IF;

END //

DELIMITER ;


DELIMITER //

CREATE PROCEDURE agregar_actualizacion (
    IN p_seguimiento_id INT,
    IN p_mensaje TEXT
)

BEGIN

    DECLARE v_existe INT DEFAULT 0;

    SELECT COUNT(*)
    INTO v_existe

    FROM seguimiento

    WHERE id_seguimiento = p_seguimiento_id;

    IF v_existe = 0 THEN

        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
        'Seguimiento no encontrado';

    ELSE

        INSERT INTO actualizaciones_seguimiento (
            seguimiento_id,
            mensaje,
            created_at,
            updated_at
        )

        VALUES (
            p_seguimiento_id,
            p_mensaje,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );

    END IF;

END //

DELIMITER ;


DELIMITER //

CREATE PROCEDURE registrar_ingreso_refugio (
    IN p_refugio_id INT,
    IN p_animal_id INT,
    IN p_seguimiento_id INT,
    IN p_observacion TEXT
)

BEGIN

    INSERT INTO ingresos_refugio (
        refugio_id,
        animal_id,
        seguimiento_id,
        observacion,
        estado
    )

    VALUES (
        p_refugio_id,
        p_animal_id,
        p_seguimiento_id,
        p_observacion,
        'pendiente'
    );

END //

DELIMITER ;


DELIMITER //

CREATE PROCEDURE cambiar_estado_seguimiento (
    IN p_seguimiento_id INT,
    IN p_nuevo_estado VARCHAR(30)
)

BEGIN

    DECLARE v_existe INT DEFAULT 0;

    SELECT COUNT(*)
    INTO v_existe

    FROM seguimiento

    WHERE id_seguimiento = p_seguimiento_id;

    IF v_existe = 0 THEN

        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
        'El seguimiento no existe';

    ELSE

        UPDATE seguimiento

        SET estado_seguimiento = p_nuevo_estado

        WHERE id_seguimiento = p_seguimiento_id;

    END IF;

END //

DELIMITER ;