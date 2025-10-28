package com.proyecto_it.mercado_oficio.Domain.Service.Mensaje;

import com.proyecto_it.mercado_oficio.Domain.Model.Mensaje;
import com.proyecto_it.mercado_oficio.Domain.Model.Multimedia;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MensajeService {
    Mensaje enviarMensaje(Mensaje mensaje, List<MultipartFile> archivos);
    Mensaje obtenerMensajePorId(Integer id);
    List<Mensaje> obtenerMensajesDeChat(Integer usuario1Id, Integer usuario2Id);
    List<Multimedia> obtenerArchivosAdjuntos(Integer mensajeId);
    void eliminarMensaje(Integer id);
}