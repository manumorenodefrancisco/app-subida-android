package com.example.app_subida.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.app_subida.data.entities.ProgresoDiario;
import java.util.List;

@Dao
public interface ProgresoDiarioDao {

    @Insert
    void insertar(ProgresoDiario progreso);

    @Update
    void actualizar(ProgresoDiario progreso);

    @Query("SELECT * FROM progreso_diario WHERE fecha = :fecha LIMIT 1")
    ProgresoDiario getPorFecha(String fecha);

    @Query("SELECT * FROM progreso_diario WHERE fecha BETWEEN :inicio AND :fin ORDER BY fecha DESC")
    List<ProgresoDiario> getRango(String inicio, String fin);

    @Query("SELECT * FROM progreso_diario WHERE diaSuperado = 1 ORDER BY fecha DESC LIMIT 30")
    List<ProgresoDiario> getUltimos30DiasSuperados();

    // Reciclaje: eliminar registros de más de 6 meses
    @Query("DELETE FROM progreso_diario WHERE fecha < :fechaLimite")
    void eliminarAntiguos(String fechaLimite);
}
