package com.example.app_subida.data.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.app_subida.data.AppDatabase;
import com.example.app_subida.data.utils.FechaUtils;

public class LimpiezaWorker extends Worker {

    public LimpiezaWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());

            String limite60Dias = FechaUtils.haceNDias(60);
            db.podometroDao().eliminarAntiguos(limite60Dias);

            String limite6Meses = FechaUtils.haceNMeses(6);
            db.progresoDiarioDao().eliminarAntiguos(limite6Meses);

            String limite3Meses = FechaUtils.haceNMeses(3);
            db.tareaDao().eliminarCompletadasAntiguas(limite3Meses);

            db.registroSistemaDao().limpiarExceso();

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
