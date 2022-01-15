package com.encens.khipus.model.rest;

public class NitVerificationResponsePOJO {

    private String mensaje;
    private Integer code;
    private Long nitParaVerificacion;

    public NitVerificationResponsePOJO() {
    }

    public NitVerificationResponsePOJO(String mensaje, Integer code, Long nitParaVerificacion) {
        this.mensaje = mensaje;
        this.code = code;
        this.nitParaVerificacion = nitParaVerificacion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Long getNitParaVerificacion() {
        return nitParaVerificacion;
    }

    public void setNitParaVerificacion(Long nitParaVerificacion) {
        this.nitParaVerificacion = nitParaVerificacion;
    }
}
