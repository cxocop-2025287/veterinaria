package org.carlosxocop.veterinaria;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.carlosxocop.veterinaria.dto.auth.LoginRequest;
import org.carlosxocop.veterinaria.dto.auth.RegisterRequest;
import org.carlosxocop.veterinaria.dto.cita.CitaRequest;
import org.carlosxocop.veterinaria.dto.expediente.ExpedienteRequest;
import org.carlosxocop.veterinaria.dto.mascota.MascotaRequest;
import org.carlosxocop.veterinaria.dto.usuario.UsuarioRequest;
import org.carlosxocop.veterinaria.entity.CitaMedica;
import org.carlosxocop.veterinaria.entity.Mascota;
import org.carlosxocop.veterinaria.entity.Usuario;
import org.carlosxocop.veterinaria.enums.Especie;
import org.carlosxocop.veterinaria.enums.EstadoCita;
import org.carlosxocop.veterinaria.enums.Rol;
import org.carlosxocop.veterinaria.repository.CitaMedicaRepository;
import org.carlosxocop.veterinaria.repository.ExpedienteClinicoRepository;
import org.carlosxocop.veterinaria.repository.MascotaRepository;
import org.carlosxocop.veterinaria.repository.UsuarioRepository;
import org.carlosxocop.veterinaria.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VeterinariaApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private CitaMedicaRepository citaMedicaRepository;

    @Autowired
    private ExpedienteClinicoRepository expedienteClinicoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Usuario adminUser;
    private Usuario vetUser;
    private Usuario clienteUser;
    private String adminToken;
    private String vetToken;
    private String clienteToken;

    @BeforeEach
    void setUp() {
        expedienteClinicoRepository.deleteAll();
        citaMedicaRepository.deleteAll();
        mascotaRepository.deleteAll();
        usuarioRepository.deleteAll();

        // 1 ADMIN
        adminUser = usuarioRepository.save(Usuario.builder()
                .nombre("Admin General")
                .email("admin@test.com")
                .password(passwordEncoder.encode("Admin123*"))
                .rol(Rol.ADMIN)
                .build());

        // 1 VET
        vetUser = usuarioRepository.save(Usuario.builder()
                .nombre("Dr. Roberto Martínez")
                .email("vet@test.com")
                .password(passwordEncoder.encode("Vet123*"))
                .rol(Rol.VET)
                .build());

        // 1 CLIENTE
        clienteUser = usuarioRepository.save(Usuario.builder()
                .nombre("Juan Cliente")
                .email("cliente@test.com")
                .password(passwordEncoder.encode("Cliente123*"))
                .rol(Rol.CLIENTE)
                .build());

        adminToken = "Bearer " + jwtService.generateToken(adminUser, adminUser.getRol());
        vetToken = "Bearer " + jwtService.generateToken(vetUser, vetUser.getRol());
        clienteToken = "Bearer " + jwtService.generateToken(clienteUser, clienteUser.getRol());
    }

    @Test
    @DisplayName("1. Registro de cliente exitoso")
    void test1_RegistroCliente() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .nombre("Nuevo Cliente")
                .telefono("12345678")
                .email("nuevo@cliente.com")
                .password("Password123*")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.email").value("nuevo@cliente.com"));
    }

    @Test
    @DisplayName("2. Login de usuario exitoso")
    void test2_Login() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("cliente@test.com")
                .password("Cliente123*")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    @DisplayName("3. Acceso protegido sin JWT es rechazado con 401 Unauthorized")
    void test3_AccesoProtegidoSinJwt() throws Exception {
        mockMvc.perform(get("/api/v1/mascotas/mis-mascotas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("4. Acceso con JWT exitoso")
    void test4_AccesoConJwt() throws Exception {
        mockMvc.perform(get("/api/v1/mascotas/mis-mascotas")
                        .header("Authorization", clienteToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("5. Restricción de roles (CLIENTE intenta acceder a endpoint exclusivo de VET/ADMIN recibe 403)")
    void test5_RestriccionRoles() throws Exception {
        mockMvc.perform(get("/api/v1/citas/agenda")
                        .header("Authorization", clienteToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("6. Registro de mascota exitoso (permite edad 0 para cachorros)")
    void test6_RegistroMascota() throws Exception {
        MascotaRequest request = MascotaRequest.builder()
                .nombre("Firulais")
                .especie(Especie.PERRO)
                .raza("Labrador")
                .edad(0)
                .build();

        mockMvc.perform(post("/api/v1/mascotas")
                        .header("Authorization", clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value("Firulais"))
                .andExpect(jsonPath("$.especie").value("PERRO"))
                .andExpect(jsonPath("$.edad").value(0))
                .andExpect(jsonPath("$.clienteId").value(clienteUser.getId()));
    }

    @Test
    @DisplayName("7. Consulta de mascotas propias del cliente autenticado")
    void test7_ConsultaMascotasPropias() throws Exception {
        mascotaRepository.save(Mascota.builder()
                .nombre("Pelusa")
                .especie(Especie.GATO)
                .raza("Persa")
                .edad(2)
                .cliente(clienteUser)
                .build());

        mockMvc.perform(get("/api/v1/mascotas/mis-mascotas")
                        .header("Authorization", clienteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Pelusa"));
    }

    @Test
    @DisplayName("8. Creación de cita médica exitosa con fecha futura")
    void test8_CreacionCita() throws Exception {
        Mascota mascota = mascotaRepository.save(Mascota.builder()
                .nombre("Max")
                .especie(Especie.PERRO)
                .cliente(clienteUser)
                .build());

        LocalDateTime fechaHora = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        CitaRequest request = CitaRequest.builder()
                .mascotaId(mascota.getId())
                .veterinarioId(vetUser.getId())
                .fechaHora(fechaHora)
                .motivo("Revisión anual")
                .build();

        mockMvc.perform(post("/api/v1/citas")
                        .header("Authorization", clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.motivo").value("Revisión anual"));
    }

    @Test
    @DisplayName("9. Conflicto de horario del veterinario (rechaza cita solapada con 409 Conflict)")
    void test9_ConflictoHorarioVeterinario() throws Exception {
        Mascota mascota = mascotaRepository.save(Mascota.builder()
                .nombre("Toby")
                .especie(Especie.PERRO)
                .cliente(clienteUser)
                .build());

        LocalDateTime fechaHora = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);

        // Crear primera cita a las 10:00
        citaMedicaRepository.save(CitaMedica.builder()
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(fechaHora)
                .motivo("Vacunación")
                .estado(EstadoCita.PENDIENTE)
                .build());

        // Intentar crear otra cita a las 10:15 (solapamiento dentro de los 30 min)
        CitaRequest requestSolapada = CitaRequest.builder()
                .mascotaId(mascota.getId())
                .veterinarioId(vetUser.getId())
                .fechaHora(fechaHora.plusMinutes(15))
                .motivo("Chequeo")
                .build();

        mockMvc.perform(post("/api/v1/citas")
                        .header("Authorization", clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestSolapada)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("10. Límite de 2 citas pendientes por cliente para el mismo día (rechazo con 400)")
    void test10_LimiteDosCitasPendientesCliente() throws Exception {
        Mascota mascota = mascotaRepository.save(Mascota.builder()
                .nombre("Rocky")
                .especie(Especie.PERRO)
                .cliente(clienteUser)
                .build());

        LocalDateTime fechaBase = LocalDateTime.now().plusDays(3).withHour(9).withMinute(0).withSecond(0).withNano(0);

        // Cita 1
        citaMedicaRepository.save(CitaMedica.builder()
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(fechaBase)
                .motivo("Cita 1")
                .estado(EstadoCita.PENDIENTE)
                .build());

        // Cita 2 (a las 11:00)
        citaMedicaRepository.save(CitaMedica.builder()
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(fechaBase.plusHours(2))
                .motivo("Cita 2")
                .estado(EstadoCita.PENDIENTE)
                .build());

        // Intento de Cita 3 en el mismo día
        CitaRequest requestTercera = CitaRequest.builder()
                .mascotaId(mascota.getId())
                .veterinarioId(vetUser.getId())
                .fechaHora(fechaBase.plusHours(4))
                .motivo("Cita 3 rechazada")
                .build();

        mockMvc.perform(post("/api/v1/citas")
                        .header("Authorization", clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestTercera)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("ya tiene 2 citas pendientes")));
    }

    @Test
    @DisplayName("11. Cancelación de cita exitosa con más de 2 horas de anticipación")
    void test11_CancelacionMasDeDosHoras() throws Exception {
        Mascota mascota = mascotaRepository.save(Mascota.builder()
                .nombre("Bruno")
                .especie(Especie.PERRO)
                .cliente(clienteUser)
                .build());

        // Cita en 24 horas (más de 2 horas)
        CitaMedica cita = citaMedicaRepository.save(CitaMedica.builder()
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(LocalDateTime.now().plusHours(24))
                .motivo("Desparasitación")
                .estado(EstadoCita.PENDIENTE)
                .build());

        mockMvc.perform(patch("/api/v1/citas/" + cita.getId() + "/cancelar")
                        .header("Authorization", clienteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        CitaMedica citaActualizada = citaMedicaRepository.findById(cita.getId()).orElseThrow();
        assertEquals(EstadoCita.CANCELADA, citaActualizada.getEstado());
    }

    @Test
    @DisplayName("12. Rechazo de cancelación de cita con menos de 2 horas de anticipación (400 Bad Request)")
    void test12_RechazoCancelacionMenosDeDosHoras() throws Exception {
        Mascota mascota = mascotaRepository.save(Mascota.builder()
                .nombre("Coco")
                .especie(Especie.AVE)
                .cliente(clienteUser)
                .build());

        // Cita en 1 hora (menos de 2 horas)
        CitaMedica cita = citaMedicaRepository.save(CitaMedica.builder()
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(LocalDateTime.now().plusHours(1))
                .motivo("Corte de uñas")
                .estado(EstadoCita.PENDIENTE)
                .build());

        mockMvc.perform(patch("/api/v1/citas/" + cita.getId() + "/cancelar")
                        .header("Authorization", clienteToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("2 horas de anticipación")));
    }

    @Test
    @DisplayName("13. Creación de expediente clínico exitoso por veterinario")
    void test13_CreacionExpedienteClinico() throws Exception {
        Mascota mascota = mascotaRepository.save(Mascota.builder()
                .nombre("Simba")
                .especie(Especie.GATO)
                .cliente(clienteUser)
                .build());

        CitaMedica cita = citaMedicaRepository.save(CitaMedica.builder()
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(LocalDateTime.now().minusHours(1))
                .motivo("Chequeo general")
                .estado(EstadoCita.PENDIENTE)
                .build());

        ExpedienteRequest request = ExpedienteRequest.builder()
                .citaId(cita.getId())
                .diagnostico("Gastroenteritis leve")
                .tratamiento("Dieta blanda y suero oral")
                .pesoKg(4.5)
                .build();

        mockMvc.perform(post("/api/v1/expedientes")
                        .header("Authorization", vetToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.diagnostico").value("Gastroenteritis leve"))
                .andExpect(jsonPath("$.pesoKg").value(4.5));
    }

    @Test
    @DisplayName("14. Cambio automático de estado de cita a COMPLETADA tras registrar expediente")
    void test14_CambioEstadoCitaACompletada() throws Exception {
        Mascota mascota = mascotaRepository.save(Mascota.builder()
                .nombre("Luna")
                .especie(Especie.PERRO)
                .cliente(clienteUser)
                .build());

        CitaMedica cita = citaMedicaRepository.save(CitaMedica.builder()
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(LocalDateTime.now().minusHours(2))
                .motivo("Vacunación antirrábica")
                .estado(EstadoCita.PENDIENTE)
                .build());

        ExpedienteRequest request = ExpedienteRequest.builder()
                .citaId(cita.getId())
                .diagnostico("Vacuna aplicada con éxito")
                .tratamiento("Monitorear posibles reacciones por 24h")
                .pesoKg(10.2)
                .build();

        mockMvc.perform(post("/api/v1/expedientes")
                        .header("Authorization", vetToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        CitaMedica citaActualizada = citaMedicaRepository.findById(cita.getId()).orElseThrow();
        assertEquals(EstadoCita.COMPLETADA, citaActualizada.getEstado());
    }

    @Test
    @DisplayName("15. Consulta de historial clínico de una mascota")
    void test15_ConsultaHistorialClinico() throws Exception {
        Mascota mascota = mascotaRepository.save(Mascota.builder()
                .nombre("Thor")
                .especie(Especie.PERRO)
                .cliente(clienteUser)
                .build());

        CitaMedica cita = citaMedicaRepository.save(CitaMedica.builder()
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(LocalDateTime.now().minusDays(5))
                .motivo("Otitis")
                .estado(EstadoCita.COMPLETADA)
                .build());

        ExpedienteRequest request = ExpedienteRequest.builder()
                .citaId(cita.getId())
                .diagnostico("Infección en oído derecho")
                .tratamiento("Gotas óticas cada 12 horas")
                .pesoKg(15.0)
                .build();

        // Crear el expediente primero
        mockMvc.perform(post("/api/v1/expedientes")
                        .header("Authorization", vetToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Consultar historial clínico con token del CLIENTE dueño
        mockMvc.perform(get("/api/v1/expedientes/mascota/" + mascota.getId())
                        .header("Authorization", clienteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].diagnostico").value("Infección en oído derecho"))
                .andExpect(jsonPath("$[0].pesoKg").value(15.0));
    }

    @Test
    @DisplayName("16. Login con contraseña inválida devuelve 401 Unauthorized")
    void test16_LoginInvalido() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("cliente@test.com")
                .password("PasswordIncorrecta")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("17. ADMIN crea un nuevo veterinario dinámicamente")
    void test17_AdminCreaVeterinario() throws Exception {
        UsuarioRequest request = UsuarioRequest.builder()
                .nombre("Dra. María Lopez")
                .telefono("55559988")
                .email("mlopez@veterinaria.com")
                .password("VetPass123*")
                .rol(Rol.VET)
                .build();

        mockMvc.perform(post("/api/v1/usuarios/veterinarios")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("VET"))
                .andExpect(jsonPath("$.email").value("mlopez@veterinaria.com"));
    }

    @Test
    @DisplayName("18. JSON malformado devuelve 400 Bad Request en vez de 500 con código MALFORMED_JSON_OR_ENUM")
    void test18_JsonMalformadoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"invalido\", unclosed json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigo").value("MALFORMED_JSON_OR_ENUM"));
    }

    @Test
    @DisplayName("19. ADMIN crea un nuevo administrador dinámicamente")
    void test19_AdminCreaAdministrador() throws Exception {
        UsuarioRequest request = UsuarioRequest.builder()
                .nombre("Admin Secundario")
                .telefono("55557788")
                .email("admin2@veterinaria.com")
                .password("AdminPass123*")
                .build();

        mockMvc.perform(post("/api/v1/usuarios/administradores")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.email").value("admin2@veterinaria.com"));
    }

    @Test
    @DisplayName("20. CLIENTE consulta sus propias citas médicas (GET /citas/mis-citas)")
    void test20_ClienteConsultaMisCitas() throws Exception {
        Mascota mascota = mascotaRepository.save(Mascota.builder()
                .nombre("Oso")
                .especie(Especie.PERRO)
                .cliente(clienteUser)
                .build());

        citaMedicaRepository.save(CitaMedica.builder()
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(LocalDateTime.now().plusDays(5))
                .motivo("Vacuna anual")
                .estado(EstadoCita.PENDIENTE)
                .build());

        mockMvc.perform(get("/api/v1/citas/mis-citas")
                        .header("Authorization", clienteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].mascotaNombre").value("Oso"))
                .andExpect(jsonPath("$[0].clienteId").value(clienteUser.getId()));
    }

    @Test
    @DisplayName("21. Cita con solapamiento permite reagendar si la cita anterior fue CANCELADA")
    void test21_PermiteCitaSiAnteriorEstaCancelada() throws Exception {
        Mascota mascota = mascotaRepository.save(Mascota.builder()
                .nombre("Rex")
                .especie(Especie.PERRO)
                .cliente(clienteUser)
                .build());

        LocalDateTime fechaHora = LocalDateTime.now().plusDays(4).withHour(11).withMinute(0).withSecond(0).withNano(0);

        // Cita cancelada previamente en ese horario
        citaMedicaRepository.save(CitaMedica.builder()
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(fechaHora)
                .motivo("Consulta previa cancelada")
                .estado(EstadoCita.CANCELADA)
                .build());

        // Nueva cita a la misma hora debe ser permitida
        CitaRequest request = CitaRequest.builder()
                .mascotaId(mascota.getId())
                .veterinarioId(vetUser.getId())
                .fechaHora(fechaHora)
                .motivo("Nueva consulta")
                .build();

        mockMvc.perform(post("/api/v1/citas")
                        .header("Authorization", clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("22. Validación de contraseña rechaza formatos inválidos (sin mayúscula, sin número, sin símbolo o < 8 caracteres)")
    void test22_ValidacionPasswordInvalida() throws Exception {
        // Sin símbolo ni mayúscula
        RegisterRequest req1 = RegisterRequest.builder()
                .nombre("Test")
                .telefono("12345678")
                .email("test1@val.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validaciones.password").isNotEmpty());

        // Menos de 8 caracteres
        RegisterRequest req2 = RegisterRequest.builder()
                .nombre("Test")
                .telefono("12345678")
                .email("test2@val.com")
                .password("Ab1*")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validaciones.password").isNotEmpty());

        // Sin número
        RegisterRequest req3 = RegisterRequest.builder()
                .nombre("Test")
                .telefono("12345678")
                .email("test3@val.com")
                .password("Password*")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req3)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validaciones.password").isNotEmpty());

        // Sin símbolo
        RegisterRequest req4 = RegisterRequest.builder()
                .nombre("Test")
                .telefono("12345678")
                .email("test4@val.com")
                .password("Password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req4)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validaciones.password").isNotEmpty());
    }

    @Test
    @DisplayName("23. Validación de teléfono rechaza números que no tengan exactamente 8 dígitos numéricos")
    void test23_ValidacionTelefonoInvalido() throws Exception {
        // Con guiones (no solo números)
        RegisterRequest req1 = RegisterRequest.builder()
                .nombre("Test")
                .telefono("555-1234")
                .email("test5@val.com")
                .password("Password123*")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validaciones.telefono").isNotEmpty());

        // Menos de 8 dígitos
        RegisterRequest req2 = RegisterRequest.builder()
                .nombre("Test")
                .telefono("1234567")
                .email("test6@val.com")
                .password("Password123*")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validaciones.telefono").isNotEmpty());

        // Más de 8 dígitos
        RegisterRequest req3 = RegisterRequest.builder()
                .nombre("Test")
                .telefono("123456789")
                .email("test7@val.com")
                .password("Password123*")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req3)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validaciones.telefono").isNotEmpty());
    }
}

