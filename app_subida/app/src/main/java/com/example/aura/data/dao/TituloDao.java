package com.example.aura.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aura.data.entities.Titulo;
import java.util.List;

@Dao
public interface TituloDao {

    @Insert
    void insertar(Titulo titulo);

    @Update
    void actualizar(Titulo titulo);

    @Query("SELECT * FROM titulos ORDER BY desbloqueado DESC, nivelRequerido ASC")
    List<Titulo> getAll();

    @Query("SELECT * FROM titulos WHERE desbloqueado = 1")
    List<Titulo> getDesbloqueados();

    @Query("SELECT * FROM titulos WHERE activo = 1 LIMIT 1")
    Titulo getActivo();

    @Query("UPDATE titulos SET activo = 0")
    void desactivarTodos();

    @Query("UPDATE titulos SET activo = 1 WHERE id = :id")
    void activar(int id);

    @Query("UPDATE titulos SET desbloqueado = 1, fechaDesbloqueo = :fecha WHERE id = :id")
    void desbloquear(int id, String fecha);
}
