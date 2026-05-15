# EcoMerk2 — Usuarios Service

Microservicio de usuarios para **EcoMerk2**, una plataforma móvil de comparación de precios y asistente de recetas con IA.

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Seguridad | Spring Security + JWT (jjwt) |
| Base de datos | PostgreSQL |
| Persistencia | Spring Data JPA / Hibernate |
| Validación | Jakarta Validation |
| Cifrado E2E | RSA 2048 + AES-128 CBC |
| Build | Maven |
| IA | OpenRouter (GPT / Gemini) |
| Despliegue | Railway + Docker |

## Arquitectura

```
src/main/java/co/uceva/usuariosservice/
├── delivery/rest/       # Controladores REST
├── delivery/exception/  # Manejador global de excepciones
├── domain/model/        # Entidades JPA y DTOs
├── domain/repository/   # Repositorios JPA
├── domain/service/      # Lógica de negocio
├── domain/exception/    # Excepciones de dominio
└── infrastructure/      # Seguridad, config, utilidades
    ├── security/        # JWT, RSA, AES, cifrado
    └── config/          # Beans de configuración
```

## Funcionalidades

- **Gestión de usuarios** — registro, login, CRUD con roles (USER/ADMIN)
- **Favoritos** — sincronización de productos favoritos por usuario
- **Chat con IA** — chat contextual con EcoIA vía OpenRouter
- **Historial de precios** — registro y consulta de precios de productos
- **Cifrado E2E obligatorio** — toda petición con body y toda respuesta viaja cifrada con RSA 2048 + AES-128 CBC
- **Refresh tokens** — rotación de tokens con almacenamiento hasheado (SHA-256)

## Documentación de la API

→ **[docs.md](docs.md)** — Documentación completa de endpoints, ejemplos de entrada/salida con cifrado, códigos de error y guía de implementación para el frontend Flutter.

---

## Ejecutar con Docker

### Prerrequisitos

- Docker
- Archivo `.env` con las variables de entorno (ver sección [Configuración](#configuración))

### docker-compose (producción local)

El archivo [`compose.yaml`](compose.yaml) levanta PostgreSQL y el microservicio:

```yaml
services:
  postgres:
    image: 'postgres:latest'
    volumes:
      - postgres_data:/var/lib/postgresql/data
    environment:
      - POSTGRES_DB=${POSTGRES_DB:-usuarios}
      - POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
      - POSTGRES_USER=${POSTGRES_USER:-devdb}
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $POSTGRES_USER -d $POSTGRES_DB"]
      interval: 10s
      timeout: 5s
      retries: 5
    ports:
      - '5432'

  usuarios-service:
    image: joseloaiza01/usuarios-bd-1.0:latest
    restart: always
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}
      - SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
      - JWT_SECRET=${JWT_SECRET:-Secret_Key_Para_Local}
      - JPA_DDL=update
      - PORT=8080
      - OPENROUTER_API_KEY=${OPENROUTER_API_KEY}

volumes:
  postgres_data:
```

### Construir la imagen manualmente

El `Dockerfile` usa `amazoncorretto:21-alpine` y el JAR preconstruido:

```dockerfile
FROM amazoncorretto:21-alpine
WORKDIR /app
COPY target/usuarios-bd-1.0.jar /app
ENTRYPOINT ["java", "-jar", "usuarios-bd-1.0.jar"]
```

> **Nota:** el `Dockerfile` está en `.gitignore` porque la imagen se publica directo a Docker Hub. Para construir localmente:
>
> ```bash
> ./mvnw clean package -DskipTests
> docker build -t usuarios-bd .
> ```

### Ejecutar con Compose

```bash
# 1. Crear archivo .env (ver sección de variables)
cat > .env << EOF
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/usuarios
SPRING_DATASOURCE_USERNAME=devdb
SPRING_DATASOURCE_PASSWORD=tu_password
OPENROUTER_API_KEY=sk-...
EOF

# 2. Levantar todo
docker compose up -d
```

La API queda disponible en `http://localhost:8080/api/v1`.

---

## Configuración

Variables de entorno disponibles:

| Variable | Descripción | Default |
|---|---|---|
| `PORT` | Puerto del servidor | `8080` |
| `JWT_SECRET` | Secreto para firmar JWT | `Secret_Key_Para_Local` |
| `JWT_REFRESH_SECRET` | Secreto para hash de refresh tokens | `default_refresh_secret` |
| `jwt.expiration` | Duración del access token (ms) | `900000` (15 min) |
| `jwt.refresh-expiration` | Duración del refresh token (ms) | `86400000` (1 día) |
| `SPRING_DATASOURCE_URL` | URL JDBC de PostgreSQL | — |
| `SPRING_DATASOURCE_USERNAME` | Usuario BD | — |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña BD | — |
| `OPENROUTER_API_KEY` | API key de OpenRouter | — |
| `OPENROUTER_MODEL` | Modelo de IA | `openai/gpt-oss-120b:free` |
| `JPA_DDL` | Estrategia DDL de Hibernate | `update` |

## Ejecutar localmente (sin Docker)

```bash
# Requiere Java 21+ y PostgreSQL corriendo en localhost:5432
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/usuarios
export SPRING_DATASOURCE_USERNAME=devdb
export SPRING_DATASOURCE_PASSWORD=pass

./mvnw spring-boot:run
```

## Licencia

Proyecto académico — UCEVA
