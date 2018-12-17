package ar.edu.utn.frsf.isi.dam.laboratorio05.modelo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.io.File;

@Entity
public class Reclamo {
    public enum TipoReclamo { VEREDAS,SEMAFOROS,ILUMINACION,CALLE_EN_MAL_ESTADO,RESIDUOS,RUIDOS_MOLESTOS,OTRO}

    @PrimaryKey(autoGenerate = true)
    private long id;
    private Double latitud;
    private Double longitud;
    private String reclamo;
    private String email;
    private String pathFotoReclamo;
    private String pathAudioReclamo;

    @TypeConverters(TipoReclamoConverter.class)
    private TipoReclamo tipo;

    public String getPathFotoReclamo() {
        return pathFotoReclamo;
    }

    public void setPathFotoReclamo(String pathFotoReclamo) {
        this.pathFotoReclamo = pathFotoReclamo;
    }

    public String getPathAudioReclamo() {
        return pathAudioReclamo;
    }

    public void setPathAudioReclamo(String pathAudioReclamo) {
        this.pathAudioReclamo = pathAudioReclamo;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getReclamo() {
        return reclamo;
    }

    public void setReclamo(String reclamo) {
        this.reclamo = reclamo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TipoReclamo getTipo() {
        return tipo;
    }

    public void setTipo(TipoReclamo tipo) {
        this.tipo = tipo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
