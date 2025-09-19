package com.proyecto_it.mercado_oficio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class MercadoOficioApplication {

	public static void main(String[] args) {
		SpringApplication.run(MercadoOficioApplication.class, args);
	}

}
