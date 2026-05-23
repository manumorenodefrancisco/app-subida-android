package com.example.aura.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aura.data.entities.Tarea;
import java.util.List;

@Dao
public interface TareaDao {

    @Insert
    long insertar(Tarea tarea);  // Retorna el ID de la tarea insertada

    @Update
    void actualizar(Tarea tarea);

    @Delete
    void eliminar(Tarea tarea);

    @Query("SELECT * FROM tareas WHERE completada = 0 AND aceptada = 1 ORDER BY fechaLimite ASC")
    List<Tarea> getActivas();

    @Query("SELECT * FROM tareas WHERE completada = 1 ORDER BY fechaCompletado DESC")
    List<Tarea> getCompletadas();

    @Query("SELECT * FROM tareas WHERE periodicidad = :tipo AND completada = 0 AND aceptada = 1")
    List<Tarea> getPorPeriodicidad(String tipo);

    @Query("SELECT * FROM tareas WHERE fechaLimite = :fecha AND completada = 0 AND aceptada = 1")
    List<Tarea> getPorFechaLimite(String fecha);

    @Query("SELECT COUNT(*) FROM tareas WHERE completada = 1 AND fechaCompletado LIKE :fecha || '%'")
    int contarCompletadasEnFecha(String fecha);

    @Query("SELECT * FROM tareas WHERE idTareaPlantilla IS NULL AND esRutinaria = 1")
    List<Tarea> getPlantillasRutinarias();

    @Query("UPDATE tareas SET completada = 1, fechaCompletado = :fecha WHERE id = :id")
    void marcarCompletada(int id, String fecha);

    /*// Obtener tareas que aún no han expirado (para el contador y lista del home)
    @Query("SELECT * FROM tareas WHERE aceptada = 1 AND fechaLimite >= :fechaActual ORDER BY fechaLimite ASC")
    List<Tarea> getTareasVigentes(String fechaActual);*/

    //contador del Home
    @Query("SELECT COUNT(*) FROM tareas WHERE aceptada = 1 AND (fechaLimite >= :fechaActual OR fechaLimite IS NULL) AND completada = 0")
    int contarVigentes(String fechaActual);

    @Query("SELECT COUNT(*) FROM tareas WHERE aceptada = 1 AND (fechaLimite >= :fechaActual OR fechaLimite IS NULL) AND completada = 1")
    int contarCompletadasVigentes(String fechaActual);

    @Query("SELECT * FROM tareas WHERE aceptada = 1 AND (fechaLimite >= :fechaActual OR fechaLimite IS NULL) AND completada = 0 ORDER BY CASE WHEN fechaLimite IS NULL THEN 1 ELSE 0 END, fechaLimite ASC LIMIT 2")
    List<Tarea> getTop2Pendientes(String fechaActual);

    //eliminar tareas completadas de mas de 3 meses
    @Query("DELETE FROM tareas WHERE completada = 1 AND fechaCompletado < :fechaLimite")
    void eliminarCompletadasAntiguas(String fechaLimite);

    //progreso semanal
    @Query("SELECT COUNT(*) FROM tareas WHERE DATE(fechaCompletado) = :fecha")
    int contarTotalPorFecha(String fecha);

    @Query("SELECT COUNT(*) FROM tareas WHERE completada = 1 AND DATE(fechaCompletado) = :fecha")
    int contarCompletadasPorFecha(String fecha);

    @Query("SELECT COUNT(*) FROM tareas WHERE completada = 1 AND DATE(fechaCompletado) >= :fechaInicio AND DATE(fechaCompletado) <= :fechaFin")
    int contarCompletadasEnRango(String fechaInicio, String fechaFin);

    @Query("SELECT COUNT(*) FROM tareas WHERE completada = 1")
    int contarTodasCompletadas();

    @Query("SELECT SUM(xpRecompensa) FROM tareas WHERE completada = 1 AND DATE(fechaCompletado) >= :fechaInicio AND DATE(fechaCompletado) <= :fechaFin")
    int sumarXpEnRango(String fechaInicio, String fechaFin);

    // Contar tareas DIARIAS cuya fecha límite sea hoy (YYYY-MM-DD)
    @Query("SELECT COUNT(*) FROM tareas WHERE periodicidad = 'DIARIA' AND aceptada = 1 AND DATE(fechaLimite) = :fecha")
    int contarDiariasPorFecha(String fecha);

    // Contar tareas DIARIAS completadas cuya fecha límite sea hoy
    @Query("SELECT COUNT(*) FROM tareas WHERE periodicidad = 'DIARIA' AND completada = 1 AND DATE(fechaLimite) = :fecha")
    int contarDiariasCompletadasPorFecha(String fecha);

        // Obtener todas las plantillas (tareas DIARIAS o SEMANALES que son plantillas base Y rutinarias)
        @Query("SELECT * FROM tareas WHERE (periodicidad = 'DIARIA' OR periodicidad = 'SEMANAL') AND esRutinaria = 1 AND idTareaPlantilla IS NULL AND aceptada = 1")
        List<Tarea> obtenerPlantillas();

        // Verificar si ya existe una instancia de una plantilla para una fecha específica
        @Query("SELECT COUNT(*) FROM tareas WHERE idTareaPlantilla = :idPlantilla AND DATE(fechaLimite) = :fecha")
        int existeInstanciaParaFecha(int idPlantilla, String fecha);

        // Obtener plantilla por ID
        @Query("SELECT * FROM tareas WHERE id = :id LIMIT 1")
        Tarea obtenerPorId(int id);

    // Contar tareas CREADAS hoy
    @Query("SELECT COUNT(*) FROM tareas WHERE DATE(fechaCreacion) = :fecha")
    int contarCreadasPorFecha(String fecha);
}
