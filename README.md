# 🛒 EcoMerca2 - Usuarios Service API

Documentación de los endpoints del microservicio de usuarios desplegado en **Railway**.

## 🚀 Información del Servidor
- **URL Base:** `https://usuarios-bd-production.up.railway.app/api/v1/usuarios`
- **Autenticación:** Bearer Token (JWT)

---

## 👥 Gestión de Usuarios

### 1. Registrar Nuevo Usuario
Crea una cuenta nueva. Por defecto se asigna el rol `ROLE_USER`.
```bash
curl -X POST https://usuarios-bd-production.up.railway.app/api/v1/usuarios/ \
-H "Content-Type: application/json" \
-d '{
  "nombre": "Jose Alejandro",
  "email": "jose@ecomerca.com",
  "password": "password123",
  "fechaNacimiento": "2000-10-25"
}'
```

### 2. Inicio de Sesión (Login)
Obtiene el **ID** del usuario y el **Token JWT** para peticiones protegidas.
```bash
curl -X POST https://usuarios-bd-production.up.railway.app/api/v1/usuarios/login \
-H "Content-Type: application/json" \
-d '{
  "email": "jose@ecomerca.com",
  "password": "password123"
}'
```

### 3. Obtener Perfil de Usuario
Requiere token. Solo accesible por el dueño de la cuenta o un administrador.
```bash
curl -X GET https://usuarios-bd-production.up.railway.app/api/v1/usuarios/6 \
-H "Authorization: Bearer <TU_TOKEN>"
```

### 4. Actualizar Perfil
Actualiza los datos básicos. El campo `role` y `alimentosFavoritos` se omiten por seguridad en este endpoint.
```bash
curl -X PUT https://usuarios-bd-production.up.railway.app/api/v1/usuarios/6 \
-H "Authorization: Bearer <TU_TOKEN>" \
-H "Content-Type: application/json" \
-d '{
  "nombre": "Jose Alejandro Actualizado",
  "email": "jose.actualizado@ecomerca.com",
  "password": "nuevapassword123",
  "fechaNacimiento": "2000-10-25"
}'
```

### 5. Eliminar Cuenta
Borra al usuario y sus registros asociados en la tabla de favoritos (borrado en cascada).
```bash
curl -X DELETE https://usuarios-bd-production.up.railway.app/api/v1/usuarios/6 \
-H "Authorization: Bearer <TU_TOKEN>"
```

---

## 🍎 Gestión de Favoritos

### 6. Sincronizar Alimentos Favoritos
Endpoint tipo `PATCH` para actualizar la lista de productos preferidos del usuario.
```bash
curl -X PATCH https://usuarios-bd-production.up.railway.app/api/v1/usuarios/6/favoritos \
-H "Authorization: Bearer <TU_TOKEN>" \
-H "Content-Type: application/json" \
-d '["Aguacate", "Cafe del Valle", "Arepas"]'
```

---

## Comandos de Admin

Listar todos los Usuarios:
```bash
curl -X GET https://usuarios-bd-production.up.railway.app/api/v1/usuarios/ \
-H "Authorization: Bearer <TOKEN_DE_ADMIN>"
```

Listar Usuarios por páginas:
```bash
curl -X GET https://usuarios-bd-production.up.railway.app/api/v1/usuarios/page/0 \
-H "Authorization: Bearer <TOKEN_DE_ADMIN>"
```

---

## 🛡️ Códigos de Respuesta Comunes

| Código | Significado | Motivo |
| :--- | :--- | :--- |
| **200 OK** | Éxito | La operación se realizó correctamente. |
| **201 Created** | Creado | El usuario fue registrado con éxito. |
| **400 Bad Request** | Error de Validación | Faltan campos obligatorios o el formato es incorrecto. |
| **401 Unauthorized** | No autorizado | El token ha expirado o no se envió el header Authorization. |
| **403 Forbidden** | Acceso Denegado | Intentaste modificar o eliminar un usuario que no es el tuyo sin ser ADMIN. |
| **404 Not Found** | No encontrado | El ID del usuario no existe en la base de datos. |
| **500 Internal Error** | Error de Servidor | Error de credenciales inválidas o falla en la DB. |

---

### 💡 Notas de Implementación
- Las contraseñas se almacenan encriptadas con **BCrypt**.
- Se utiliza **Habeas Data** para asegurar que los correos electrónicos sean únicos en el sistema.
- La arquitectura sigue el patrón **DTO** para evitar la exposición de campos sensibles como el rol durante las actualizaciones.

---
