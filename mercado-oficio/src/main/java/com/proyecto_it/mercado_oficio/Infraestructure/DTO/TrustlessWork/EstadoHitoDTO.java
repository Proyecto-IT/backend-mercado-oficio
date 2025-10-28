package com.proyecto_it.mercado_oficio.Infraestructure.DTO.TrustlessWork;

import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito.HitoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoHitoDTO {
    private HitoDTO hito;
    private TrustlessWorkEscrowData escrowData;
}
