package org.carlosxocop.veterinaria;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class VeterinariaApplication {

    @PostConstruct
    public void init() {
        // Establecer zona horaria unificada para toda la aplicacion
        TimeZone.setDefault(TimeZone.getTimeZone("America/Guatemala"));
    }

    public static void main(String[] args) {
        SpringApplication.run(VeterinariaApplication.class, args);
    }
}
