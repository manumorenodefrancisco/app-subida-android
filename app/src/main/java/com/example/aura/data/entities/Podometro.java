package com.example.aura.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
@Entity(tableName = "podometro")
public class Podometro {
    @PrimaryKey
    @NonNull

    public String fecha;// YYYY-MM-DD

    public int pasos = 0;
    public double calorias = 0;
    public double distanciaKm = 0;
    public int minutosActividad = 0;
}
