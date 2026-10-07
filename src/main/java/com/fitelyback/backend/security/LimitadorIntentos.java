package com.fitelyback.backend.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Lleva la cuenta de intentos por clave (IP o usuario) y decide cuándo bloquear
@Component
public class LimitadorIntentos {

    private record Registro(int intentos, Instant inicioVentana, Instant bloqueadoHasta) {}

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();

    // Segundos que faltan para que termine el bloqueo; 0 si puede continuar
    public long segundosBloqueado(String clave) {
        Registro registro = registros.get(clave);
        if (registro == null || registro.bloqueadoHasta() == null) {
            return 0;
        }
        return Math.max(Duration.between(Instant.now(), registro.bloqueadoHasta()).getSeconds(), 0);
    }

    // Suma un intento; al llegar al máximo dentro de la ventana, bloquea durante ese mismo tiempo
    public void registrarIntento(String clave, int maximo, Duration ventana) {
        Instant ahora = Instant.now();
        registros.compute(clave, (k, actual) -> {
            boolean reiniciar = actual == null
                    || actual.inicioVentana().plus(ventana).isBefore(ahora)
                    || (actual.bloqueadoHasta() != null && actual.bloqueadoHasta().isBefore(ahora));

            int intentos = reiniciar ? 1 : actual.intentos() + 1;
            Instant inicio = reiniciar ? ahora : actual.inicioVentana();
            Instant bloqueadoHasta = intentos >= maximo ? ahora.plus(ventana) : null;

            return new Registro(intentos, inicio, bloqueadoHasta);
        });
    }

    public void limpiar(String clave) {
        registros.remove(clave);
    }

    // Cada hora descarta los registros antiguos para no acumular memoria
    @Scheduled(fixedRate = 3_600_000)
    public void depurar() {
        Instant ahora = Instant.now();
        Instant limite = ahora.minus(Duration.ofHours(2));
        registros.values().removeIf(r -> r.inicioVentana().isBefore(limite)
                && (r.bloqueadoHasta() == null || r.bloqueadoHasta().isBefore(ahora)));
    }
}