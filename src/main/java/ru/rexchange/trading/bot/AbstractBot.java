package ru.rexchange.trading.bot;

public abstract class AbstractBot implements Runnable {
	protected String name = null;
	public static interface Directions {
		public static int SELL_ONLY = -1;
		public static int BUY_AND_SELL = 0;
		public static int BUY_ONLY = 1;
	}

	public AbstractBot() {
		super();
	}

	public abstract boolean isIdle();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
