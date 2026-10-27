package my.portfolio.port_view.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "dailyBatchTaskExecutor")
    public TaskExecutor dailyBatchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        /*
         * Daily Batch is primarily a local production pipeline.
         * There is no reason to run multiple instances concurrently, and the DB also prevents
         * duplicate RUNNING executions, so the Thread Pool is limited to one thread.
         */
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("daily-batch-");

        executor.initialize();
        return executor;
    }
}