package com.example.aura.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aura.data.entities.RegistroSistema;
import com.example.aura.data.utils.FechaUtils;
import java.util.List;

@Dao
public interface RegistroSistemaDao {

    @Insert
    void insertar(RegistroSistema registro);

    @Update
    void actualizar(RegistroSistema registro);


    @Query("SELECT * FROM registro_sistema ORDER BY fechaHora DESC LIMIT :limite")
    List<RegistroSistema> getRecientes(int limite);

    @Query("SELECT * FROM registro_sistema WHERE tipo = :tipo ORDER BY fechaHora DESC LIMIT 20")
    List<RegistroSistema> getPorTipo(String tipo);

    @Query("SELECT * FROM registro_sistema WHERE leido = 0 ORDER BY fechaHora DESC")
    List<RegistroSistema> getNoLeidos();

    @Query("UPDATE registro_sistema SET leido = 1 WHERE id = :id")
    void marcarLeido(int id);

    @Query("UPDATE registro_sistema SET leido = 1")
    void marcarTodosLeidos();

    // mantener solo los últimos 70 registros
    @Query("DELETE FROM registro_sistema WHERE id NOT IN (SELECT id FROM registro_sistema ORDER BY fechaHora DESC LIMIT 70)")
    void limpiarExceso();



    default void logLogro(String nombreLogro) {
        RegistroSistema reg = new RegistroSistema();
        reg.mensaje = "¡Desbloqueaste «" + nombreLogro + "»!";
        reg.tipo = "LOGRO";
        reg.iconoDrawable = "bg_log_badge_logro";
        reg.fechaHora = FechaUtils.ahora();
        insertar(reg);
        limpiarExceso();
    }

    default void logXP(int cantidad, String motivo) {
        RegistroSistema reg = new RegistroSistema();
        reg.mensaje = "+" + cantidad + " XP · " + motivo;
        reg.tipo = "XP";
        reg.iconoDrawable = "bg_log_badge_xp";
        reg.fechaHora = FechaUtils.ahora();
        insertar(reg);
        limpiarExceso();
    }

    default void logNivel(int nivel) {
        RegistroSistema reg = new RegistroSistema();
        reg.mensaje = "¡Subiste al nivel " + nivel + "! Nuevas misiones desbloqueadas";
        reg.tipo = "NIVEL";
        reg.iconoDrawable = "bg_log_badge_nivel";
        reg.fechaHora = FechaUtils.ahora();
        insertar(reg);
        limpiarExceso();
    }

    default void logRacha(int dias) {
        RegistroSistema reg = new RegistroSistema();
        reg.mensaje = "Racha de " + dias + " días consecutivos mantenida.";
        reg.tipo = "RACHA";
        reg.iconoDrawable = "bg_log_badge_racha";
        reg.fechaHora = FechaUtils.ahora();
        insertar(reg);
        limpiarExceso();
    }

    default void logBienvenida(int nivel) {
        RegistroSistema reg = new RegistroSistema();
        reg.mensaje = "¡Bienvenido al sistema! Eres nivel " + nivel + ". Tu viaje comienza ahora ⚡";
        reg.tipo = "SISTEMA";
        reg.iconoDrawable = "bg_log_badge_nivel";
        reg.fechaHora = FechaUtils.ahora();
        insertar(reg);
        limpiarExceso();
    }

    default void logPrimeraTarea() {
        RegistroSistema reg = new RegistroSistema();
        reg.mensaje = "¡Primera tarea creada! Tu camino hacia la grandeza empieza ya :)";
        reg.tipo = "LOGRO";
        reg.iconoDrawable = "bg_log_badge_logro";
        reg.fechaHora = FechaUtils.ahora();
        insertar(reg);
        limpiarExceso();
    }

    default void logPrimeraCompletada() {
        RegistroSistema reg = new RegistroSistema();
        reg.mensaje = "¡Primera tarea completada! Sigue con esa productividad!";
        reg.tipo = "LOGRO";
        reg.iconoDrawable = "bg_log_badge_logro";
        reg.fechaHora = FechaUtils.ahora();
        insertar(reg);
        limpiarExceso();
    }

    default void logRachaPerdida() {
        RegistroSistema reg = new RegistroSistema();
        reg.mensaje = "Tu racha se reinició. No te rindas, ¡vuelve más fuerte!";
        reg.tipo = "ADVERTENCIA";
        reg.iconoDrawable = "bg_log_badge_racha";
        reg.fechaHora = FechaUtils.ahora();
        insertar(reg);
        limpiarExceso();
    }

    default void logMilestone(int totalTareas) {
        RegistroSistema reg = new RegistroSistema();
        reg.mensaje = "¡Hito alcanzado! " + totalTareas + " tareas completadas en total 🏆";
        reg.tipo = "LOGRO";
        reg.iconoDrawable = "bg_log_badge_logro";
        reg.fechaHora = FechaUtils.ahora();
        insertar(reg);
        limpiarExceso();
    }

    default void logDiaProductivo(int tareasCompletadas) {
        RegistroSistema reg = new RegistroSistema();
        reg.mensaje = "¡Día productivo! Completaste " + tareasCompletadas + " tareas hoy.";
        reg.tipo = "XP";
        reg.iconoDrawable = "bg_log_badge_xp";
        reg.fechaHora = FechaUtils.ahora();
        insertar(reg);
        limpiarExceso();
    }


}
