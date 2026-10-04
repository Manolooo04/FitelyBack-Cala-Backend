package com.fitelyback.backend.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TareasMantenimientoScheduler {

    @Scheduled(cron = "0 0 0 * * ?")
    public void ejecutarLimpiezaTokens() {
        // Tarea programada en segundo plano
    }
}