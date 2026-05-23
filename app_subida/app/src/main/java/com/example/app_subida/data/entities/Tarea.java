package com.example.app_subida.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tareas")
public class Tarea {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String titulo;
    public String descripcion;

    // Periodicidad
    public String periodicidad;
    public boolean esRutinaria;// si true, se regenera automáticamente
    public String diasSemana;

    // Dificultad y recompensas
    public String dificultad;
    public int xpRecompensa = 0;
    public int monedasRecompensa = 0;

    // Estado
    public boolean aceptada = true;// por defecto true, false para tareas opcionales
    public boolean completada = false;

    // Fechas
    public String fechaCreacion;
    public String fechaLimite;
    public String fechaCompletado;       // ISO 8601, null hasta completarse

    // Para tareas rutinarias: referencia a la plantilla original
    // Si es null, esta tarea ES la plantilla o es una tarea única
    // Si tiene valor, esta tarea es una instancia generada de esa plantilla
    public Integer idTareaPlantilla;

    public static int xpPorDificultad(String dificultad) {
        switch (dificultad) {
            case "FACIL": return 10;
            case "MEDIA": return 25;
            case "DIFICIL": return 50;
            default: return 10;
        }
    }

    public static int monedasPorDificultad(String dificultad) {
        switch (dificultad) {
            case "FACIL": return 5;
            case "MEDIA": return 15;
            case "DIFICIL": return 30;
            default: return 5;
        }
    }
}
