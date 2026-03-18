# 🛒 EcoMerca2 - Usuarios Service

Este microservicio gestiona la autenticación, perfiles y preferencias de los usuarios de la plataforma **EcoMerca2**. Implementa seguridad basada en **JWT (JSON Web Tokens)** y control de acceso por roles (**RBAC**).

## 🚀 Requisitos Previos
* **Docker & Docker Compose** instalados.
* Base de datos PostgreSQL activa (contenedor `intellij-postgres-1`).
* Servidor corriendo en `http://localhost:8080`.

---

## 🔐 Guía de Endpoints y Pruebas

### 1. Registro de Usuario (Público)
Crea una nueva cuenta. Por defecto, el sistema asigna el rol `ROLE_USER`.
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

### 2. Inicio de Sesión (Login)
Obtén tu token de acceso. **Nota:** Debes copiar el `token` devuelto para los siguientes pasos.
```bash
curl -X POST http://localhost:8080/api/v1/usuarios/login \
-H "Content-Type: application/json" \
-d '{
  "email": "jose@ecomerca.com",
  "password": "password123"
}'
```

### 3. Listar Usuarios (Solo ADMIN)
Este endpoint requiere un token con `ROLE_ADMIN`. Si usas un token de usuario normal, recibirás un `403 Forbidden`.
```bash
curl -X GET http://localhost:8080/api/v1/usuarios/ \
-H "Authorization: Bearer <TU_TOKEN_AQUI>"
```

### 4. Sincronizar Favoritos (Propietario/Admin)
Permite guardar una lista de strings con los productos preferidos.
```bash
curl -X PATCH http://localhost:8080/api/v1/usuarios/1/favoritos \
-H "Authorization: Bearer <TU_TOKEN_AQUI>" \
-H "Content-Type: application/json" \
-d '["Aguacate", "Cafe del Valle", "Arepas"]'
```

### 5. Actualizar Perfil (Propietario/Admin)
Actualiza datos básicos. El campo `role` es de **solo lectura** y no se puede modificar por este medio.
```bash
curl -X PUT http://localhost:8080/api/v1/usuarios/ \
-H "Authorization: Bearer <TU_TOKEN_AQUI>" \
-H "Content-Type: application/json" \
-d '{
  "id": 1,
  "nombre": "Jose Alejandro Actualizado",
  "fechaNacimiento": "2000-10-25",
  "password": "nuevapassword123"
}'
```

### 6. Eliminar Cuenta (Propietario/Admin)
Elimina el registro de la base de datos. Si un usuario intenta borrar a otro sin ser Admin, el sistema lanzará una `AccesoDenegadoException`.
```bash
curl -X DELETE http://localhost:8080/api/v1/usuarios/1 \
-H "Authorization: Bearer <TU_TOKEN_AQUI>"
```

---

## 🛠️ Estructura de Seguridad

| Rol | Permisos |
| :--- | :--- |
| **Anónimo** | Registro y Login. |
| **ROLE_USER** | Ver/Editar/Borrar su **propio** perfil y favoritos. |
| **ROLE_ADMIN** | Todo lo anterior + Listar todos los usuarios y gestionar perfiles ajenos. |

