package com.example.aura.data.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class FechaUtils {

    public static String hoy() {
        return LocalDate.now().toString();
    }

    public static String ahora() {
        return LocalDateTime.now().toString();
    }

    public static String semanaActual() {
        LocalDate hoy = LocalDate.now();
        int year = hoy.getYear();
        int semana = hoy.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        return String.format(Locale.US, "%04d-W%02d", year, semana);
    }

    public static String mesActual() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    public static String haceNDias(int dias) {
        return LocalDate.now().minusDays(dias).toString();
    }

    public static String haceNMeses(int meses) {
        return LocalDate.now().minusMonths(meses).toString();
    }

    public static String formatearParaLog(String isoFechaHora) {try {
        LocalDateTime fechaLog = LocalDateTime.parse(isoFechaHora);
        LocalDateTime ahora = LocalDateTime.now();

        if (fechaLog.toLocalDate().isEqual(ahora.toLocalDate())) {
            return fechaLog.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (fechaLog.toLocalDate().isEqual(ahora.minusDays(1).toLocalDate())) {
            return "Ayer";
        } else {
            return fechaLog.format(DateTimeFormatter.ofPattern("dd MMM"));
        }
    } catch (Exception e) {
        return "---";
    }
    }
}
