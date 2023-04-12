package ru.rexchange.trading;

import org.apache.log4j.Logger;

public abstract class AbstractTrader {
	protected static Logger LOGGER = Logger.getLogger(AbstractTrader.class);
	public static final String DEFAULT_TRADER = "default";

	protected String name = DEFAULT_TRADER;
	// todo: задавать валютную пару
	// todo: суммы сделки вынести в отдельную структуру (возможно, нужно сделать
	// что-то вроде синглтона на каждую биржу)
	protected float baseCurrencyAmount = 0f;
	protected float quotedCurrencyAmount = 0f;
	protected float openedDealAmount = 0f;
	protected int amountDeterminant = AmountDeterminant.PERCENT_QUOTED;
	protected float amountValue = 10;
	protected float dealRate = 0f;
	protected int state = States.IDLE;

	public static interface AmountDeterminant {
		public static int FIXED = 1;
		public static int PERCENT = 2;
		public static int FIXED_QOUTED = 3;
		public static int PERCENT_QUOTED = 4;
	}

	public static interface States {
		public static final int NOT_ACTIVE = -1;// Неактивен
		public static int IDLE = 0;// Ожидание открытия сделки
		public static int READY_TO_BUY = 1;// Готовность открытия сделки на покупку
		public static int READY_TO_SELL = 2;// Готовность открытия сделки на продажу
		public static int BOUGHT = 3;// Сделка на покупку открыта
		public static int SOLD = 4;// Сделка на продажу открыта
		public static int WAITING_TO_SELL = 5;// Готовность закрытия сделки на покупку
		public static int WAITING_TO_BUY = 6;// Готовность закрытия сделки на продажу
	}

	public static final String getStateDescription(int state) {
		switch (state) {
		case States.NOT_ACTIVE:
			return "inactive";
		case States.IDLE:
			return "idle";
		case States.READY_TO_BUY:
			return "ready to buy";
		case States.READY_TO_SELL:
			return "ready to sell";
		case States.BOUGHT:
			return "bought";
		case States.SOLD:
			return "sold";
		case States.WAITING_TO_SELL:
			return "waiting to sell";
		case States.WAITING_TO_BUY:
			return "waiting to buy";
		default:
			return "unknown";
		}
	}

	public AbstractTrader(String name, int amountDeterminant, int amountValue) {
		if (amountDeterminant < AmountDeterminant.FIXED || amountDeterminant > AmountDeterminant.PERCENT_QUOTED) {
			throw new IllegalArgumentException("Некорректный тип определения суммы сделки");
		}
		this.amountDeterminant = amountDeterminant;
		this.amountValue = amountValue;
		this.name = name;
		requestCurrenciesAmount();
	}

	public void setCurrenciesLimit(float baseCurrency, float quotedCurrency) {
		baseCurrencyAmount = baseCurrency;
		quotedCurrencyAmount = quotedCurrency;
	}

	public abstract void requestCurrenciesAmount();

	public boolean openBuy(float desiredRate) {
		return openBuy(desiredRate, null);
	}

	public abstract boolean openBuy(float desiredRate, DealInfo info);

	public abstract boolean preOpenBuy(float desiredRate, DealInfo info);

	public boolean openSell(float desiredRate) {
		return openSell(desiredRate, null);
	}

	public abstract boolean openSell(float desiredRate, DealInfo info);

	public abstract boolean preOpenSell(float desiredRate, DealInfo info);

	public boolean closeBuy(float desiredRate) {
		return closeBuy(desiredRate, null);
	}

	public abstract boolean closeBuy(float desiredRate, DealInfo info);

	public boolean closeSell(float desiredRate) {
		return closeSell(desiredRate, null);
	}

	public abstract boolean closeSell(float desiredRate, DealInfo info);

	public abstract void configureDefault();

	public abstract void customRuntimeReconfig();

	protected float getDealAmount(float rate) {
		if (amountDeterminant == AmountDeterminant.PERCENT) {
			return (baseCurrencyAmount * amountValue) / 100;
		} else if (amountDeterminant == AmountDeterminant.FIXED) {
			return amountValue;
		} else if (amountDeterminant == AmountDeterminant.FIXED_QOUTED) {
			return TradeTools.inverse(rate, amountValue);
		} else {
			return TradeTools.inverse(rate, (quotedCurrencyAmount * amountValue) / 100);
		}
	}

	public String getBalance() {
		return String.format("Base cur: %s. Quoted cur: %s. Deal volume %s", baseCurrencyAmount,
				quotedCurrencyAmount, openedDealAmount);
	}

	public void changeState(int newState) {
		LOGGER.info(String.format("Trader <%s> state change. <%s> => <%s>", name,
				getStateDescription(state),
				getStateDescription(newState)));
		state = newState;
	}

	public int getState() {
		return state;
	}

	public void setInactive() {
		state = States.NOT_ACTIVE;
	}

	public boolean isActive() {
		return getState() != States.NOT_ACTIVE;
	}

	public void setActive() {
		state = States.IDLE;
	}

	public String getName() {
		return name;
	}

	public float getDealRate() {
		return dealRate;
	}

	public void setDealRate(float dealRate) {
		this.dealRate = dealRate;
	}

	public static interface DealInfo {
	}

	public static class TrendInfo implements DealInfo {
		Integer trend = 0;

		public TrendInfo(int trend) {
			this.trend = trend;
		}

		public Integer getTrend() {
			return trend;
		}
	}

	public static class TrendAndFileInfo extends TrendInfo {
		String filePath = null;

		public TrendAndFileInfo(int trend, String path) {
			super(trend);
			filePath = path;
		}

		public String getFilePath() {
			return filePath;
		}
	}
}
