package com.example.app_subida.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "progreso_diario")
public class ProgresoDiario {
    @PrimaryKey
    @NonNull
    public String fecha = "";// YYYY-MM-DD
    public int tareasCreadas = 0;
    public int tareasCompletadas = 0;
    public boolean diaSuperado = false;// true si tareasCompletadas >= tareasObjetivo
    public int xpGanada = 0;
    public int monedasGanadas = 0;
    public boolean penalizacionActiva = false;   // si hubo penalización por no cumplir,
                                    // por ejemplo si la racha es 0 hay penalizacion si es >0 no hay
}
