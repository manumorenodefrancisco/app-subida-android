package com.example.aura.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aura.data.entities.Objeto;
import java.util.List;

@Dao
public interface ObjetoDao {

    @Insert
    void insertar(Objeto objeto);

    @Update
    void actualizar(Objeto objeto);

    @Query("SELECT * FROM objeto ORDER BY comprado DESC, nivelRequerido ASC")
    List<Objeto> getAll();

    @Query("SELECT * FROM objeto WHERE comprado = 1")
    List<Objeto> getComprado();

    @Query("SELECT * FROM objeto WHERE equipado = 1")
    List<Objeto> getEquipado();

    @Query("SELECT * FROM objeto WHERE tipo = :tipo AND equipado = 1 LIMIT 1")
    Objeto getEquipadoPorTipo(String tipo);

    @Query("UPDATE objeto SET equipado = 0 WHERE tipo = :tipo")
    void desequiparTipo(String tipo);

    @Query("UPDATE objeto SET equipado = 1 WHERE id = :id")
    void equipar(int id);

    @Query("UPDATE objeto SET comprado = 1, fechaObtencion = :fecha WHERE id = :id")
    void comprar(int id, String fecha);
}
