package com.example.aura.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "registro_sistema")
public class RegistroSistema {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String mensaje = "";
    public String tipo = "";// "NIVEL", "LOGRO", "XP", "RACHA", "ADVERTENCIA"
    public String iconoDrawable;// bg_log_badge_logro, bg_log_badge_nivel, etc.
    public String fechaHora;// ISO 8601 con hora
    public boolean leido;
}
