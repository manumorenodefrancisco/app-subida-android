package com.example.aura;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aura.data.entities.Tarea;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TareasAdapter extends RecyclerView.Adapter<TareasAdapter.TareaViewHolder> {

    private List<Tarea> tareas = new ArrayList<>();
    private final OnTareaClickListener listener;

    public interface OnTareaClickListener {
        void onTareaClick(Tarea tarea);
    }

    public TareasAdapter(OnTareaClickListener listener) {
        this.listener = listener;
    }

    public void setTareas(List<Tarea> tareas) {
        this.tareas = tareas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tarea, parent, false);
        return new TareaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        Tarea tarea = tareas.get(position);

        holder.tvTitulo.setText(tarea.titulo);

        if (tarea.descripcion != null && !tarea.descripcion.isEmpty()) {
            holder.tvDescripcion.setText(tarea.descripcion);
            holder.tvDescripcion.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescripcion.setVisibility(View.GONE);
        }

        if (tarea.fechaLimite != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date fechaLimite = sdf.parse(tarea.fechaLimite);
                Date ahora = new Date();

                long diffMillis = fechaLimite.getTime() - ahora.getTime();
                long horas = diffMillis / (1000 * 60 * 60);

                if (horas > 24) {
                    long dias = horas / 24;
                    holder.tvFecha.setText("Vence en " + dias + " días");
                } else if (horas > 0) {
                    holder.tvFecha.setText("Vence en " + horas + " horas");
                } else {
                    holder.tvFecha.setText("Vence pronto");
                }
            } catch (Exception e) {
                holder.tvFecha.setText("Fecha límite próxima");
            }
        } else {
            holder.tvFecha.setText("Sin fecha límite");
        }

        holder.tvXp.setText("+" + tarea.xpRecompensa + " XP");

        // Usamos el contexto de la view para el color
        holder.ivIcono.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && !tarea.completada) {
                listener.onTareaClick(tarea);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tareas.size();
    }

    public static class TareaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcono;
        TextView tvTitulo;
        TextView tvDescripcion;
        TextView tvFecha;
        TextView tvXp;

        public TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcono = itemView.findViewById(R.id.iv_icono_tarea);
            tvTitulo = itemView.findViewById(R.id.tv_titulo_tarea);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion_tarea);
            tvFecha = itemView.findViewById(R.id.tv_fecha_tarea);
            tvXp = itemView.findViewById(R.id.tv_xp_tarea);
        }
    }
}
