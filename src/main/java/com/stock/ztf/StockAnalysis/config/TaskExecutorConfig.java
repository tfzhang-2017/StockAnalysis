package com.stock.ztf.StockAnalysis.config;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class TaskExecutorConfig extends AsyncConfigurerSupport {

	private final static Logger logger = LoggerFactory.getLogger(TaskExecutorConfig.class);

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(20);
		taskExecutor.setMaxPoolSize(50);
		taskExecutor.setQueueCapacity(25);
		taskExecutor.setWaitForTasksToCompleteOnShutdown(true);  
		taskExecutor.setAwaitTerminationSeconds(60 * 15);
		taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());  
		taskExecutor.initialize();
		return taskExecutor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new MyAsyncExceptionHandler();
	}

	/**
	 * 自定义异常处理类
	 * 
	 * @author hry
	 *
	 */
	class MyAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

		// 手动处理捕获的异常
		@Override
		public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
			logger.error("MyAsyncException message - " + throwable.getMessage());
			logger.error("MyAsyncMethod name - " + method.getName());
			for (Object param : obj) {
				logger.error("MyAsyncParameter value - " + param);
			}
		}

	}

}
