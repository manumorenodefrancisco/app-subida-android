package com.example.app_subida.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.app_subida.data.entities.Podometro;
import java.util.List;

@Dao
public interface PodometroDao {

    @Insert
    void insertar(Podometro podometro);

    @Update
    void actualizar(Podometro podometro);

    @Query("SELECT * FROM podometro WHERE fecha = :fecha LIMIT 1")
    Podometro getPorFecha(String fecha);

    @Query("SELECT * FROM podometro WHERE fecha BETWEEN :inicio AND :fin ORDER BY fecha ASC")
    List<Podometro> getRango(String inicio, String fin);

    @Query("SELECT SUM(pasos) FROM podometro WHERE fecha BETWEEN :inicio AND :fin")
    int getPasosTotales(String inicio, String fin);

    @Query("SELECT SUM(calorias) FROM podometro WHERE fecha BETWEEN :inicio AND :fin")
    double getCaloriasTotales(String inicio, String fin);

    @Query("SELECT SUM(distanciaKm) FROM podometro WHERE fecha BETWEEN :inicio AND :fin")
    double getDistanciaTotal(String inicio, String fin);

    @Query("SELECT * FROM podometro ORDER BY fecha DESC LIMIT 7")
    List<Podometro> getPasosSemanales();

    // Reciclaje: eliminar registros de más de 60 días
    @Query("DELETE FROM podometro WHERE fecha < :fechaLimite")
    void eliminarAntiguos(String fechaLimite);
}
