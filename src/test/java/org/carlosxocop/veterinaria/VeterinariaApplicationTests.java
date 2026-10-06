package org.carlosxocop.veterinaria;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.carlosxocop.veterinaria.dto.auth.AuthResponse;
import org.carlosxocop.veterinaria.dto.auth.LoginRequest;
import org.carlosxocop.veterinaria.dto.auth.RegisterRequest;
import org.carlosxocop.veterinaria.dto.cita.CitaRequest;
import org.carlosxocop.veterinaria.dto.expediente.ExpedienteRequest;
import org.carlosxocop.veterinaria.dto.mascota.MascotaRequest;
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
import org.springframework.test.web.servlet.MvcResult;

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
                .password(passwordEncoder.encode("admin123"))
                .rol(Rol.ADMIN)
                .build());

        // 1 VET
        vetUser = usuarioRepository.save(Usuario.builder()
                .nombre("Dr. Veterinario")
                .email("vet@test.com")
                .password(passwordEncoder.encode("vet123"))
                .rol(Rol.VET)
                .build());

        // 1 CLIENTE
        clienteUser = usuarioRepository.save(Usuario.builder()
                .nombre("Juan Cliente")
                .email("cliente@test.com")
                .password(passwordEncoder.encode("cliente123"))
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
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.email").value("nuevo@cliente.com"));
    }

    @Test
    @DisplayName("2. Login de usuario exitoso")
    void test2_Login() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("cliente@test.com")
                .password("cliente123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    @DisplayName("3. Acceso protegido sin JWT es rechazado (403/401)")
    void test3_AccesoProtegidoSinJwt() throws Exception {
        mockMvc.perform(get("/api/v1/mascotas/mis-mascotas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("4. Acceso con JWT exitoso")
    void test4_AccesoConJwt() throws Exception {
        mockMvc.perform(get("/api/v1/mascotas/mis-mascotas")
                        .header("Authorization", clienteToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("5. Restricción de roles (CLIENTE intenta acceder a endpoint exclusivo de VET/ADMIN)")
    void test5_RestriccionRoles() throws Exception {
        mockMvc.perform(get("/api/v1/citas/agenda")
                        .header("Authorization", clienteToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("6. Registro de mascota exitoso")
    void test6_RegistroMascota() throws Exception {
        MascotaRequest request = MascotaRequest.builder()
                .nombre("Firulais")
                .especie(Especie.PERRO)
                .raza("Labrador")
                .edad(4)
                .build();

        mockMvc.perform(post("/api/v1/mascotas")
                        .header("Authorization", clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value("Firulais"))
                .andExpect(jsonPath("$.especie").value("PERRO"))
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
    @DisplayName("8. Creación de cita médica exitosa")
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
    @DisplayName("9. Conflicto de horario del veterinario (rechaza cita solapada)")
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
    @DisplayName("10. Límite de 2 citas pendientes por cliente para el mismo día")
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
    @DisplayName("12. Rechazo de cancelación de cita con menos de 2 horas de anticipación")
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
}
