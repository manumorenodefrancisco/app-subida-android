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

    @Query("UPDATE usuario SET xpTotal = xpTotal + :xp WHERE id = " + Usuario.USER_ID)
    void agregarXp(int xp);

    @Query("UPDATE usuario SET monedas = monedas + :cantidad WHERE id = " + Usuario.USER_ID)
    void agregarMonedas(int cantidad);

    @Query("UPDATE usuario SET rachaDias = :dias WHERE id = " + Usuario.USER_ID)
    void actualizarRacha(int dias);

    @Query("UPDATE usuario SET tituloActivo = :titulo WHERE id = " + Usuario.USER_ID)
    void cambiarTitulo(String titulo);
}
