-- Tabla de favoritos por usuario: cada fila asocia usuario_id con link del producto
-- y un flag para notificaciones. Creada para inicializar DB en entornos nuevos.
CREATE TABLE IF NOT EXISTS usuario_favoritos (
	usuario_id BIGINT NOT NULL,
	link TEXT NOT NULL,
	notificaciones BOOLEAN NOT NULL DEFAULT false,
	CONSTRAINT fk_usuario_favoritos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
