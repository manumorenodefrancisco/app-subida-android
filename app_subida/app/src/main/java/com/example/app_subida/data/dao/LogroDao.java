package com.example.app_subida.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.app_subida.data.entities.Logro;
import java.util.List;

@Dao
public interface LogroDao {

    @Insert
    void insertar(Logro logro);

    @Update
    void actualizar(Logro logro);

    @Query("SELECT * FROM logros ORDER BY desbloqueado DESC, id ASC")
    List<Logro> getAll();

    @Query("SELECT * FROM logros WHERE desbloqueado = 1 ORDER BY fechaDesbloqueo DESC")
    List<Logro> getDesbloqueados();

    @Query("SELECT * FROM logros WHERE desbloqueado = 0")
    List<Logro> getBloqueados();

    @Query("UPDATE logros SET progresoActual = :progreso WHERE id = :id")
    void actualizarProgreso(int id, int progreso);

    @Query("UPDATE logros SET desbloqueado = 1, fechaDesbloqueo = :fecha WHERE id = :id")
    void desbloquear(int id, String fecha);
}
