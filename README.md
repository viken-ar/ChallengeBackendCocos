# Challenge Backend Cocos

API REST para gestión de órdenes bursátiles, con soporte para órdenes de mercado y limit, idempotencia, consulta de portfolio y búsqueda de instrumentos.

## 🛠️ Stack Tecnológico

| Componente | Versión |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.2 |
| PostgreSQL | 18.3 |
| Flyway | (incluido en Spring Boot) |
| MapStruct | 1.6.3 |
| SpringDoc OpenAPI | 2.8.5 |

## 📐 Arquitectura

El proyecto sigue la **Arquitectura Hexagonal (Ports & Adapters)**:

```
├── domain/          # Modelos, excepciones, eventos y lógica de negocio
├── application/     # Casos de uso (inbound), puertos (outbound), comandos y estrategias
├── infrastructure/  # Adaptadores de salida (repositorios JPA, publishers)
└── interfaces/      # Controladores REST, mappers, resources y listeners
```

## 🚀 Requisitos

- **JDK 25** (o superior)
- **Maven 3.9+** (incluido el wrapper `./mvnw`)
- **PostgreSQL 18+** (local o remoto)
- **Docker & Docker Compose** (opcional, para ejecutar con contenedores)

---

## 📋 Opciones de ejecucion

El proyecto soporta **4 formas** de ejecutarse. Elegí la que mejor se adapte a tu flujo de trabajo.

### Opción 1 — Docker Compose (Local)

La forma más rápida y aislada. Levanta la base de datos PostgreSQL y la aplicación en contenedores.

```bash
# 1. Asegurate de tener un archivo .env (ver sección "Configuración con .env")
# Para DB local, usá las variables de .env.docker-compose

# 2. Levantar todo
docker compose up --build

# 3. La app estará disponible en http://localhost:8080
```

**Servicios:**

| Servicio | Imagen | Puerto | Descripción |
|---|---|---|---|
| `db` | `postgres:18.3` | `5432` | Base de datos PostgreSQL |
| `app` | Build desde Dockerfile | `8080` | Aplicación Spring Boot |

La aplicación espera a que la base de datos esté healthy antes de arrancar (`depends_on: condition: service_healthy`).

### Opción 2 — IDE con perfil `local`

Ideal para desarrollo y debugging.

1. **Levantá PostgreSQL local** (Opción 1 solo la DB, o instalalo manualmente):

```bash
# Solo la base de datos con Docker
docker compose up -d db
```

2. **Configurá las variables de entorno** en tu IDE:

| Variable | Valor |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/neondb` |
| `DB_USER` | `postgres` |
| `DB_PASSWORD` | `postgres` |
| `SPRING_PROFILES_ACTIVE` | `local` |

3. **Ejecutá** `ChallengeBackendCocosApplication.java` desde el IDE.

4. La app estará disponible en `http://localhost:8080`.

> **IntelliJ IDEA:** Edit Configuration → Environment variables: `DB_URL=jdbc:postgresql://localhost:5432/neondb DB_USER=postgres DB_PASSWORD=postgres SPRING_PROFILES_ACTIVE=local`
>
> **VS Code:** Agregá en `launch.json` las variables en `env`.

### Opción 3 — Maven con variables de entorno

Ejecutá la aplicación directamente con Maven, pasando las variables de conexión.

```bash
# Con base de datos local
export APPLICATION_DATASOURCE_URL=jdbc:postgresql://localhost:5432/neondb
export APPLICATION_DATASOURCE_USERNAME=postgres
export APPLICATION_DATASOURCE_PASSWORD=postgres

# Ejecutar
./mvnw spring-boot:run
```

La app estará disponible en `http://localhost:8080`.

### Opción 4 — IDE con `.env` (Base de datos remota)

Si querés conectar a una base de datos remota (ej: Neon.tech), usá el archivo `.env`.

1. **Copiá y editá** el archivo `.env`:

```bash
cp .env.example .env
```

2. **Completá** las variables con tus credenciales:

```properties
APPLICATION_DATASOURCE_URL=jdbc:postgresql://tu-host:5432/tu-db
APPLICATION_DATASOURCE_USERNAME=tu-usuario
APPLICATION_DATASOURCE_PASSWORD=tu-password
SPRING_PROFILES_ACTIVE=local
```

3. **Cargá las variables** en tu IDE o ejecutá:

```bash
# Cargar variables del .env y ejecutar
set -a && source .env && set +a && ./mvnw spring-boot:run
```

---

## ⚙️ Configuración

### Variables de Entorno

| Variable | Perfil | Descripción | Default |
|---|---|---|---|
| `APPLICATION_DATASOURCE_URL` | default | URL JDBC de la base de datos | `jdbc:postgresql://localhost:5432/cocos_db` |
| `APPLICATION_DATASOURCE_USERNAME` | default | Usuario de la base de datos | `postgres` |
| `APPLICATION_DATASOURCE_PASSWORD` | default | Contraseña de la base de datos | `postgres` |
| `DB_URL` | `local` | URL JDBC (override del default) | `jdbc:postgresql://localhost:5432/neondb` |
| `DB_USER` | `local` | Usuario (override del default) | `postgres` |
| `DB_PASSWORD` | `local` | Contraseña (override del default) | `postgres` |
| `SPRING_PROFILES_ACTIVE` | — | Perfil activo de Spring | `default` |

### Perfiles Spring

| Perfil | Archivo | Uso |
|---|---|---|
| `default` | `application.yaml` | Configuración base con variables `APPLICATION_DATASOURCE_*` |
| `local` | `application-local.yaml` | Desarrollo local con variables `DB_*` y Flyway sin baseline |

### Migraciones de Base de Datos

Las migraciones se ejecutan automáticamente con **Flyway** al iniciar la aplicación:

```
src/main/resources/db/migration/
├── V1__initial_schema_and_seed.sql   # Esquema inicial + datos seed
├── V2__idempotency_keys.sql          # Tabla de idempotencia
└── V3__ADD_COLUMN_REJECT_REASON.sql  # Columna reject_reason en orders
```

Las tablas creadas son: `users`, `instruments`, `orders`, `marketdata`, `idempotency_keys`.

---

## 📡 API Endpoints

Una vez levantada la aplicación, accedé a:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec:** `http://localhost:8080/v3/api-docs`

### Endpoints principales

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/v1/orders` | Crear una orden (idempotente, header `Idempotency-Key`) |
| `DELETE` | `/v1/orders/{orderId}` | Cancelar una orden |
| `GET` | `/v1/users/{userId}/orders` | Listar órdenes de un usuario (paginado) |
| `GET` | `/v1/users/{userId}/orders/{orderId}` | Obtener una orden específica |
| `GET` | `/v1/users/{userId}/portfolio` | Obtener el portfolio valorizado |
| `GET` | `/v1/instruments` | Buscar instrumentos por ticker/nombre |

---

## 🧪 Tests

```bash
# Ejecutar todos los tests
./mvnw test

# Ejecutar con perfil específico
./mvnw test -Dspring.profiles.active=local
```

Los tests usan **H2** como base de datos en memoria.

---

## 🐳 Docker — Notas

- El `Dockerfile` usa un **multi-stage build**: compila con Maven/Amazon Corretto 23 y ejecuta con Eclipse Temurin 21.
- El volumen `postgresdata` persiste los datos de PostgreSQL entre reinicios de contenedores.
- Para limpiar datos: `docker compose down -v`.
