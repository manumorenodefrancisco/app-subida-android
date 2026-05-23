package com.example.aura.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.aura.data.entities.Usuario;

@Dao
public interface UsuarioDao {

    @Insert
    void insertar(Usuario usuario);

    @Update
    void actualizar(Usuario usuario);

    @Query("SELECT * FROM usuario WHERE id = " + Usuario.USER_ID + " LIMIT 1")
    Usuario getUsuario();
}
