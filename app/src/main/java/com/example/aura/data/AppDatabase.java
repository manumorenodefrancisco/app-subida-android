package com.example.aura.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.aura.data.dao.*;
import com.example.aura.data.entities.*;

@Database(entities = {
        Usuario.class,
        Logro.class,
        Titulo.class,
        Objeto.class,
        Tarea.class,
        ProgresoDiario.class,
        Podometro.class,
        RegistroSistema.class
    },
    version = 5,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();
    public abstract LogroDao logroDao();
    public abstract TituloDao tituloDao();
    public abstract ObjetoDao objetoDao();
    public abstract TareaDao tareaDao();
    public abstract ProgresoDiarioDao progresoDiarioDao();
    public abstract PodometroDao podometroDao();
    public abstract RegistroSistemaDao registroSistemaDao();

    // Singleton
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "aura_database"
                    )
                    .fallbackToDestructiveMigration() // eliminar en producción
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
