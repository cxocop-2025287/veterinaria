# 🐾 API REST - Sistema de Clínica Veterinaria

Backend RESTful profesional y escalable para la gestión integral de una clínica veterinaria, desarrollado con **Java 17**, **Spring Boot 3**, **Spring Security**, **JWT (jjwt)**, **Spring Data JPA** y **MySQL**.

---

## 🚀 1. Tecnologías Utilizadas

* **Java**: 17
* **Framework**: Spring Boot 3.3.5
* **Gestor de dependencias**: Apache Maven
* **Seguridad**: Spring Security 6 + BCrypt + JWT (`io.jsonwebtoken:jjwt:0.12.6`)
* **Persistencia**: Spring Data JPA / Hibernate (con soporte `jakarta.persistence`)
* **Base de Datos**: MySQL 8+ (con fallback en memoria H2 para suite de tests automáticos)
* **Validación**: Jakarta Validation (`@Valid`, `@NotBlank`, `@NotNull`, `@Email`, `@Positive`, etc.)
* **Utilidades**: Lombok
* **Testing**: JUnit 5, MockMvc, Spring Boot Test

---

## 📋 2. Requisitos Previos

* **JDK 17** o superior instalado y configurado en el `PATH` (`JAVA_HOME`).
* **MySQL 8.0+** instalado y en ejecución en el puerto `3306`.
* **IntelliJ IDEA** (o cualquier IDE compatible con proyectos Maven) o terminal de comandos.

---

## ⚙️ 3. Configuración de Base de Datos MySQL

### Crear la base de datos
Ejecuta la siguiente sentencia en tu cliente MySQL (Workbench, phpMyAdmin o consola MySQL):

```sql
CREATE DATABASE IF NOT EXISTS veterinaria CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Configuración de Credenciales
El archivo [`src/main/resources/application.properties`](file:///C:/2025287/veterinaria/src/main/resources/application.properties) contiene la configuración conectada a MySQL:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/veterinaria?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:TU_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
```

> [!TIP]
> Puedes cambiar `TU_PASSWORD` directamente en `application.properties` o mediante las variables de entorno `DB_PASSWORD`, `DB_USERNAME` y `DB_URL`.

---

## 🏃 4. Cómo Ejecutar el Proyecto

### Opción 1: Con Maven Wrapper (Recomendado)
Desde la raíz del proyecto (`C:\2025287\veterinaria`):

```bash
# Compilar y ejecutar tests:
.\mvnw clean install

# Iniciar la aplicación:
.\mvnw spring-boot:run
```

### Opción 2: Con Maven Global
```bash
mvn clean install
mvn spring-boot:run
```

### Opción 3: En IntelliJ IDEA
1. Abre IntelliJ IDEA y selecciona **Open**.
2. Selecciona la carpeta `veterinaria` (donde se encuentra `pom.xml`).
3. Deja que IntelliJ descargue las dependencias y sincronice el proyecto Maven.
4. Ejecuta la clase principal [`VeterinariaApplication.java`](file:///C:/2025287/veterinaria/src/main/java/org/carlosxocop/veterinaria/VeterinariaApplication.java).

---

## 👥 5. Usuarios Iniciales y Roles

El archivo [`data.sql`](file:///C:/2025287/veterinaria/src/main/resources/data.sql) inicializa automáticamente usuarios y mascotas para pruebas:

| Rol | Nombre | Email | Contraseña |
| :--- | :--- | :--- | :--- |
| `ADMIN` | Administrador del Sistema | `admin@veterinaria.com` | `admin123` |
| `VET` | Dr. Roberto Martínez | `vet@veterinaria.com` | `vet123` |
| `CLIENTE` | Carlos Cliente | `cliente@veterinaria.com` | `cliente123` |

> [!NOTE]
> Todas las contraseñas se almacenan cifradas con **BCrypt**.

---

## 🔑 6. Autenticación y Uso del JWT

1. **Obtener el token**: Realizar una petición `POST` a `/api/v1/auth/login` con las credenciales del usuario.
2. **Utilizar el token**: En todas las peticiones a endpoints protegidos, incluye el encabezado HTTP:
   ```http
   Authorization: Bearer <TU_TOKEN_JWT>
   ```

---

## 📡 7. Catálogo de Endpoints

### 🔐 Autenticación (`/api/v1/auth`)

#### 1. Registro de Cliente
* **Método**: `POST`
* **URL**: `/api/v1/auth/register`
* **Acceso**: Público (asigna automáticamente el rol `CLIENTE`)
* **Body Request**:
  ```json
  {
    "nombre": "Ana Pérez",
    "telefono": "555-1234",
    "email": "ana@gmail.com",
    "password": "password123"
  }
  ```
* **Response (201 Created)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tipo": "Bearer",
    "rol": "CLIENTE",
    "email": "ana@gmail.com",
    "nombre": "Ana Pérez"
  }
  ```

#### 2. Inicio de Sesión (Login)
* **Método**: `POST`
* **URL**: `/api/v1/auth/login`
* **Acceso**: Público
* **Body Request**:
  ```json
  {
    "email": "cliente@veterinaria.com",
    "password": "cliente123"
  }
  ```
* **Response (200 OK)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tipo": "Bearer",
    "rol": "CLIENTE",
    "email": "cliente@veterinaria.com",
    "nombre": "Carlos Cliente"
  }
  ```

---

### 🐶 Mascotas (`/api/v1/mascotas`)

#### 1. Consultar Mis Mascotas
* **Método**: `GET`
* **URL**: `/api/v1/mascotas/mis-mascotas`
* **Permisos**: `CLIENTE` (obtiene el usuario autenticado desde el SecurityContext)
* **Response (200 OK)**:
  ```json
  [
    {
      "id": 1,
      "nombre": "Firulais",
      "especie": "PERRO",
      "raza": "Golden Retriever",
      "edad": 3,
      "clienteId": 3,
      "clienteNombre": "Carlos Cliente",
      "clienteEmail": "cliente@veterinaria.com"
    }
  ]
  ```

#### 2. Registrar Mascota
* **Método**: `POST`
* **URL**: `/api/v1/mascotas`
* **Permisos**: `CLIENTE`, `ADMIN`
* **Body Request**:
  ```json
  {
    "nombre": "Boby",
    "especie": "PERRO",
    "raza": "Beagle",
    "edad": 2
  }
  ```
* **Response (201 Created)**:
  ```json
  {
    "id": 3,
    "nombre": "Boby",
    "especie": "PERRO",
    "raza": "Beagle",
    "edad": 2,
    "clienteId": 3,
    "clienteNombre": "Carlos Cliente",
    "clienteEmail": "cliente@veterinaria.com"
  }
  ```

#### 3. Consultar Mascota por ID
* **Método**: `GET`
* **URL**: `/api/v1/mascotas/{id}`
* **Permisos**: `VET`, `ADMIN` (y `CLIENTE` solo para su propia mascota)

---

### 📅 Citas Médicas (`/api/v1/citas`)

#### 1. Agendar Cita
* **Método**: `POST`
* **URL**: `/api/v1/citas`
* **Permisos**: `CLIENTE`, `ADMIN`
* **Reglas validadas**:
  * Duración de cada cita: 30 minutos.
  * Disponibilidad del veterinario: No permite citas que se crucen o solapen dentro de los 30 minutos.
  * Límite de cliente: Máximo 2 citas `PENDIENTE` en el mismo día.
  * Un cliente solo puede agendar citas para sus propias mascotas.
* **Body Request**:
  ```json
  {
    "mascotaId": 1,
    "veterinarioId": 2,
    "fechaHora": "2026-10-15T10:00:00",
    "motivo": "Revisión general y vacuna"
  }
  ```
* **Response (201 Created)**:
  ```json
  {
    "id": 1,
    "mascotaId": 1,
    "mascotaNombre": "Firulais",
    "clienteId": 3,
    "clienteNombre": "Carlos Cliente",
    "veterinarioId": 2,
    "veterinarioNombre": "Dr. Roberto Martínez (VET)",
    "fechaHora": "2026-10-15T10:00:00",
    "fechaHoraFin": "2026-10-15T10:30:00",
    "motivo": "Revisión general y vacuna",
    "estado": "PENDIENTE"
  }
  ```

#### 2. Consultar Agenda de Citas
* **Método**: `GET`
* **URL**: `/api/v1/citas/agenda?fecha=2026-10-15&veterinarioId=2`
* **Permisos**: `VET`, `ADMIN`
* **Query Params opcionales**: `fecha` (YYYY-MM-DD), `veterinarioId` (Long)

#### 3. Cancelar Cita
* **Método**: `PATCH`
* **URL**: `/api/v1/citas/{id}/cancelar`
* **Permisos**: `CLIENTE`, `ADMIN`
* **Reglas**:
  * Solo puede cancelarse si faltan **más de 2 horas** para la cita programada.
  * No se pueden cancelar citas en estado `COMPLETADA` o `CANCELADA`.
  * `CLIENTE` solo puede cancelar sus propias citas.

---

### 🩺 Expediente e Historial Clínico (`/api/v1/expedientes`)

#### 1. Registrar Expediente Clínico
* **Método**: `POST`
* **URL**: `/api/v1/expedientes`
* **Permisos**: `VET`, `ADMIN`
* **Reglas**:
  * La cita pasa automáticamente a estado `COMPLETADA`.
  * No se permite registrar expedientes para citas `CANCELADA`.
  * Relación 1 a 1: no se permiten dos expedientes para la misma cita.
* **Body Request**:
  ```json
  {
    "citaId": 1,
    "diagnostico": "Infección leve en oído izquierdo",
    "tratamiento": "Gotas óticas cada 12 horas por 7 días",
    "pesoKg": 12.5
  }
  ```
* **Response (201 Created)**:
  ```json
  {
    "id": 1,
    "citaId": 1,
    "mascotaId": 1,
    "mascotaNombre": "Firulais",
    "veterinarioId": 2,
    "veterinarioNombre": "Dr. Roberto Martínez (VET)",
    "diagnostico": "Infección leve en oído izquierdo",
    "tratamiento": "Gotas óticas cada 12 horas por 7 días",
    "pesoKg": 12.5,
    "fechaRegistro": "2026-10-15T10:25:00"
  }
  ```

#### 2. Consultar Historial Clínico de Mascota
* **Método**: `GET`
* **URL**: `/api/v1/expedientes/mascota/{mascotaId}`
* **Permisos**: `VET`, `CLIENTE`, `ADMIN` (CLIENTE solo puede consultar sus mascotas)

---

## 🏗️ 8. Estructura del Proyecto

```text
veterinaria/
├── .mvn/
│   ├── jvm.config
│   └── wrapper/
│       └── maven-wrapper.properties
├── pom.xml
├── README.md
├── mvnw
├── mvnw.cmd
└── src/
    ├── main/
    │   ├── java/
    │   │   └── org/carlosxocop/veterinaria/
    │   │       ├── config/
    │   │       ├── controller/
    │   │       │   ├── AuthController.java
    │   │       │   ├── CitaController.java
    │   │       │   ├── ExpedienteController.java
    │   │       │   └── MascotaController.java
    │   │       ├── dto/
    │   │       │   ├── auth/ (RegisterRequest, LoginRequest, AuthResponse)
    │   │       │   ├── cita/ (CitaRequest, CitaResponse)
    │   │       │   ├── error/ (ErrorResponse)
    │   │       │   ├── expediente/ (ExpedienteRequest, ExpedienteResponse)
    │   │       │   └── mascota/ (MascotaRequest, MascotaResponse)
    │   │       ├── entity/
    │   │       │   ├── CitaMedica.java
    │   │       │   ├── ExpedienteClinico.java
    │   │       │   ├── Mascota.java
    │   │       │   └── Usuario.java
    │   │       ├── enums/
    │   │       │   ├── Especie.java
    │   │       │   ├── EstadoCita.java
    │   │       │   └── Rol.java
    │   │       ├── exception/
    │   │       │   ├── BusinessException.java
    │   │       │   ├── ConflictException.java
    │   │       │   ├── GlobalExceptionHandler.java
    │   │       │   ├── ResourceNotFoundException.java
    │   │       │   └── UnauthorizedException.java
    │   │       ├── repository/
    │   │       │   ├── CitaMedicaRepository.java
    │   │       │   ├── ExpedienteClinicoRepository.java
    │   │       │   ├── MascotaRepository.java
    │   │       │   └── UsuarioRepository.java
    │   │       ├── security/
    │   │       │   ├── CustomUserDetailsService.java
    │   │       │   ├── JwtAuthenticationFilter.java
    │   │       │   ├── JwtService.java
    │   │       │   └── SecurityConfig.java
    │   │       ├── service/
    │   │       │   ├── AuthService.java & AuthServiceImpl.java
    │   │       │   ├── CitaService.java & CitaServiceImpl.java
    │   │       │   ├── ExpedienteService.java & ExpedienteServiceImpl.java
    │   │       │   └── MascotaService.java & MascotaServiceImpl.java
    │   │       └── VeterinariaApplication.java
    │   │
    │   └── resources/
    │       ├── application.properties
    │       └── data.sql
    │
    └── test/
        ├── java/
        │   └── org/carlosxocop/veterinaria/
        │       └── VeterinariaApplicationTests.java
        └── resources/
            └── application-test.properties
```

---

## 🧪 9. Casos de Prueba Implementados

La clase [`VeterinariaApplicationTests.java`](file:///C:/2025287/veterinaria/src/test/java/org/carlosxocop/veterinaria/VeterinariaApplicationTests.java) cubre los 15 escenarios requeridos:

1. `test1_RegistroCliente`: Registro de cliente público y asignación de rol `CLIENTE`.
2. `test2_Login`: Autenticación y generación de JWT Bearer token.
3. `test3_AccesoProtegidoSinJwt`: Rechazo de peticiones sin token (403/401).
4. `test4_AccesoConJwt`: Acceso exitoso con token Bearer válido (200).
5. `test5_RestriccionRoles`: Restricción de acceso a endpoints de rol superior (403).
6. `test6_RegistroMascota`: Registro exitoso de mascota asociándola al cliente autenticado.
7. `test7_ConsultaMascotasPropias`: Consulta exclusiva de mascotas del cliente autenticado.
8. `test8_CreacionCita`: Creación de cita médica con estado inicial `PENDIENTE`.
9. `test9_ConflictoHorarioVeterinario`: Validación de horario de 30 min y rechazo por solapamiento (409 Conflict).
10. `test10_LimiteDosCitasPendientesCliente`: Validación de límite de 2 citas pendientes por día (400 Bad Request).
11. `test11_CancelacionMasDeDosHoras`: Cancelación exitosa con más de 2 horas de anticipación.
12. `test12_RechazoCancelacionMenosDeDosHoras`: Rechazo de cancelación con menos de 2 horas (400 Bad Request).
13. `test13_CreacionExpedienteClinico`: Registro de expediente clínico por veterinario.
14. `test14_CambioEstadoCitaACompletada`: Actualización automática de la cita a `COMPLETADA` tras crear expediente.
15. `test15_ConsultaHistorialClinico`: Consulta del historial clínico de una mascota por su dueño.