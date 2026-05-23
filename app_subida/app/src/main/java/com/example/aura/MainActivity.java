package com.example.aura;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.aura.data.AppDatabase;
import com.example.aura.data.entities.Tarea;
import com.example.aura.data.entities.Usuario;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private HomeFragment homeFragment;
    private TareasFragment tareasFragment;
    private EntrenoFragment entrenoFragment;
    private PerfilFragment perfilFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        // Inicializar fragments
        homeFragment = new HomeFragment();
        tareasFragment = new TareasFragment();
        entrenoFragment = new EntrenoFragment();
        perfilFragment = new PerfilFragment();
        
        if (savedInstanceState == null) {
            replaceFragment(homeFragment);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                replaceFragment(homeFragment);
                return true;
            } else if (id == R.id.nav_tareas) {
                replaceFragment(tareasFragment);
                return true;
            } else if (id == R.id.nav_entreno) {
                replaceFragment(entrenoFragment);
                return true;
            } else if (id == R.id.nav_perfil) {
                replaceFragment(perfilFragment);
                return true;
            }
            return false;
        });

        inicializarUsuarioSiNoExiste();
        verificarNombreUsuario();
        regenerarTareasSiEsNuevoDia();
    }

    private void inicializarUsuarioSiNoExiste() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Usuario usuario = db.usuarioDao().getUsuario();
            if (usuario == null) {
                db.usuarioDao().insertar(new Usuario());
            }
        });
    }

    private void verificarNombreUsuario() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Usuario usuario = db.usuarioDao().getUsuario();

            if (usuario == null) {
                db.usuarioDao().insertar(new Usuario());
                usuario = db.usuarioDao().getUsuario();
            }

            if (usuario != null && (usuario.nombre == null || usuario.nombre.isEmpty())) {
                runOnUiThread(() -> mostrarDialogoNombre());
            }
        });
    }

    private void mostrarDialogoNombre() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nombre_usuario, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etNombre = dialogView.findViewById(R.id.et_nombre_usuario);
        Button btnAceptar = dialogView.findViewById(R.id.btn_aceptar_nombre);

        btnAceptar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (!nombre.isEmpty()) {
                guardarNombreUsuario(nombre);
                dialog.dismiss();
            } else {
                etNombre.setError("El nombre es obligatorio");
            }
        });

        dialog.show();
    }

    private void guardarNombreUsuario(String nombre) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Usuario usuario = db.usuarioDao().getUsuario();

            if (usuario != null) {
                usuario.nombre = nombre;
                db.usuarioDao().actualizar(usuario);
            }

            SharedPreferences prefs = getSharedPreferences("aura_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("nombre_preguntado", true).apply();

            runOnUiThread(() -> {
                if (homeFragment != null) {
                    homeFragment.cargarDatosUsuario();
                }
            });
        });
    }

    private void regenerarTareasSiEsNuevoDia() {
        SharedPreferences prefs = getSharedPreferences("aura_prefs", MODE_PRIVATE);
        SimpleDateFormat sdfFecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String hoy = sdfFecha.format(new Date());
        String ultimaRegeneracion = prefs.getString("ultima_regeneracion", "");

        if (!hoy.equals(ultimaRegeneracion)) {
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase database = AppDatabase.getInstance(this);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
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
                            cal.set(Calendar.HOUR_OF_DAY, 23);
                            cal.set(Calendar.MINUTE, 59);
                            cal.set(Calendar.SECOND, 59);
                            nuevaInstancia.fechaLimite = sdf.format(cal.getTime());
                        } else if (plantilla.periodicidad.equals("SEMANAL")) {
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

                prefs.edit().putString("ultima_regeneracion", hoy).apply();
            });
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}