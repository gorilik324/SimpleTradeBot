package ru.rexchange.tools;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class PeriodicalTask implements Runnable {
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private long period = 60;// период запуска в секундах

	public PeriodicalTask() {
	}

	public PeriodicalTask(long period) {
		this.period = period;
	}

	public abstract void run();

	public void start() {
		scheduler.scheduleAtFixedRate(this, 0, period, TimeUnit.SECONDS);
		try {
			scheduler.awaitTermination(60, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO: handle exception
		}
	}
}
