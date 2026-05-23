package com.example.app_subida.data.entities;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuario")
public class Usuario {
    public static final int USER_ID = 1;

    @PrimaryKey
    public int id = USER_ID;
    public String nombre = "";;
    public String fechaUltimoLogin = "";
    @Embedded
    public Estadisticas estadisticas = new Estadisticas();
    @Embedded
    public Personaje personaje = new Personaje();


    public static class Estadisticas {
        public int xpTotal = 0;
        public int monedas = 0;
        public int rachaDias = 0;
    }
    public static class Personaje {
        public String tituloActivo = "<<Novato>>";
        public String avatarDrawable = "default_avatar";;
        public float intensidadAura = 0.0f; // 0.0f - 1.0f
    }

    // Curva exponencial: nivel 100 requiere muchísima XP
    public static int calcularNivel(int xpTotal) {
        // Fórmula: nivel = sqrt(xpTotal / 100)
        // Nivel 1 = 100 XP, Nivel 10 = 10,000 XP, Nivel 100 = 1,000,000 XP
        return (int) Math.sqrt(xpTotal / 100.0);
    }

    public static int xpParaNivel(int nivel) {
        return nivel * nivel * 100;
    }

    public static int xpNivelActual(int xpTotal) {
        int nivel = calcularNivel(xpTotal);
        return xpTotal - xpParaNivel(nivel);
    }

    public static int xpNivelSiguiente(int xpTotal) {
        int nivel = calcularNivel(xpTotal);
        return xpParaNivel(nivel + 1) - xpParaNivel(nivel);
    }

    //(0.0 - 1.0)
    public static float progresoNivelActual(int xpTotal) {
        return (float) xpNivelActual(xpTotal) / xpNivelSiguiente(xpTotal);
    }

    //(0.0 - 1.0, máximo en nivel 100)
    public static float calcularIntensidadAura(int xpTotal) {
        int nivel = calcularNivel(xpTotal);
        if (nivel >= 100) return 1.0f;
        return nivel / 100.0f;
    }
}
