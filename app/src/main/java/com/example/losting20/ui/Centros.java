package com.example.losting20.ui;

public class Centros {
    String latitud;
    String longitud;
    String direccio;

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getDireccio() {
        return direccio;
    }

    public void setDireccio(String direccio) {
        this.direccio = direccio;
    }

    @Override
    public String toString() {
        return "Centros{" +
                "latitud='" + latitud + '\'' +
                ", longitud='" + longitud + '\'' +
                ", direccio='" + direccio + '\'' +
                '}';
    }
}
