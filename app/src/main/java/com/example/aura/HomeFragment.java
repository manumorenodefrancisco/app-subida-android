package com.example.aura;

import android.app.Dialog;
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
    private TextView tvContadorTareas;
    private TextView tvDiasActivos;
    private TextView tvTareasCompletadas;
    private TextView tvXpTotal;

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
        Button btnCrearTarea = rootView.findViewById(R.id.btn_crear_tarea_home);

        btnCrearTarea.setOnClickListener(v -> mostrarDialogCrearTarea());
        cargarTareasPendientes();
        actualizarProgresoSemanal();

        return rootView;
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

        View.OnClickListener actualizarVisibilidadDias = v -> {
            boolean esSemanal = rbSemanal.isChecked();
            boolean esRutinaria = switchRutinaria.isChecked();
            containerDiasPersonalizados.setVisibility(
                (esSemanal && esRutinaria) ? View.VISIBLE : View.GONE //si escoge SEMANAL y rutinaria se muestran los 7d de la semana.
            );
        };

        rgPeriodicidad.setOnCheckedChangeListener((group, checkedId) -> actualizarVisibilidadDias.onClick(null));
        switchRutinaria.setOnCheckedChangeListener((buttonView, isChecked) -> actualizarVisibilidadDias.onClick(null));

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
            Toast.makeText(requireContext(), "Tarea creada con éxito", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    //guarda una tarea en la base de datos en un hilo secundario y luego recarga las tareas pendientes en el UI
    private void guardarTarea(Tarea tarea) {
        Executors.newSingleThreadExecutor().execute(() -> {
            database.tareaDao().insertar(tarea);

            requireActivity().runOnUiThread(() -> {
                cargarTareasPendientes();
                actualizarProgresoSemanal();
            });
        });
    }

    private void cargarTareasPendientes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            String fechaActual = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());

            int totalVigentes = database.tareaDao().contarVigentes(fechaActual);
            int completadasVigentes = database.tareaDao().contarCompletadasVigentes(fechaActual);

            //solo 2 para evitar tener que crear un recyclerview
            List<Tarea> top2Pendientes = database.tareaDao().getTop2Pendientes(fechaActual);

            requireActivity().runOnUiThread(() -> {
                if (tvContadorTareas != null) {
                    tvContadorTareas.setText(completadasVigentes + "/" + totalVigentes + " completadas");
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
            tarea.completada = true;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            tarea.fechaCompletado = sdf.format(new Date());
            
            database.tareaDao().actualizar(tarea);

            requireActivity().runOnUiThread(() -> {
                cargarTareasPendientes();
                actualizarProgresoSemanal();
                Toast.makeText(requireContext(), "¡Tarea completada! +" + tarea.xpRecompensa + " XP", Toast.LENGTH_SHORT).show();
            });
        });
    }

}