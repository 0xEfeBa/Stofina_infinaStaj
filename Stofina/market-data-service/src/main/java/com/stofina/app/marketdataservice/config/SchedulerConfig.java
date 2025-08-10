package com.stofina.app.marketdataservice.config;

import com.stofina.app.marketdataservice.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);

    @Bean
    public TaskScheduler taskScheduler() {
        logger.info("Configuring TaskScheduler with pool size: {}", Constants.Scheduler.THREAD_POOL_SIZE);
        
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Constants.Scheduler.THREAD_POOL_SIZE);
        scheduler.setThreadNamePrefix(Constants.Scheduler.THREAD_NAME_PREFIX);
        scheduler.setDaemon(false);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        scheduler.initialize();
        
        logger.info("TaskScheduler configured successfully");
        return scheduler;
    }

   
}