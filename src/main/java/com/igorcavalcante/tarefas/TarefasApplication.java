package com.igorcavalcante.tarefas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO) // JSON de paginação estável
public class TarefasApplication {

    public static void main(String[] args) {
        SpringApplication.run(TarefasApplication.class, args);
    }
}
