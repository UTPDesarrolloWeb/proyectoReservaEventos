package com.evently.notificacion.dto;

public class WhatsAppRequest {
    private Long usuarioId;
    private String telefono;
    private String mensaje;
    private String tipo;
    private String email;
    private String pdfBase64;
    private String pdfNombre;

    public WhatsAppRequest() {}

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPdfBase64() { return pdfBase64; }
    public void setPdfBase64(String pdfBase64) { this.pdfBase64 = pdfBase64; }
    public String getPdfNombre() { return pdfNombre; }
    public void setPdfNombre(String pdfNombre) { this.pdfNombre = pdfNombre; }
}


