package com.example.app_subida.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "objeto")
public class Objeto {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String nombre = "";
    public String descripcion = "";
    public String tipo = "OBJETO";// "ARMA", "ARMADURA", "ACCESORIO", "AURA"
    public String rareza = "COMUN";// "COMUN", "RARO", "EPICO", "LEGENDARIO"

    public int nivelRequerido = 0;
    public int precioMonedas = 0;
    public boolean comprado = false;
    public boolean equipado = false;

    public String drawableNombre = "";// nombre drawable a renderizar
    public String fechaObtencion = "";// ISO 8601, null hasta comprarse
}
