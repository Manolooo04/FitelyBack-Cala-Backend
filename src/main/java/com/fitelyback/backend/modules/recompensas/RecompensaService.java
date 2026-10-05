package com.fitelyback.backend.modules.recompensas;

import com.fitelyback.backend.exception.ApiException;
import com.fitelyback.backend.modules.recompensas.dto.RecompensaPublicaResponse;
import com.fitelyback.backend.modules.tarjetas.TarjetaEmitida;
import com.fitelyback.backend.modules.tarjetas.TarjetaEmitidaRepository;
import com.fitelyback.backend.modules.tarjetas.TarjetaEmitidaService;
import com.fitelyback.backend.modules.tarjetas.dto.TarjetaPublicaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecompensaService {

    private static final String PREFIJO_CODIGO = "R-";

    private final RecompensaRepository recompensaRepository;
    private final TarjetaEmitidaRepository tarjetaRepository;
    private final TarjetaEmitidaService tarjetaEmitidaService;

    // Se llama cada vez que cambian las estampillas de una tarjeta:
    // crea la recompensa al completarla y la anula si deja de estar completa.
    @Transactional
    public void sincronizar(TarjetaEmitida tarjeta) {
        Integer meta = tarjeta.getPlantilla().getMetaSellos();
        if (meta == null) {
            return;
        }

        boolean completa = tarjeta.getSellos() >= meta;
        Optional<Recompensa> existente = recompensaRepository.findByTarjetaId(tarjeta.getId());

        if (completa && existente.isEmpty()) {
            recompensaRepository.save(Recompensa.builder()
                    .codigo(PREFIJO_CODIGO + UUID.randomUUID())
                    .estado(EstadoRecompensa.PENDIENTE)
                    .descripcion(tarjeta.getPlantilla().getRecompensa())
                    .tarjeta(tarjeta)
                    .negocio(tarjeta.getNegocio())
                    .build());
        } else if (!completa && existente.isPresent()
                && existente.get().getEstado() == EstadoRecompensa.PENDIENTE) {
            recompensaRepository.delete(existente.get());
        }
    }

    // Botón "Canjear" en la página del cliente: devuelve el código para mostrar el QR de recompensa
    @Transactional(readOnly = true)
    public RecompensaPublicaResponse pendienteDeTarjeta(String codigoTarjeta) {
        Recompensa recompensa = recompensaDe(codigoTarjeta)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Esta tarjeta aún no tiene una recompensa disponible"));

        if (recompensa.getEstado() == EstadoRecompensa.CANJEADA) {
            throw new ApiException(HttpStatus.CONFLICT, "La recompensa de esta tarjeta ya fue canjeada");
        }
        return new RecompensaPublicaResponse(recompensa.getCodigo(), recompensa.getDescripcion());
    }

    // Botón "Mostrar siguiente QR": la tarjeta nueva que recibió el cliente al canjear
    @Transactional(readOnly = true)
    public TarjetaPublicaResponse siguienteDeTarjeta(String codigoTarjeta) {
        Recompensa recompensa = recompensaDe(codigoTarjeta)
                .filter(r -> r.getEstado() == EstadoRecompensa.CANJEADA && r.getTarjetaSiguiente() != null)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "La siguiente tarjeta estará disponible cuando canjees tu recompensa"));

        return tarjetaEmitidaService.verPorCodigo(recompensa.getTarjetaSiguiente().getCodigo());
    }

    private Optional<Recompensa> recompensaDe(String codigoTarjeta) {
        TarjetaEmitida tarjeta = tarjetaRepository.findByCodigo(codigoTarjeta)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tarjeta no encontrada"));
        return recompensaRepository.findByTarjetaId(tarjeta.getId());
    }
}