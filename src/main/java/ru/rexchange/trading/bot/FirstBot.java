package ru.rexchange.trading.bot;

import org.apache.log4j.Logger;

import ru.rexchange.data.BollingerBandsCalculator;
import ru.rexchange.data.BollingerBandsCalculator.BollBand;
import ru.rexchange.data.model.AbstractModel;
import ru.rexchange.data.storage.RateValue;
import ru.rexchange.trading.AbstractTrader;
import ru.rexchange.trading.FakeTrader;

public class FirstBot extends AbstractBot {
	protected static Logger LOGGER = Logger.getLogger(FirstBot.class);
	protected AbstractModel model = null;
	protected AbstractTrader trader = null;
	protected float dealRate = 0f;
	protected int state = States.IDLE;
	protected int direction = Directions.BUY_ONLY;

	private static interface States {
		public static int IDLE = 0;// Ожидание открытия сделки
		public static int READY_TO_BUY = 1;// Готовность открытия сделки на покупку
		public static int READY_TO_SELL = 2;// Готовность открытия сделки на продажу
		public static int WAITING_TO_BUY = 3;// Готовность закрытия сделки на продажу
		public static int WAITING_TO_SELL = 4;// Готовность закрытия сделки на покупку
	}
	
	public FirstBot(AbstractModel model) {
		this.model = model;
		this.trader = new FakeTrader(AbstractTrader.AmountDeterminant.FIXED, 20);
	}
	
	public FirstBot(AbstractModel model, int tradeDirection) {
		this.model = model;
		this.trader = new FakeTrader(AbstractTrader.AmountDeterminant.FIXED, 20);
		this.direction = tradeDirection;
	}

	public FirstBot(AbstractModel model, AbstractTrader trader, int tradeDirection) {
		this.model = model;
		this.trader = trader;
		this.direction = tradeDirection;
	}

	public boolean isIdle() {
		return state == States.IDLE;
	}

	public void run() {
		RateValue newValue = model.increment();
		BollBand band = BollingerBandsCalculator.evaluateBand(model.getRates(), 1.1f);
		if (!band.within(newValue.getRate())) {
			switch (state) {
			case States.IDLE:
				if (band.below(newValue.getRate())) {
					if (ableToBuy()) {
						changeState(States.READY_TO_BUY);
					}
				} else if (band.over(newValue.getRate())) {
					if (ableToSell()) {
						changeState(States.READY_TO_SELL);
					}
				}
				break;
			case States.WAITING_TO_BUY:
				if (band.below(newValue.getRate()) && newValue.getRate() < dealRate) {
					closeSell(newValue.getRate());
					changeState(States.IDLE);
				}
				break;
			case States.WAITING_TO_SELL:
				if (band.over(newValue.getRate()) && newValue.getRate() > dealRate) {
					closeBuy(newValue.getRate());
					changeState(States.IDLE);
				}
				break;
			}
		} else {
			switch (state) {
			case States.READY_TO_BUY:
				dealRate = newValue.getRate();
				buy(dealRate);
				changeState(States.WAITING_TO_SELL);
				break;
			case States.READY_TO_SELL:
				dealRate = newValue.getRate();
				sell(dealRate);
				changeState(States.WAITING_TO_BUY);
				break;
			}
		}
	}
	
	public boolean buy(float dealRate) {
		LOGGER.debug(String.format("Opening buy deal. Rate: %s", dealRate));
		boolean result = trader.openBuy(dealRate);
		LOGGER.debug(String.format("Balance: %s", trader.getBalance()));
		return result;
	}

	public boolean sell(float dealRate) {
		LOGGER.debug(String.format("Opening sell deal. Rate: %s", dealRate));
		boolean result = trader.openSell(dealRate);
		LOGGER.debug(String.format("Balance: %s", trader.getBalance()));
		return result;
	}

	public boolean closeBuy(float dealRate) {
		LOGGER.debug(String.format("Closing buy deal. Rate: %s", dealRate));
		boolean result = trader.closeBuy(dealRate);
		LOGGER.debug(String.format("Balance: %s", trader.getBalance()));
		return result;
	}

	public boolean closeSell(float dealRate) {
		LOGGER.debug(String.format("Closing sell deal. Rate: %s", dealRate));
		boolean result = trader.closeSell(dealRate);
		LOGGER.debug(String.format("Balance: %s", trader.getBalance()));
		return result;
	}

	public void changeState(int newState) {
		LOGGER.debug(String.format("Bot state change. Old state %s, new state %s ", state, newState));
		state = newState;
	}

	public boolean ableToBuy() {
		return direction >= Directions.BUY_AND_SELL;
	}

	public boolean ableToSell() {
		return direction <= Directions.BUY_AND_SELL;
	}
}
