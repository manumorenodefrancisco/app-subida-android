package com.example.app_subida;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.app_subida.data.AppDatabase;
import com.example.app_subida.data.entities.Tarea;
import com.example.app_subida.data.entities.Usuario;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class TareasFragment extends Fragment {

    private AppDatabase database;
    private TextView tvXpHoy;
    private TextView tvTiempoRestanteHeader;
    private TextView tvContadorProgreso;
    private TextView tvTiempoRestante;
    private ProgressBar progressDiario;
    private LinearLayout containerProgresoDiario;
    private LinearLayout penalizacionLayout;
    private TextView tabTodas, tabDiarias, tabSemanales;
    private RecyclerView recyclerTareas;
    private TareasAdapter adapter;
    private Handler handler;
    private Runnable actualizadorTiempo;
    private String tabActual = "TODAS";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tareas_fragment, container, false);

        // Inicializar base de datos
        database = AppDatabase.getInstance(requireContext());

        tvXpHoy = rootView.findViewById(R.id.tv_xp_hoy);
        tvTiempoRestanteHeader = rootView.findViewById(R.id.tv_tiempo_restante_header);
        tvContadorProgreso = rootView.findViewById(R.id.tv_contador_progreso);
        tvTiempoRestante = rootView.findViewById(R.id.tv_tiempo_restante);
        progressDiario = rootView.findViewById(R.id.progressDiario);
        containerProgresoDiario = rootView.findViewById(R.id.container_progreso_diario);
        penalizacionLayout = rootView.findViewById(R.id.penalizacion_tareas);

        tabTodas = rootView.findViewById(R.id.tab_todas);
        tabDiarias = rootView.findViewById(R.id.tab_diarias);
        tabSemanales = rootView.findViewById(R.id.tab_semanales);

        recyclerTareas = rootView.findViewById(R.id.recycler_tareas);
        recyclerTareas.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TareasAdapter(this::mostrarDialogTareaCompletada);
        recyclerTareas.setAdapter(adapter);

        LinearLayout btnCrearTarea = rootView.findViewById(R.id.btn_crear_tarea);
        btnCrearTarea.setOnClickListener(v -> mostrarDialogCrearTarea());

        configurarTabs();
        cargarDatosIniciales();
        iniciarActualizadorTiempo();

        return rootView;
    }

    private void configurarTabs() {
        tabTodas.setOnClickListener(v -> cambiarTab("TODAS"));
        tabDiarias.setOnClickListener(v -> cambiarTab("DIARIAS"));
        tabSemanales.setOnClickListener(v -> cambiarTab("SEMANALES"));
    }

    private void cambiarTab(String tab) {
        tabActual = tab;

        tabTodas.setBackgroundResource(R.drawable.filter_tab_inactive);
        tabTodas.setTextColor(ContextCompat.getColor(requireContext(), R.color.gris_azulado));
        tabDiarias.setBackgroundResource(R.drawable.filter_tab_inactive);
        tabDiarias.setTextColor(ContextCompat.getColor(requireContext(), R.color.gris_azulado));
        tabSemanales.setBackgroundResource(R.drawable.filter_tab_inactive);
        tabSemanales.setTextColor(ContextCompat.getColor(requireContext(), R.color.gris_azulado));

        switch (tab) {
            case "TODAS":
                tabTodas.setBackgroundResource(R.drawable.filter_tab_active);
                tabTodas.setTextColor(ContextCompat.getColor(requireContext(), R.color.azul_brillante));
                containerProgresoDiario.setVisibility(View.VISIBLE);
                break;
            case "DIARIAS":
                tabDiarias.setBackgroundResource(R.drawable.filter_tab_active);
                tabDiarias.setTextColor(ContextCompat.getColor(requireContext(), R.color.azul_brillante));
                containerProgresoDiario.setVisibility(View.VISIBLE);
                break;
            case "SEMANALES":
                tabSemanales.setBackgroundResource(R.drawable.filter_tab_active);
                tabSemanales.setTextColor(ContextCompat.getColor(requireContext(), R.color.azul_brillante));
                containerProgresoDiario.setVisibility(View.GONE);
                break;
        }

        cargarTareasSegunTab();
    }

    private void cargarDatosIniciales() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Usuario usuario = database.usuarioDao().getUsuario();
            int xpTotal = (usuario != null) ? usuario.estadisticas.xpTotal : 0;
            int racha = (usuario != null) ? usuario.estadisticas.rachaDias : 0;

            String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            int totalDiarias = database.tareaDao().contarDiariasPorFecha(hoy);

            int completadasDiarias = database.tareaDao().contarDiariasCompletadasPorFecha(hoy);

            int finalXpHoy = xpTotal;
            int finalTotalDiarias = totalDiarias;
            int finalCompletadasDiarias = completadasDiarias;
            int finalRacha = racha;

            requireActivity().runOnUiThread(() -> {
                tvXpHoy.setText("+" + finalXpHoy);

                tvContadorProgreso.setText(finalCompletadasDiarias + "/" + finalTotalDiarias + " COMPLETADAS");

                if (finalTotalDiarias > 0) {
                    // Calcular porcentaje: (completadas * 100) / total
                    int porcentaje = (finalCompletadasDiarias * 100) / finalTotalDiarias;
                    progressDiario.setProgress(porcentaje);
                } else {
                    progressDiario.setProgress(0);
                }

                if (finalRacha >= 1) {
                    penalizacionLayout.setVisibility(View.GONE);
                } else {
                    penalizacionLayout.setVisibility(View.VISIBLE);
                }

                actualizarTiempoRestante();
            });
        });

        cargarTareasSegunTab();
    }

    private void cargarTareasSegunTab() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Tarea> tareas = new ArrayList<>();
            String fechaActual = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());

            if (tabActual.equals("TODAS")) {
                tareas = database.tareaDao().getActivas();
            } else if (tabActual.equals("DIARIAS")) {
                tareas = database.tareaDao().getPorPeriodicidad("DIARIA");
            } else if (tabActual.equals("SEMANALES")) {
                tareas = database.tareaDao().getPorPeriodicidad("SEMANAL");
            }

            List<Tarea> finalTareas = tareas;
            requireActivity().runOnUiThread(() -> {
                adapter.setTareas(finalTareas);
            });
        });
    }

    private void iniciarActualizadorTiempo() {
        handler = new Handler();
        actualizadorTiempo = new Runnable() {
            @Override
            public void run() {
                actualizarTiempoRestante();
                handler.postDelayed(this, 60000);
            }
        };
        handler.post(actualizadorTiempo);
    }

    private void actualizarTiempoRestante() {
        //23:59:59
        Calendar ahora = Calendar.getInstance();
        Calendar finDelDia = Calendar.getInstance();
        finDelDia.set(Calendar.HOUR_OF_DAY, 23);
        finDelDia.set(Calendar.MINUTE, 59);
        finDelDia.set(Calendar.SECOND, 59);

        long diffMillis = finDelDia.getTimeInMillis() - ahora.getTimeInMillis();

        long horas = (diffMillis / (1000 * 60 * 60)) % 24;
        long minutos = (diffMillis / (1000 * 60)) % 60;

        String tiempoFormateado = horas + "h " + minutos + "m";

        if (tvTiempoRestanteHeader != null) {
            tvTiempoRestanteHeader.setText("REINICIO EN ◈ " + tiempoFormateado);
        }
        if (tvTiempoRestante != null) {
            tvTiempoRestante.setText("Tiempo restante: " + tiempoFormateado);
        }
    }

    //mismo metodo que en HomeFragment
    private void mostrarDialogCrearTarea() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_crear_tarea);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText etTitulo = dialog.findViewById(R.id.et_titulo_tarea);
        EditText etDescripcion = dialog.findViewById(R.id.et_descripcion_tarea);
        RadioGroup rgPeriodicidad = dialog.findViewById(R.id.rg_periodicidad);
        RadioButton rbDiaria = dialog.findViewById(R.id.rb_diaria);
        RadioButton rbSemanal = dialog.findViewById(R.id.rb_semanal);
        RadioButton rbUnica = dialog.findViewById(R.id.rb_unica);
        SwitchCompat switchRutinaria = dialog.findViewById(R.id.switch_rutinaria);
        LinearLayout containerDiasPersonalizados = dialog.findViewById(R.id.container_dias_personalizados);
        RadioGroup rgDificultad = dialog.findViewById(R.id.rg_dificultad);
        Button btnCancelar = dialog.findViewById(R.id.btn_cancelar);
        Button btnCrear = dialog.findViewById(R.id.btn_crear);

        AppCompatCheckBox cbLunes = dialog.findViewById(R.id.cb_lunes);
        AppCompatCheckBox cbMartes = dialog.findViewById(R.id.cb_martes);
        AppCompatCheckBox cbMiercoles = dialog.findViewById(R.id.cb_miercoles);
        AppCompatCheckBox cbJueves = dialog.findViewById(R.id.cb_jueves);
        AppCompatCheckBox cbViernes = dialog.findViewById(R.id.cb_viernes);
        AppCompatCheckBox cbSabado = dialog.findViewById(R.id.cb_sabado);
        AppCompatCheckBox cbDomingo = dialog.findViewById(R.id.cb_domingo);

        View containerSwitchRutinaria = dialog.findViewById(R.id.container_switch_rutinaria);

        View.OnClickListener actualizarVisibilidad = v -> {
            boolean esSemanal = rbSemanal.isChecked();
            boolean esUnica = rbUnica.isChecked();
            boolean esRutinaria = switchRutinaria.isChecked();

            if (esUnica) {
                containerSwitchRutinaria.setVisibility(View.GONE);
                switchRutinaria.setChecked(false); // Desactivar si estaba activo
            } else {
                containerSwitchRutinaria.setVisibility(View.VISIBLE);
            }

            containerDiasPersonalizados.setVisibility(
                (esSemanal && esRutinaria) ? View.VISIBLE : View.GONE
            );
        };

        rgPeriodicidad.setOnCheckedChangeListener((group, checkedId) -> actualizarVisibilidad.onClick(null));
        switchRutinaria.setOnCheckedChangeListener((buttonView, isChecked) -> actualizarVisibilidad.onClick(null));

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnCrear.setOnClickListener(v -> {
            String titulo = etTitulo.getText().toString().trim();
            if (titulo.isEmpty()) {
                Toast.makeText(requireContext(), "El título es obligatorio", Toast.LENGTH_SHORT).show();
                return;
            }

            String descripcion = etDescripcion.getText().toString().trim();

            String periodicidad = "UNICA";
            if (rbDiaria.isChecked()) {
                periodicidad = "DIARIA";
            } else if (rbSemanal.isChecked()) {
                periodicidad = "SEMANAL";
            }

            boolean esRutinaria = switchRutinaria.isChecked();

            List<String> diasSeleccionados = new ArrayList<>();
            if (cbLunes.isChecked()) diasSeleccionados.add("L");
            if (cbMartes.isChecked()) diasSeleccionados.add("M");
            if (cbMiercoles.isChecked()) diasSeleccionados.add("X");
            if (cbJueves.isChecked()) diasSeleccionados.add("J");
            if (cbViernes.isChecked()) diasSeleccionados.add("V");
            if (cbSabado.isChecked()) diasSeleccionados.add("S");
            if (cbDomingo.isChecked()) diasSeleccionados.add("D");

            String diasSemana = diasSeleccionados.isEmpty() ? null : String.join(",", diasSeleccionados);

            String dificultad = "FACIL";
            int dificultadChecked = rgDificultad.getCheckedRadioButtonId();
            if (dificultadChecked == R.id.rb_facil) dificultad = "FACIL";
            else if (dificultadChecked == R.id.rb_media) dificultad = "MEDIA";
            else if (dificultadChecked == R.id.rb_dificil) dificultad = "DIFICIL";

            Tarea nuevaTarea = new Tarea();
            nuevaTarea.titulo = titulo;
            nuevaTarea.descripcion = descripcion;
            nuevaTarea.periodicidad = periodicidad;
            nuevaTarea.esRutinaria = esRutinaria;
            nuevaTarea.diasSemana = diasSemana;
            nuevaTarea.dificultad = dificultad;
            nuevaTarea.xpRecompensa = Tarea.xpPorDificultad(dificultad);
            nuevaTarea.monedasRecompensa = Tarea.monedasPorDificultad(dificultad);
            nuevaTarea.aceptada = true;
            nuevaTarea.completada = false;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            nuevaTarea.fechaCreacion = sdf.format(new Date());

            Calendar cal = Calendar.getInstance();
            if (periodicidad.equals("DIARIA")) {
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                nuevaTarea.fechaLimite = sdf.format(cal.getTime());
            } else if (periodicidad.equals("SEMANAL")) {
                int diaActual = cal.get(Calendar.DAY_OF_WEEK);
                int diasHastaDomingo = Calendar.SUNDAY - diaActual;
                if (diasHastaDomingo < 0) diasHastaDomingo += 7;
                cal.add(Calendar.DAY_OF_MONTH, diasHastaDomingo);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                nuevaTarea.fechaLimite = sdf.format(cal.getTime());
            } else {
                nuevaTarea.fechaLimite = null;
            }

            nuevaTarea.fechaCompletado = null;
            nuevaTarea.idTareaPlantilla = null;

            /*
            String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            int tareasHoy = database.tareaDao().contarCreadasPorFecha(hoy);
            if (tareasHoy >= 5) {
                Toast.makeText(requireContext(), "Máximo 5 tareas por día", Toast.LENGTH_SHORT).show();
                return;
            }*/

            // Guardar en BD
            guardarTarea(nuevaTarea);

            dialog.dismiss();
            Toast.makeText(requireContext(), "Tarea creada con éxito", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void guardarTarea(Tarea tarea) {
        Executors.newSingleThreadExecutor().execute(() -> {
            database.tareaDao().insertar(tarea);

            String mensajeToast = "Tarea creada con éxito";
            if (tarea.esRutinaria) {
                if (tarea.periodicidad.equals("DIARIA")) {
                    mensajeToast = "Tarea diaria rutinaria creada. Se recreará automáticamente cada día ♻️";
                } else if (tarea.periodicidad.equals("SEMANAL")) {
                    mensajeToast = "Tarea semanal rutinaria creada. Se recreará automáticamente cada semana ♻️";
                }
            }

            String finalMensaje = mensajeToast;
            requireActivity().runOnUiThread(() -> {
                // Recargar datos y tareas
                cargarDatosIniciales();
                cargarTareasSegunTab();
                Toast.makeText(requireContext(), finalMensaje, Toast.LENGTH_LONG).show();
            });
        });
    }

    private void mostrarDialogTareaCompletada(Tarea tarea) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_completar_tarea);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvTareaDialog = dialog.findViewById(R.id.tv_tarea_dialog);
        Button btnCancelar = dialog.findViewById(R.id.btn_cancelar);
        Button btnCompletar = dialog.findViewById(R.id.btn_completar);

        tvTareaDialog.setText(tarea.titulo);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnCompletar.setOnClickListener(v -> {
            completarTarea(tarea);
            dialog.dismiss();
        });

        dialog.show();
    }

    // Completar tarea cuando se hace click
    private void completarTarea(Tarea tarea) {
        Executors.newSingleThreadExecutor().execute(() -> {
            tarea.completada = true;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            tarea.fechaCompletado = sdf.format(new Date());

            database.tareaDao().actualizar(tarea);

            Usuario usuario = database.usuarioDao().getUsuario();
            if (usuario != null) {
                usuario.estadisticas.xpTotal += tarea.xpRecompensa;
                database.usuarioDao().actualizar(usuario);
            }

            database.registroSistemaDao().logXP(tarea.xpRecompensa, tarea.titulo);

            int totalCompletadas = database.tareaDao().contarTodasCompletadas();
            if (totalCompletadas == 1) {
                database.registroSistemaDao().logPrimeraCompletada();
            }

            // Verificar milestones
            if (totalCompletadas % 10 == 0 && totalCompletadas > 0) {
                database.registroSistemaDao().logMilestone(totalCompletadas);
            }

            String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            int tareasHoy = database.tareaDao().contarCompletadasPorFecha(hoy);
            if (tareasHoy == 5) {
                database.registroSistemaDao().logDiaProductivo(tareasHoy);
            }

            requireActivity().runOnUiThread(() -> {
                cargarDatosIniciales();
                cargarTareasSegunTab();
                Toast.makeText(requireContext(), "¡Tarea completada! +" + tarea.xpRecompensa + " XP", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Detener el actualizador de tiempo
        if (handler != null && actualizadorTiempo != null) {
            handler.removeCallbacks(actualizadorTiempo);
        }
    }

    private void regenerarTareasRecurrentes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat sdfFecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String hoy = sdfFecha.format(new Date());
            Calendar cal = Calendar.getInstance();

            List<Tarea> plantillas = database.tareaDao().obtenerPlantillas();

            for (Tarea plantilla : plantillas) {
                int yaExiste = database.tareaDao().existeInstanciaParaFecha((int) plantilla.id, hoy);

                if (yaExiste == 0) {
                    Tarea nuevaInstancia = new Tarea();
                    nuevaInstancia.titulo = plantilla.titulo;
                    nuevaInstancia.descripcion = plantilla.descripcion;
                    nuevaInstancia.periodicidad = plantilla.periodicidad;
                    nuevaInstancia.esRutinaria = plantilla.esRutinaria;
                    nuevaInstancia.diasSemana = plantilla.diasSemana;
                    nuevaInstancia.dificultad = plantilla.dificultad;
                    nuevaInstancia.xpRecompensa = plantilla.xpRecompensa;
                    nuevaInstancia.monedasRecompensa = plantilla.monedasRecompensa;
                    nuevaInstancia.aceptada = true;
                    nuevaInstancia.completada = false;
                    nuevaInstancia.fechaCreacion = sdf.format(new Date());

                    nuevaInstancia.idTareaPlantilla = (int) plantilla.id;

                    if (plantilla.periodicidad.equals("DIARIA")) {
                        // 23:59:59
                        cal.set(Calendar.HOUR_OF_DAY, 23);
                        cal.set(Calendar.MINUTE, 59);
                        cal.set(Calendar.SECOND, 59);
                        nuevaInstancia.fechaLimite = sdf.format(cal.getTime());
                    } else if (plantilla.periodicidad.equals("SEMANAL")) {
                        //domingo a las 23:59:59
                        int diaActual = cal.get(Calendar.DAY_OF_WEEK);
                        int diasHastaDomingo = Calendar.SUNDAY - diaActual;
                        if (diasHastaDomingo < 0) diasHastaDomingo += 7;
                        cal.add(Calendar.DAY_OF_MONTH, diasHastaDomingo);
                        cal.set(Calendar.HOUR_OF_DAY, 23);
                        cal.set(Calendar.MINUTE, 59);
                        cal.set(Calendar.SECOND, 59);
                        nuevaInstancia.fechaLimite = sdf.format(cal.getTime());
                    }

                    nuevaInstancia.fechaCompletado = null;

                    database.tareaDao().insertar(nuevaInstancia);
                }
            }
        });
    }


}