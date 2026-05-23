package com.example.app_subida;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.app_subida.data.AppDatabase;
import com.example.app_subida.data.entities.Usuario;
import com.example.app_subida.data.entities.Titulo;
import com.example.app_subida.data.entities.Logro;
import java.util.List;
import java.util.concurrent.Executors;

public class PerfilFragment extends Fragment {
    private AppDatabase db;
    private TextView tvNombre, tvTitulo, tvNivel, tvXpTotal, tvRacha, tvProgresoXp, tvProgresoNivel;
    private ProgressBar xpBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.perfil_fragment, container, false);
        
        db = AppDatabase.getInstance(requireContext());
        
        tvNombre = view.findViewById(R.id.tv_nombre_usuario);
        tvTitulo = view.findViewById(R.id.tv_titulo_activo);
        tvNivel = view.findViewById(R.id.tv_nivel);
        tvXpTotal = view.findViewById(R.id.tv_xp_total);
        tvRacha = view.findViewById(R.id.tv_racha);
        tvProgresoNivel = view.findViewById(R.id.tv_progreso_nivel);
        tvProgresoXp = view.findViewById(R.id.tv_progreso_xp);
        xpBar = view.findViewById(R.id.xp_bar);
        
        cargarDatos();
        
        return view;
    }
    
    private void cargarDatos() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Usuario usuario = db.usuarioDao().getUsuario();
            List<Titulo> titulos = db.tituloDao().getDesbloqueados();
            List<Logro> logros = db.logroDao().getDesbloqueados();
            
            requireActivity().runOnUiThread(() -> {
                if (usuario != null) {
                    tvNombre.setText(usuario.nombre.isEmpty() ? "JUGADOR" : usuario.nombre.toUpperCase());
                    tvTitulo.setText(usuario.personaje.tituloActivo);
                    
                    int nivel = Usuario.calcularNivel(usuario.estadisticas.xpTotal);
                    tvNivel.setText(String.valueOf(nivel));
                    
                    String xpFormateado = formatearXp(usuario.estadisticas.xpTotal);
                    tvXpTotal.setText(xpFormateado);
                    
                    //tvRacha.setText(usuario.estadisticas.rachaDias + "d");
                    tvRacha.setText(String.valueOf(usuario.estadisticas.rachaDias));

                    int nivelSiguiente = nivel + 1;
                    tvProgresoNivel.setText("PROGRESO NIV." + nivel + " → " + nivelSiguiente);

                    int xpActual = Usuario.xpNivelActual(usuario.estadisticas.xpTotal);
                    int xpSiguiente = Usuario.xpNivelSiguiente(usuario.estadisticas.xpTotal);
                    tvProgresoXp.setText(formatearXp(xpActual) + " / " + formatearXp(xpSiguiente));
                    
                    float progreso = Usuario.progresoNivelActual(usuario.estadisticas.xpTotal);
                    xpBar.setProgress((int)(progreso * 100));
                }
            });
        });
    }
    
    private String formatearXp(int xp) {
        if (xp >= 1000000) {
            return (xp / 1000000) + "M";
        } else if (xp >= 1000) {
            return (xp / 1000) + "K";
        }
        return String.valueOf(xp);
    }
}