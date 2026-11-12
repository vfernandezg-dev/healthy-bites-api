package com.healthybites;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor;

/**
 * En los tests, {@code @Async} se ejecuta en el mismo hilo: los correos que se envían "en segundo plano" ya se han
 * enviado cuando la petición termina, y ningún hilo suelto toca un mock que el siguiente test ya ha reiniciado.
 */
@TestConfiguration(proxyBeanMethods = false)
public class SynchronousAsyncConfiguration {

    @Bean(name = AsyncAnnotationBeanPostProcessor.DEFAULT_TASK_EXECUTOR_BEAN_NAME)
    TaskExecutor taskExecutor() {
        return new SyncTaskExecutor();
    }
}
