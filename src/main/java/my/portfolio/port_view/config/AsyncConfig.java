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
         * Daily Batch는 로컬 운영 파이프라인 성격이 강함.
         * 동시에 여러 개 돌릴 이유가 없고, DB에서도 RUNNING 중복 실행을 막고 있으므로
         * Thread Pool도 1개로 제한한다.
         */
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("daily-batch-");

        executor.initialize();
        return executor;
    }
}