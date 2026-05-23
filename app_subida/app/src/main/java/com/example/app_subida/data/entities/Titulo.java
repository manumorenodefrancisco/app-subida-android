package com.example.app_subida.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "titulos")
public class Titulo {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nombre;
    public String descripcion;

    public int nivelRequerido;
    public String colorHex;

    public boolean desbloqueado;
    public boolean activo;
    public String fechaDesbloqueo;// ISO 8601, null hasta desbloquearse
}
