package com.example.aura;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.aura.data.AppDatabase;
import com.example.aura.data.entities.Tarea;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private AppDatabase database;
    private LinearLayout containerTareasPendientes;
    private LinearLayout containerLogs;
    private TextView tvContadorTareas;
    private TextView tvDiasActivos;
    private TextView tvTareasCompletadas;
    private TextView tvXpTotal;
    private TextView tvUserHome;
    private TextView tvRangoSub;
    private TextView tvDiasRacha;
    private TextView tvNivelHome;
    private boolean esPrimeraVez = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment, container, false);

        // Inicializar bd Room
        database = AppDatabase.getInstance(requireContext());

        containerTareasPendientes = rootView.findViewById(R.id.container_tareas_dinamicas);
        tvContadorTareas = rootView.findViewById(R.id.tv_contador_tareas);
        tvDiasActivos = rootView.findViewById(R.id.tv_dias_activos);
        tvTareasCompletadas = rootView.findViewById(R.id.tv_tareas_completadas);
        tvXpTotal = rootView.findViewById(R.id.tv_xp_total);
        tvUserHome = rootView.findViewById(R.id.user_home);
        tvRangoSub = rootView.findViewById(R.id.tvRangoSub);
        tvDiasRacha = rootView.findViewById(R.id.tv_dias_racha);
        tvNivelHome = rootView.findViewById(R.id.tv_nivel_home);
        Button btnCrearTarea = rootView.findViewById(R.id.btn_crear_tarea_home);

        btnCrearTarea.setOnClickListener(v -> mostrarDialogCrearTarea());

        verificarPrimeraVez();
        cargarDatosUsuario();
        cargarTareasPendientes();
        actualizarProgresoSemanal();
        cargarRegistrosSistema();

        return rootView;
    }

    public void cargarDatosUsuario() {
        Executors.newSingleThreadExecutor().execute(() -> {
            com.example.aura.data.entities.Usuario usuario = database.usuarioDao().getUsuario();

            requireActivity().runOnUiThread(() -> {
                if (usuario != null) {
                    String nombre = usuario.nombre.isEmpty() ? "JUGADOR" : usuario.nombre.toUpperCase();
                    tvUserHome.setText(nombre);

                    tvRangoSub.setText(usuario.personaje.tituloActivo);

                    tvDiasRacha.setText("Días de racha: " + usuario.estadisticas.rachaDias);

                    int nivel = com.example.aura.data.entities.Usuario.calcularNivel(usuario.estadisticas.xpTotal);
                    tvNivelHome.setText(String.valueOf(nivel));
                }
            });
        });
    }

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

        // Contenedor del switch de rutinaria
        LinearLayout containerSwitchRutinaria = dialog.findViewById(R.id.container_switch_rutinaria);

        // Actualizar visibilidad de switch y días personalizados según periodicidad
        View.OnClickListener actualizarVisibilidad = v -> {
            boolean esUnica = rbUnica.isChecked();
            boolean esDiaria = rbDiaria.isChecked();
            boolean esSemanal = rbSemanal.isChecked();
            boolean esRutinaria = switchRutinaria.isChecked();

            // Mostrar switch solo si es DIARIA o SEMANAL
            if (esDiaria || esSemanal) {
                containerSwitchRutinaria.setVisibility(View.VISIBLE);
            } else {
                containerSwitchRutinaria.setVisibility(View.GONE);
                switchRutinaria.setChecked(false); // Desactivar si estaba activo
            }

            // Mostrar días personalizados solo si es SEMANAL y RUTINARIA
            containerDiasPersonalizados.setVisibility(
                (esSemanal && esRutinaria) ? View.VISIBLE : View.GONE
            );
        };

        rgPeriodicidad.setOnCheckedChangeListener((group, checkedId) -> actualizarVisibilidad.onClick(null));
        switchRutinaria.setOnCheckedChangeListener((buttonView, isChecked) -> actualizarVisibilidad.onClick(null));

        // Llamar al inicio para configurar visibilidad inicial (UNICA está seleccionada por defecto)
        actualizarVisibilidad.onClick(null);

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
            }
            else if (rbSemanal.isChecked()) {
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

            Tarea nuevaTarea = new Tarea(); //crear objeto de entidad Tarea
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
            nuevaTarea.fechaCreacion = sdf.format(new Date()); // Fecha actual

            // Calcular fecha límite con clase Calendar según la periodicidad elegida
            Calendar cal = Calendar.getInstance();
            if (periodicidad.equals("DIARIA")) {
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
            } else if (periodicidad.equals("SEMANAL")) {
                int diaActual = cal.get(Calendar.DAY_OF_WEEK);
                int diasHastaDomingo = Calendar.SUNDAY - diaActual;
                if (diasHastaDomingo < 0) diasHastaDomingo += 7;
                cal.add(Calendar.DAY_OF_MONTH, diasHastaDomingo);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
            } else {
                nuevaTarea.fechaLimite = null;
            }
            if (!periodicidad.equals("UNICA")) {
                nuevaTarea.fechaLimite = sdf.format(cal.getTime());
            }
            nuevaTarea.fechaCompletado = null;
            nuevaTarea.idTareaPlantilla = null; // null porque es una tarea nueva, no una instancia

            guardarTarea(nuevaTarea);//Guardar en Room

            dialog.dismiss();
            // Toast se muestra en guardarTarea()
        });

        dialog.show();
    }

    //guarda una tarea en la base de datos en un hilo secundario y luego recarga las tareas pendientes en el UI
    private void guardarTarea(Tarea tarea) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Guardar la tarea en la BD
            database.tareaDao().insertar(tarea);

            // Verificar si es la primera tarea creada (usando SharedPreferences)
            SharedPreferences prefs = requireActivity().getSharedPreferences("aura_prefs", Context.MODE_PRIVATE);
            boolean primeraTareaCreada = prefs.getBoolean("primera_tarea_creada", false);
            if (!primeraTareaCreada) {
                database.registroSistemaDao().logPrimeraTarea();
                prefs.edit().putBoolean("primera_tarea_creada", true).apply();
            }

            // Mensaje informativo según tipo y si es rutinaria
            String mensajeToast = "Tarea creada con éxito";

            if (tarea.periodicidad.equals("DIARIA") && tarea.esRutinaria) {
                mensajeToast = "Tarea diaria rutinaria creada. Se recreará automáticamente cada día ♻️";
            } else if (tarea.periodicidad.equals("SEMANAL") && tarea.esRutinaria) {
                mensajeToast = "Tarea semanal rutinaria creada. Se recreará automáticamente cada semana ♻️";
            } else if (tarea.periodicidad.equals("DIARIA")) {
                mensajeToast = "Tarea diaria creada. Desaparecerá al completarse";
            } else if (tarea.periodicidad.equals("SEMANAL")) {
                mensajeToast = "Tarea semanal creada. Desaparecerá al completarse";
            }

            String finalMensaje = mensajeToast;
            requireActivity().runOnUiThread(() -> {
                cargarTareasPendientes();
                actualizarProgresoSemanal();
                cargarRegistrosSistema();
                Toast.makeText(requireContext(), finalMensaje, Toast.LENGTH_LONG).show();
            });
        });
    }

    private void cargarTareasPendientes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            String fechaActual = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());

            int totalVigentes = database.tareaDao().contarVigentes(fechaActual);

            //solo 2 para evitar tener que crear un recyclerview
            List<Tarea> top2Pendientes = database.tareaDao().getTop2Pendientes(fechaActual);

            requireActivity().runOnUiThread(() -> {
                if (tvContadorTareas != null) {
                    tvContadorTareas.setText(totalVigentes + " pendientes");
                }

                limpiarTareasExistentes(); //para volverlas a cargar

                for (Tarea tarea : top2Pendientes) {
                    crearCardTarea(tarea);
                }
            });
        });
    }

    private void limpiarTareasExistentes() {
        if (containerTareasPendientes != null) {
            containerTareasPendientes.removeAllViews();
        }
    }

    private void actualizarProgresoSemanal() {
        Executors.newSingleThreadExecutor().execute(() -> {
            String[] ids = {"dia_lunes", "dia_martes", "dia_miercoles", "dia_jueves",
                    "dia_viernes", "dia_sabado", "dia_domingo"};

            // Obtener el día de hoy
            Calendar hoy = Calendar.getInstance();
            int diaHoyDeLaSemana = hoy.get(Calendar.DAY_OF_WEEK); // 1=Domingo, 2=Lunes, ..., 7=Sábado

            // Convertir a índice 0-6 donde 0=Lunes, 6=Domingo
            int diaHoyIndice;
            if (diaHoyDeLaSemana == Calendar.SUNDAY) {
                diaHoyIndice = 6; // Domingo es el último día
            } else {
                diaHoyIndice = diaHoyDeLaSemana - 2; // Lunes=0, Martes=1, ..., Sábado=5
            }

            // Calcular el lunes de esta semana
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -diaHoyIndice); // Retroceder hasta el lunes
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            String fechaInicio = sdf.format(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 6);
            String fechaFin = sdf.format(cal.getTime());

            int diasActivos = 0;
            int tareasCompletadasSemana = database.tareaDao().contarCompletadasEnRango(fechaInicio, fechaFin);
            int xpTotalSemana = database.tareaDao().sumarXpEnRango(fechaInicio, fechaFin);

            // Volver al lunes para recorrer los días
            cal.add(Calendar.DAY_OF_MONTH, -6);

            for (int i = 0; i < 7; i++) {
                String idDia = ids[i];
                boolean esFuturo = i > diaHoyIndice;
                String fecha = sdf.format(cal.getTime());

                int total = database.tareaDao().contarTotalPorFecha(fecha);
                int completadas = database.tareaDao().contarCompletadasPorFecha(fecha);

                if (completadas > 0) {
                    diasActivos++;
                }

                int viewId = getResources().getIdentifier(idDia, "id", requireContext().getPackageName());

                int backgroundRes = 0;
                boolean mostrarCheck = false;
                boolean mostrarContador = true;
                String textoContador = "—";

                if (esFuturo) {
                    backgroundRes = R.drawable.bg_dia_futuro;
                    textoContador = "—";
                } else if (total > 0 && completadas == total) {
                    backgroundRes = R.drawable.bg_dia_completado;
                    mostrarCheck = true;
                    mostrarContador = false;
                } else if (total > 0) {
                    backgroundRes = R.drawable.bg_dia_hoy;
                    textoContador = completadas + "/" + total;
                } else {
                    backgroundRes = R.drawable.bg_dia_hoy;
                    textoContador = "—";
                }

                int finalViewId = viewId;
                int finalBackgroundRes = backgroundRes;
                boolean finalMostrarCheck = mostrarCheck;
                boolean finalMostrarContador = mostrarContador;
                String finalTextoContador = textoContador;
                int finalDiasActivos = diasActivos;
                int finalTareasCompletadas = tareasCompletadasSemana;
                int finalXpTotal = xpTotalSemana;

                requireActivity().runOnUiThread(() -> {
                    View dia = requireView().findViewById(finalViewId);
                    if (dia != null) {
                        if (finalBackgroundRes != 0) {
                            dia.setBackgroundResource(finalBackgroundRes);
                        } else {
                            dia.setBackground(null);
                        }

                        ImageView ivCheck = dia.findViewById(R.id.iv_check);
                        TextView tvCounter = dia.findViewById(R.id.tv_counter);

                        if (ivCheck != null) {
                            ivCheck.setVisibility(finalMostrarCheck ? View.VISIBLE : View.GONE);
                        }
                        if (tvCounter != null) {
                            tvCounter.setText(finalTextoContador);
                            tvCounter.setVisibility(finalMostrarContador ? View.VISIBLE : View.GONE);
                        }
                    }

                    if (tvDiasActivos != null) {
                        tvDiasActivos.setText(String.valueOf(finalDiasActivos));
                    }
                    if (tvTareasCompletadas != null) {
                        tvTareasCompletadas.setText(String.valueOf(finalTareasCompletadas));
                    }
                    if (tvXpTotal != null) {
                        tvXpTotal.setText(String.valueOf(finalXpTotal));
                    }
                });

                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        });
    }

    //modifica datos dinámicos del card tarea.xml
    private void crearCardTarea(Tarea tarea) {
        View cardTarea = LayoutInflater.from(requireContext()).inflate(R.layout.tarea, containerTareasPendientes, false);

        ImageView ivIcono = cardTarea.findViewById(R.id.iv_icono_tarea);
        TextView tvTitulo = cardTarea.findViewById(R.id.tv_titulo_tarea);
        TextView tvFecha = cardTarea.findViewById(R.id.tv_fecha_tarea);
        TextView tvDescripcion = cardTarea.findViewById(R.id.tv_descripcion_tarea);
        TextView tvXp = cardTarea.findViewById(R.id.tv_xp_tarea);

        tvTitulo.setText(tarea.titulo);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date fechaLimite = sdf.parse(tarea.fechaLimite);
            Date ahora = new Date();

            long diffMillis = fechaLimite.getTime() - ahora.getTime();
            long horas = diffMillis / (1000 * 60 * 60);

            if (horas > 24) {
                long dias = horas / 24;
                tvFecha.setText("Vence en " + dias + " días");
            } else if (horas > 0) {
                tvFecha.setText("Vence en " + horas + " horas");
            } else {
                tvFecha.setText("Vence pronto");
            }
        } catch (Exception e) {
            tvFecha.setText("Vence: " + tarea.fechaLimite);
        }

        if (tarea.descripcion != null && !tarea.descripcion.isEmpty()) {
            tvDescripcion.setText(tarea.descripcion);
            tvDescripcion.setVisibility(View.VISIBLE);
        } else {
            tvDescripcion.setVisibility(View.GONE);
        }

        tvXp.setText("+" + tarea.xpRecompensa + " XP");

        ivIcono.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white));

        cardTarea.setOnClickListener(v -> mostrarDialogTareaCompletada(tarea));

        if (containerTareasPendientes != null) {
            containerTareasPendientes.addView(cardTarea);
        }
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

    private void completarTarea(Tarea tarea) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Marcar como completada
            tarea.completada = true;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            tarea.fechaCompletado = sdf.format(new Date());

            database.tareaDao().actualizar(tarea);

            // Log de XP ganado
            database.registroSistemaDao().logXP(tarea.xpRecompensa, tarea.titulo);

            // Verificar si es la primera tarea completada
            int totalCompletadas = database.tareaDao().contarTodasCompletadas();
            if (totalCompletadas == 1) {
                database.registroSistemaDao().logPrimeraCompletada();
            }

            // Verificar milestones
            if (totalCompletadas % 10 == 0 && totalCompletadas > 0) {
                database.registroSistemaDao().logMilestone(totalCompletadas);
            }

            // Verificar día productivo (más de 5 tareas en un día)
            String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            int tareasHoy = database.tareaDao().contarCompletadasPorFecha(hoy);
            if (tareasHoy == 5) {
                database.registroSistemaDao().logDiaProductivo(tareasHoy);
            }

            // Mensaje diferente según tipo de tarea (solo si es rutinaria)
            String mensaje = "¡Tarea completada! +" + tarea.xpRecompensa + " XP";
            if (tarea.esRutinaria) {
                if (tarea.periodicidad.equals("DIARIA")) {
                    mensaje += " · Se regenerará mañana ♻️";
                } else if (tarea.periodicidad.equals("SEMANAL")) {
                    mensaje += " · Se regenerará la próxima semana ♻️";
                }
            }

            String finalMensaje = mensaje;
            requireActivity().runOnUiThread(() -> {
                cargarTareasPendientes();
                actualizarProgresoSemanal();
                cargarRegistrosSistema();
                Toast.makeText(requireContext(), finalMensaje, Toast.LENGTH_LONG).show();
            });
        });
    }

    private void verificarPrimeraVez() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int totalRegistros = database.registroSistemaDao().getRecientes(100).size();
            if (totalRegistros == 0) {
                esPrimeraVez = true;
                database.registroSistemaDao().logBienvenida(1);
                requireActivity().runOnUiThread(() -> {
                    cargarRegistrosSistema();
                });
            }
        });
    }

    private void cargarRegistrosSistema() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<com.example.aura.data.entities.RegistroSistema> logs = database.registroSistemaDao().getRecientes(4);

            requireActivity().runOnUiThread(() -> {
                View rootView = getView();
                if (rootView == null) return;

                LinearLayout containerLogs = rootView.findViewById(R.id.container_logs_dinamicos);
                if (containerLogs == null) return;

                containerLogs.removeAllViews();

                if (logs.isEmpty()) {
                    // Mostrar mensaje de que no hay logs
                    TextView tvVacio = new TextView(requireContext());
                    tvVacio.setText("No hay actividad reciente");
                    tvVacio.setTextColor(ContextCompat.getColor(requireContext(), R.color.gris_azulado));
                    tvVacio.setGravity(Gravity.CENTER);
                    tvVacio.setPadding(0, 20, 0, 20);
                    containerLogs.addView(tvVacio);
                    return;
                }

                for (com.example.aura.data.entities.RegistroSistema log : logs) {
                    crearVistaLog(log, containerLogs);
                }
            });
        });
    }

    private void crearVistaLog(com.example.aura.data.entities.RegistroSistema log, LinearLayout container) {
        LinearLayout logRow = new LinearLayout(requireContext());
        logRow.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        logRow.setOrientation(LinearLayout.HORIZONTAL);
        logRow.setGravity(Gravity.CENTER_VERTICAL);
        int paddingVertical = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
        logRow.setPadding(0, paddingVertical, 0, paddingVertical);

        // TextView de hora
        TextView tvHora = new TextView(requireContext());
        tvHora.setLayoutParams(new LinearLayout.LayoutParams(
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()),
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tvHora.setText(com.example.aura.data.utils.FechaUtils.formatearParaLog(log.fechaHora));
        tvHora.setTextColor(ContextCompat.getColor(requireContext(), R.color.gris_azulado));
        tvHora.setTypeface(null, android.graphics.Typeface.BOLD);
        tvHora.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        logRow.addView(tvHora);

        // TextView badge con tipo
        TextView tvBadge = new TextView(requireContext());
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        badgeParams.setMarginEnd((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        tvBadge.setLayoutParams(badgeParams);
        tvBadge.setText(log.tipo);
        tvBadge.setTextColor(getColorForBadge(log.tipo));
        tvBadge.setTypeface(null, android.graphics.Typeface.BOLD);
        tvBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
        int paddingH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
        int paddingV = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        tvBadge.setPadding(paddingH, paddingV, paddingH, paddingV);

        int drawableId = getDrawableIdFromName(log.iconoDrawable);
        if (drawableId != 0) {
            tvBadge.setBackgroundResource(drawableId);
        }
        logRow.addView(tvBadge);

        // TextView mensaje
        TextView tvMensaje = new TextView(requireContext());
        tvMensaje.setLayoutParams(new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ));
        tvMensaje.setText(log.mensaje);
        tvMensaje.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_sobre_azules));
        tvMensaje.setTypeface(null, android.graphics.Typeface.BOLD);
        tvMensaje.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        logRow.addView(tvMensaje);

        container.addView(logRow);

        // Agregar separador si no es el último
        View separator = new View(requireContext());
        separator.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, getResources().getDisplayMetrics())
        ));
        separator.setBackgroundColor(Color.parseColor("#1a0a30"));
        container.addView(separator);
    }

    private int getColorForBadge(String tipo) {
        switch (tipo) {
            case "NIVEL":
                return Color.WHITE;
            case "LOGRO":
                return Color.parseColor("#0a0a0a");
            case "XP":
                return Color.parseColor("#0a0a0a");
            case "RACHA":
                return Color.parseColor("#0a0a0a");
            case "SISTEMA":
                return Color.WHITE;
            case "ADVERTENCIA":
                return Color.parseColor("#0a0a0a");
            default:
                return Color.WHITE;
        }
    }

    private int getDrawableIdFromName(String drawableName) {
        if (drawableName == null || drawableName.isEmpty()) {
            return R.drawable.bg_log_badge_xp;
        }
        try {
            return getResources().getIdentifier(drawableName, "drawable", requireContext().getPackageName());
        } catch (Exception e) {
            return R.drawable.bg_log_badge_xp;
        }
    }

    // Método para regenerar tareas DIARIAS y SEMANALES RUTINARIAS automáticamente
    // Solo regenera tareas que tengan esRutinaria = true
    private void regenerarTareasRecurrentes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat sdfFecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String hoy = sdfFecha.format(new Date());
            Calendar cal = Calendar.getInstance();

            // Obtener solo las plantillas RUTINARIAS (esRutinaria = true)
            List<Tarea> plantillas = database.tareaDao().obtenerPlantillas();

            for (Tarea plantilla : plantillas) {
                // Verificar si ya existe una instancia para hoy
                int yaExiste = database.tareaDao().existeInstanciaParaFecha((int) plantilla.id, hoy);

                if (yaExiste == 0) {
                    // No existe instancia para hoy, crear una nueva
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

                    // Establecer referencia a la plantilla
                    nuevaInstancia.idTareaPlantilla = (int) plantilla.id;

                    // Calcular fecha límite según tipo
                    if (plantilla.periodicidad.equals("DIARIA")) {
                        // Fecha límite: hoy a las 23:59:59
                        cal.set(Calendar.HOUR_OF_DAY, 23);
                        cal.set(Calendar.MINUTE, 59);
                        cal.set(Calendar.SECOND, 59);
                        nuevaInstancia.fechaLimite = sdf.format(cal.getTime());
                    } else if (plantilla.periodicidad.equals("SEMANAL")) {
                        // Fecha límite: domingo de esta semana a las 23:59:59
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

                    // Insertar la nueva instancia
                    database.tareaDao().insertar(nuevaInstancia);
                }
            }
        });
    }

}