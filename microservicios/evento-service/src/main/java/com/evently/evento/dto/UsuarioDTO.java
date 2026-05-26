package com.evently.evento.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String rol;
    private LocalDateTime fechaRegistro;
    private Boolean activo;
}
