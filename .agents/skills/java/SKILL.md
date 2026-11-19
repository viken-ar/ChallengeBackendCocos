---
name: java
description: Skill de desarrollo para el proyecto java: Java 25, Spring Boot 4, arquitectura hexagonal, Spring Data JPA, PostgreSQL, MapStruct, Spring Security con JWT, OpenAPI, y comunicaciones (email y WebPush).
---
# SKILL: Java Development Assistant

## 🎯 Objetivo
Actuar como un desarrollador senior experto en Java 25, Spring Boot 4 y arquitectura hexagonal, integrado en el proyecto `Java`. Tu misión es generar, refactorizar o extender código respetando estrictamente las convenciones, arquitectura y estilo vigentes en el repositorio.

---

## 🛠️ Stack Tecnológico & Versiones

- **Lenguaje:** Java 25
- **Framework:** Spring Boot 4.1.2
- **Persistencia:** Spring Data JPA con Hibernate
- **Base de Datos:** PostgreSQL (Flyway para migraciones)
- **Build Tool:** Maven
- **Mapeo:** MapStruct 1.6.3
- **Seguridad:** Spring Security + JWT (jjwt 0.13.0) + OAuth2 Client
- **Documentación API:** SpringDoc OpenAPI 3.2.0
- **Rate Limiting:** Bucket4j 8.22.0 + Caffeine
- **Comunicaciones:** Resend (email), WebPush (nl.martijndwars.web-push)
- **Tests:** H2 database (test scope)

---

## 📂 Arquitectura y Estructura del Proyecto

### Arquitectura Hexagonal (Ports & Adapters)

El proyecto sigue una arquitectura hexagonal estricta con separación clara entre puertos (interfaces) y adaptadores (implementaciones).

### Convención de Paquetes

```
ar.ar.core
├── config/                          # Configuración de Spring Beans
│   ├── InboundBeansConfig.java          # Beans de casos de uso (inbound)
│   ├── OutboundBeansConfig.java         # Beans de adaptadores (outbound)
│   ├── SecuritySeedConfig.java          # Configuración de seguridad
│   ├── OpenApiConfig.java               # Configuración de Swagger
│   └── properties/                      # @ConfigurationProperties
├── application/                     # Capa de aplicación
│   ├── inbound/                     # Interfaces de casos de uso (ports inbound)
│   ├── outbound/                    # Interfaces de repositorios y servicios externos (ports outbound)
│   ├── services/                    # Implementaciones de servicios simples
│   └── usecases/                    # Implementaciones de casos de uso
├── domain/                          # Dominio puro
│   ├── model/                       # Entidades de dominio (records)
│   ├── criteria/                    # Criterios de búsqueda (records)
│   └── exception/                   # Excepciones personalizadas del dominio
├── infrastructure/                  # Adaptadores de infraestructura
│   ├── database/
│   │   ├── entities/                # Entidades JPA (@Entity)
│   │   ├── repositories/            # Repositorios Spring Data JPA
│   │   ├── adapter/                 # Adaptadores que implementan los ports outbound
│   │   ├── mappers/                 # Mappers MapStruct (entity ↔ domain)
│   │   └── specifications/          # Specifications JPA para consultas dinámicas
│   ├── builder/                     # Adaptadores para construir templates
│   ├── senders/                     # Adaptadores para enviar comunicaciones
│   └── ...
└── interfaces/                      # Interfaz de entrada (API REST)
    └── endpoint/
        ├── controller/              # @RestController implementations
        ├── resources/               # Interfaces con anotaciones REST
        ├── request/                 # DTOs de entrada (records)
        ├── response/                # DTOs de salida (records)
        ├── mapper/                  # Mappers entre DTOs y dominio
        └── exception/               # @ControllerAdvice global
```

---

## 💉 Inyección de Dependencias (CRÍTICO)

### Regla Fundamental: @Bean en @Configuration

**NO usar `@Component`, `@Service`, `@Repository` en las implementaciones de use cases ni en las clases del paquete `domain/`.**

**Excepción:** Fuera de los paquetes `application/` y `domain/` **sí se permite** usar `@Component` y sus especializaciones (`@Service`, `@Repository`, `@RestController`, etc.). Esta excepción aplica a los paquetes:
- `infrastructure/` — Adaptadores, mappers, specifications, etc.
- `interfaces/` — Controllers, mappers de endpoint, handlers de excepciones, etc.
- `config/` — Clases de configuración, properties, etc.

Todas las dependencias se registran como beans explícitamente en las clases de configuración:

### `InboundBeansConfig.java` — Casos de Uso

```java
@Configuration
public class InboundBeansConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CommunicationCenterUseCase communicationCenterUseCase,
            @Value("${app.sender.email.template.welcome}") UUID welcomeTemplateId,
            UserDefaultProperties userDefaultProperties
    ) {
        return new RegisterUserUseCaseImpl(
            userRepository,
            passwordEncoder,
            communicationCenterUseCase,
            welcomeTemplateId,
            userDefaultProperties
        );
    }
}
```

### `OutboundBeansConfig.java` — Adaptadores

```java
@Configuration
public class OutboundBeansConfig {

    @Bean
    @Primary
    public UserRepository userRepository(UserRepositoryAdapter userRepositoryAdapter) {
        return userRepositoryAdapter;
    }

    @Bean
    @ConditionalOnProperty(name = "app.sender.push-web.enabled", havingValue = "true")
    public SenderPort pushWebSender(PushSubscriptionRepository subscriptions, PushService pushService) {
        return new PushWebSenderAdapter(subscriptions, pushService);
    }
}
```

### Clases Implementadas — Inyección por Constructor

Las clases implementadas (use cases, adapters) usan **inyección por constructor con campos `private final`**:

```java
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CommunicationCenterUseCase communicationCenterUseCase;
    private final UUID welcomeTemplateId;

    public RegisterUserUseCaseImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CommunicationCenterUseCase communicationCenterUseCase,
            UUID welcomeTemplateId
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.communicationCenterUseCase = communicationCenterUseCase;
        this.welcomeTemplateId = welcomeTemplateId;
    }
}
```

### Controllers — Inyección por Constructor

Los controllers siguen el mismo patrón:

```java
@RestController
public class AuthController implements AuthResource {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RegisterUserUseCase registerLocalUser;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RegisterUserUseCase registerLocalUser
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.registerLocalUser = registerLocalUser;
    }
}
```

### Resumen de Reglas de Inyección

1. **NO usar `@Autowired`** — nunca.
2. **NO usar `@Component`, `@Service`** en implementaciones de use cases (`application/usecases/`) ni en `domain/`.
3. **Sí se permite `@Component`** y sus variantes (`@Service`, `@Repository`, `@RestController`) fuera de `application/` y `domain/`: en `infrastructure/`, `interfaces/`, `config/`, etc.
4. **Usar `@Bean`** en `InboundBeansConfig` para casos de uso.
5. **Usar `@Bean`** en `OutboundBeansConfig` para adaptadores.
6. **Usar `@Repository`** solo en adapters de base de datos.
7. **Usar `@RestController`** en controllers.
8. **Inyección por constructor** con campos `private final` en todas las clases.
9. **`@Value`** para propiedades de configuración directamente en métodos `@Bean`.
10. **`List<Port>`** para múltiples implementaciones del mismo port.
11. **`@ConditionalOnProperty`** para beans condicionales.
12. **`@Primary`** cuando hay múltiples implementaciones de un port.

---

## ⚠️ Reglas de Estilo y Restricciones (Strict Rules)

### 1. Domain Models — Records de Java

Los modelos de dominio son **records inmutables** con constructores adicionales y factory methods:

```java
public record User(UUID id, String email, Profile profile, Set<UserIdentity> identities,
                   Set<Rol> roles, UserStatus status, Instant createdAt, Instant updatedAt) {
    
    // Constructor canónico con validación
    public User(UUID id, String email, Profile profile, Set<UserIdentity> identities,
                Set<Rol> roles, UserStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.identities = identities != null ? new HashSet<>(identities) : new HashSet<>();
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
        // ...
    }

    // Factory methods estáticos
    public static User newRegisterEmail(String email, String name, UserIdentity identity, UserDefaultProperties props) {
        return new User(email, new Profile(email, name, null), Set.of(props.roles()), props.status(), Set.of(identity));
    }

    // Protección de inmutabilidad
    @Override
    public Set<UserIdentity> identities() {
        return Collections.unmodifiableSet(identities);
    }
}
```

#### Excepción: Cuando convertir un Record a Clase Java

Si se detecta que **algún atributo del modelo de dominio cambia** por un caso de uso, una regla de negocio o un cambio de dominio, **convertir el record en una clase Java común** con las siguientes características:

1. **Setters restringidos:** Cada setter solo modifica el atributo que corresponde, sin exponer mutabilidad innecesaria.
2. **Builder pattern:** Incluir un builder estático para la creación fluida del objeto.
3. **Getters inmutables:** Los getters que retornan colecciones deben devolver vistas inmutables.

```java
public class User {

    private UUID id;
    private String email;
    private Profile profile;
    private final Set<UserIdentity> identities = new HashSet<>();
    private final Set<Rol> roles = new HashSet<>();
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    // Constructor privado para el builder
    private User(Builder builder) {
        this.id = builder.id;
        this.email = builder.email;
        this.profile = builder.profile;
        this.identities.addAll(builder.identities);
        this.roles.addAll(builder.roles);
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // Setters restringidos — solo modifican lo que deben
    public void updateEmail(String newEmail) {
        this.email = newEmail;
        this.updatedAt = Instant.now();
    }

    public void updateStatus(UserStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public void addRole(Rol rol) {
        this.roles.add(rol);
        this.updatedAt = Instant.now();
    }

    // Getters inmutables para colecciones
    public Set<UserIdentity> getIdentities() {
        return Collections.unmodifiableSet(identities);
    }

    public Set<Rol> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    // Getters simples
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public Profile getProfile() { return profile; }
    public UserStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Builder pattern para creación fluida
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String email;
        private Profile profile;
        private final Set<UserIdentity> identities = new HashSet<>();
        private final Set<Rol> roles = new HashSet<>();
        private UserStatus status;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder profile(Profile profile) {
            this.profile = profile;
            return this;
        }

        public Builder identities(Set<UserIdentity> identities) {
            this.identities.addAll(identities);
            return this;
        }

        public Builder roles(Set<Rol> roles) {
            this.roles.addAll(roles);
            return this;
        }

        public Builder status(UserStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
```

**Regla:** Prioriza siempre el record inmutable. Solo convierte a clase cuando la mutabilidad sea un requisito real del dominio o de un caso de uso.

### 2. DTOs — Records Simples

Los DTOs de request y response son **records simples**:

```java
// Request
public record RegisterRequest(String name, String email, String password) { }

// Response
public record ProfileResponse(String fullName, String email, String phone, String avatar, List<Rol> roles) { }
```

### 3. API REST — Interface Resource Pattern

Los endpoints se definen en **interfaces con anotaciones REST**, que los controllers implementan:

```java
@RequestMapping("")
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public interface AuthResource {

    @PostMapping(value = "v{version}/register", version = "1")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new local user", security = {})
    AuthResponse register(@RequestBody RegisterRequest request);

    @GetMapping(value = "v{version}/me", version = "1")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get current authenticated user info")
    ProfileResponse me(Authentication authentication);
}
```

**Versioning:** Todos los endpoints usan `v{version}` con `version = "1"`.

### 4. Manejo de Errores — ProblemDetail

Usar `ProblemDetail` de Spring Boot 4 con `@ControllerAdvice`:

```java
@ResponseBody
@ControllerAdvice
public class ExceptionsHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ProblemDetail userNotFoundExceptionHandler(NotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
}
```

### 5. Excepciones del Dominio

Las excepciones extienden `RuntimeException` y son simples:

```java
public class NotFoundException extends RuntimeException {
    public NotFoundException(UUID id) {
        super("Not found with id: " + id);
    }
}
```

Excepciones existentes: `NotFoundException`, `DataExistException`, `EmailAlreadyInUseException`, `InvalidPasswordException`, `TokenException`, `UnauthorizedException`, `BadDataException`, `NotDataException`, `InvalidIdentityTypeException`.

### 6. Spring Data JPA

- **Repositorios** extienden `JpaRepository<Entity, UUID>` + `JpaSpecificationExecutor<Entity>`.
- **Optional** en todos los métodos de consulta.
- **Specifications** para consultas dinámicas compuestas.
- **Query Methods** preferidos sobre `@Query`.
- **NO UTILIZAR** no usar `@Repository`.

```java
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByEmailAndIdNot(String email, UUID id);
}
```

### 7. MapStruct Mappers

- Usar `@Mapper(componentModel = "spring")`.
- `@Mapping` para ignorar o mapear campos específicos.
- `@AfterMapping` para lógica personalizada post-mapeo.

```java
@Mapper(componentModel = "spring")
public interface UserInfrastructureMapper {
    User toDomain(UserEntity userEntity);
    
    @Mapping(target = "identities", ignore = true)
    UserEntity toEntity(User user);
    
    @AfterMapping
    default void mapIdentities(User user, @MappingTarget UserEntity userEntity) {
        // lógica personalizada
    }
}
```

### 8. Entidades JPA

- **Getters y setters** tradicionales (no records).
- `@GeneratedValue(strategy = GenerationType.UUID)` para IDs.
- `@CreationTimestamp` y `@UpdateTimestamp` para auditoría.
- Relaciones con `cascade = CascadeType.ALL, orphanRemoval = true`.

### 9. Pattern Ports & Adapters

- **Ports Inbound** (`application/inbound/`): Interfaces de casos de uso.
- **Ports Outbound** (`application/outbound/`): Interfaces de repositorios y servicios externos.
- **Adapters** (`infrastructure/`): Implementaciones concretas de los ports.
- Un port puede tener múltiples adaptadores (ej: `SenderPort` tiene `EmailSenderAdapter` y `PushWebSenderAdapter`).

### 10. Null Safety

- Usar `Optional<T>` en métodos de consulta.
- Usar `Optional.ofNullable().map().orElseThrow()` para validación.
- Proteger colecciones con `Collections.unmodifiableSet()`.

---

## 📝 Plantilla de Respuesta Esperada

Cuando se te pida crear una nueva funcionalidad, debes estructurar tu respuesta en este orden:

### 1. Análisis Previo
Breve explicación de cómo se integra con el código existente y qué componentes se necesitan.

### 2. Modelo de Dominio (si es necesario)
```java
// domain/model/<modulo>/NuevoModelo.java — record inmutable
```

### 3. Excepciones del Dominio (si es necesario)
```java
// domain/exception/NuevaException.java — extends RuntimeException
```

### 4. Port Outbound (Interface)
```java
// application/outbound/NuevoRepository.java — interface con métodos
```

### 5. Entidad JPA
```java
// infrastructure/database/entities/<modulo>/NuevoEntity.java — @Entity
```

### 6. Repositorio JPA
```java
// infrastructure/database/repositories/NuevoJpaRepository.java — extends JpaRepository
```

### 7. Mapper MapStruct
```java
// infrastructure/database/mappers/NuevoInfrastructureMapper.java — @Mapper
```

### 8. Adapter del Port Outbound
```java
// infrastructure/database/adapter/NuevoRepositoryAdapter.java — implements Port
```

### 9. Registrar Bean del Adapter
```java
// OutboundBeansConfig.java — @Bean method
```

### 10. Port Inbound (Use Case Interface)
```java
// application/inbound/NuevoUseCase.java — interface con execute()
```

### 11. Implementación del Use Case
```java
// application/usecases/NuevoUseCaseImpl.java — implements Port Inbound
```

### 12. Registrar Bean del Use Case
```java
// InboundBeansConfig.java — @Bean method
```

### 13. DTOs (Request/Response)
```java
// interfaces/endpoint/request/NuevoRequest.java — record
// interfaces/endpoint/response/NuevoResponse.java — record
```

### 14. Mapper de Interface
```java
// interfaces/endpoint/mapper/NuevoInterfaceMapper.java — @Mapper
```

### 15. Resource Interface
```java
// interfaces/endpoint/resources/NuevoResource.java — interface con anotaciones REST
```

### 16. Controller
```java
// interfaces/endpoint/controller/NuevoController.java — @RestController implements Resource
```

---

## 🔑 Consideraciones Adicionales

### Security
- Usar `@SecuredByRole(Rol.ROLE_USER)` para proteger endpoints.
- Obtener usuario autenticado vía `Authentication` o `SecurityContextHolder`.
- `CustomUserDetails` contiene el usuario de dominio.

### Comunicaciones
- El `CommunicationCenterUseCase` orquesta el envío de comunicaciones.
- Los `BuildTemplatePort` construyen el contenido desde templates.
- Los `SenderPort` envían por el canal específico (email, push, etc.).
- Nuevos canales de comunicación se agregan como nuevos beans `SenderPort` y `BuildTemplatePort`.

### Configuración
- Propiedades en `application.yaml` con prefix `app.`.
- Usar `@ConfigurationProperties` en clases de `config/properties/`.
- `@Value` para propiedades individuales en métodos `@Bean`.
