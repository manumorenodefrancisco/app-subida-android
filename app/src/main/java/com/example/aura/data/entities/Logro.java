package com.example.aura.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "logros")
public class Logro {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nombre = "";
    public String descripcion = "";
    public String iconoDrawable = ""; //nombre del drawable del icono

    public int xpRecompensa = 0;
    public int monedasRecompensa = 0;

    // Progreso
    public String condicion = "";
    public int progresoActual = 0;
    public int progresoObjetivo = 0;

    // Estado
    public boolean desbloqueado = false;
    public String fechaDesbloqueo = "";// ISO 8601, null hasta desbloquearse
}
