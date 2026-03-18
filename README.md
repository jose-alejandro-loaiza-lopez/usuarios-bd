# 🛒 Microservicio: Usuarios-Service (EcoMerca2)

Este microservicio es el núcleo de gestión de perfiles para la aplicación **EcoMerca2**. Se encarga de la autenticación segura, el almacenamiento de preferencias del usuario y la sincronización de datos (como alimentos favoritos) entre dispositivos.

## 🚀 Tecnologías utilizadas

* **Java 21** (Amazon Corretto)
* **Spring Boot 3**
* **Spring Security** (BCrypt para contraseñas)
* **PostgreSQL**
* **Docker & Docker Compose**

## 🧪 Flujo de Pruebas (Base de datos vacía)

Sigue este orden exacto para probar el sistema desde cero:

### 1️⃣ Registro de Usuario (Abierto)

Como la base de datos está vacía, primero creamos al usuario. Este endpoint no requiere token.

```bash
curl -X POST http://localhost:8080/api/v1/usuarios/ \
-H "Content-Type: application/json" \
-d '{
  "nombre": "Jose Alejandro",
  "email": "jose@ecomerca.com",
  "password": "password123",
  "fechaNacimiento": "2000-10-25"
}'

```

### 2️⃣ Inicio de Sesión (Login)

Obtén tu llave de acceso (Token). **Copia el valor del "token" que recibas.**

```bash
curl -X POST http://localhost:8080/api/v1/usuarios/login \
-H "Content-Type: application/json" \
-d '{
  "email": "jose@ecomerca.com",
  "password": "password123"
}'

```

### 3️⃣ Acceso a Endpoints Protegidos

Para cualquier otra operación, debes incluir el token en la cabecera. Sustituye `TU_TOKEN` por el código que obtuviste en el paso anterior.

#### 🔹 Listar todos los usuarios

```bash
curl -X GET http://localhost:8080/api/v1/usuarios/ \
-H "Authorization: Bearer TU_TOKEN"

```

#### 🔹 Actualizar alimentos favoritos (PATCH)

Sincroniza los gustos del usuario sin modificar el resto del perfil.

```bash
curl -X PATCH http://localhost:8080/api/v1/usuarios/1/favoritos \
-H "Authorization: Bearer TU_TOKEN" \
-H "Content-Type: application/json" \
-d '["Pollo", "Tomate", "Lentejas"]'

```

#### 🔹 Obtener perfil por ID

```bash
curl -X GET http://localhost:8080/api/v1/usuarios/1 \
-H "Authorization: Bearer TU_TOKEN"

```

## 📦 Endpoints REST

Base URL: `/api/v1/usuarios`

### 🔹 Obtener todos los usuarios

```bash
curl -X GET http://localhost:8080/api/v1/usuarios/

```

### 🔹 Obtener usuarios paginados

```bash
curl -X GET http://localhost:8080/api/v1/usuarios/page/0

```

### 🔹 Obtener un usuario por ID

```bash
curl -X GET http://localhost:8080/api/v1/usuarios/1

```

### 🔹 Registrar un usuario (EcoMerca2)

*Nota: La contraseña se encriptará automáticamente en el servidor.*

```bash
curl -X POST http://localhost:8080/api/v1/usuarios/ \
-H "Content-Type: application/json" \
-d '{
  "nombre": "Jose Alejandro",
  "email": "jose@ecomerca.com",
  "password": "password123",
  "fechaNacimiento": "2000-10-25"
}'

```

### 🔹 Actualizar perfil o favoritos

```bash
curl -X PUT http://localhost:8080/api/v1/usuarios/ \
-H "Content-Type: application/json" \
-d '{
  "id": 1,
  "nombre": "Jose Alejandro",
  "email": "jose@ecomerca.com",
  "password": "newpassword123",
  "fechaNacimiento": "2000-10-25",
  "alimentosFavoritos": ["Manzana", "Pollo", "Arroz", "Lentejas"]
}'

```

### 🔹 Eliminar un usuario

```bash
curl -X DELETE http://localhost:8080/api/v1/usuarios/1

```

### 🔹 Actualizar listado de favoritos

```bash
curl -X PATCH http://localhost:8080/api/v1/usuarios/1/favoritos \
-H "Content-Type: application/json" \
-d '["Pollo", "Tomate", "Cerveza"]'
```

## 🗃️ Modelo de Datos (JPA)

```java
@Entity
public class Usuarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    
    @Column(unique = true)
    private String email;

    private String password; // Almacenado como Hash BCrypt

    private LocalDate fechaNacimiento;

    @ElementCollection
    private List<String> alimentosFavoritos;
}

```
