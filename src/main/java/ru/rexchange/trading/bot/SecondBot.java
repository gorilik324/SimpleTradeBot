package ru.rexchange.trading.bot;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ru.rexchange.data.BollingerBandsCalculator;
import ru.rexchange.data.BollingerBandsCalculator.BollBand;
import ru.rexchange.data.StatisticCalculator;
import ru.rexchange.data.model.AbstractModel;
import ru.rexchange.data.model.LimitedQueue;
import ru.rexchange.data.storage.RateSequence;
import ru.rexchange.data.storage.RateValue;
import ru.rexchange.drawing.SeriesDrawer;
import ru.rexchange.trading.AbstractTrader;
import ru.rexchange.trading.AbstractTrader.States;

public class SecondBot extends AbstractBot {
	protected static Logger LOGGER = Logger.getLogger(SecondBot.class);
	protected static final String TMP_FILE_NAME = "graphics.png";
	protected AbstractModel model = null;
	protected LimitedQueue<BollBand> lastBands = new LimitedQueue<>(100);
	protected boolean bought = false, sold = false;
	protected float bandWidth = 2.5f;
	protected int avgLength = 20;

	protected Map<String, AbstractTrader> traders = new HashMap<>();
	protected List<String> traderNames = new ArrayList<>();

	protected int direction = Directions.BUY_ONLY;

	public static interface Directions {
		public static int SELL_ONLY = -1;
		public static int BUY_AND_SELL = 0;
		public static int BUY_ONLY = 1;
	}

	public SecondBot(AbstractModel model) {
		setModel(model);
		direction = Directions.BUY_AND_SELL;
	}

	public SecondBot(AbstractModel model, int tradeDirection) {
		setModel(model);
		this.direction = tradeDirection;
	}

	public SecondBot(AbstractModel model, AbstractTrader trader, int tradeDirection) {
		setModel(model);
		traders.put(AbstractTrader.DEFAULT_TRADER, trader);
		this.direction = tradeDirection;
	}

	public boolean isIdle() {
		for (String trader : traders.keySet()) {
			if (getTrader(trader).getState() != States.IDLE)
				return false;
		}
		return true;
	}

	public void run() {
		RateValue newValue = model.increment();
		if (newValue == null) {
			LOGGER.warn("Couldn't got new value. Skipping iteration...");
			return;
		}
		float lastRate = newValue.getRate();
		// todo адаптивное изменение ширины канала?
		RateSequence rates = model.getRates();
		BollBand band = BollingerBandsCalculator.evaluateBand(rates, bandWidth,
				avgLength);
		lastBands.addToQueue(band);
		int ratesLength = rates.size();
		float ma1 = StatisticCalculator.getFirstAverageAdaptive(rates.getRates(),
				(int) (ratesLength * 0.6f)),
				ma2 = StatisticCalculator.getLastAverageAdaptive(rates.getRates(),
						(int) (ratesLength * 0.6f));
		int trend = Float.compare(ma2, ma1);
		LOGGER.debug(String.format("Bot activated. Band <%s-%s>. Value <%s>. Trend <%s>",
				band.getLower(),
				band.getUpper(), 
				lastRate, trend == 0 ? "flat" : trend > 0 ? "up" : "down"));

		//TODO странные простановка и снятие флагов КУплено/Продано
		for (String trader : traderNames) {
			if (!band.within(lastRate)) {
				switch (getTrader(trader).getState()) {
				case States.IDLE:
					if (band.below(lastRate)) {
						notifyLow(trader, lastRate, trend, createGraphicAttachment(trader));
						if (ableToBuy()) {
							getTrader(trader).changeState(States.READY_TO_BUY);
							setBought();
						}
					} else if (band.over(lastRate)) {
						notifyHigh(trader, lastRate, trend, createGraphicAttachment(trader));
						if (ableToSell()) {
							getTrader(trader).changeState(States.READY_TO_SELL);
							setSold();
						}
					}
					break;
				case States.BOUGHT:
					if (band.over(lastRate)) {
						getTrader(trader).changeState(States.WAITING_TO_SELL);
					}
					break;
				case States.SOLD:
					if (band.below(lastRate)) {
						getTrader(trader).changeState(States.WAITING_TO_BUY);
					}
					break;
				}
			} else {
				// todo: не забыть сделать принудительное закрытие при движении не туда
				switch (getTrader(trader).getState()) {
				case States.READY_TO_BUY:
					if (buy(trader, lastRate, trend, createGraphicAttachment(trader))) {
						getTrader(trader).changeState(States.BOUGHT);
					} else {
						getTrader(trader).changeState(States.IDLE);
					}
					resetBougthFlag();
					break;
				case States.READY_TO_SELL:
					if (sell(trader, lastRate, trend, createGraphicAttachment(trader))) {
						getTrader(trader).changeState(States.SOLD);
					} else {
						getTrader(trader).changeState(States.IDLE);
					}
					resetSoldFlag();
					break;
				case States.WAITING_TO_BUY:
					if (lastRate < getTrader(trader).getDealRate()) {
						closeSell(trader, lastRate, trend);
						getTrader(trader).changeState(States.IDLE);
					} else {
						getTrader(trader).changeState(States.SOLD);// Ждём следующего момента
					}
					break;
				case States.WAITING_TO_SELL:
					if (lastRate > getTrader(trader).getDealRate()) {
						closeBuy(trader, lastRate, trend);
						getTrader(trader).changeState(States.IDLE);
					} else {
						getTrader(trader).changeState(States.BOUGHT);// Ждём следующего момента
					}
					break;
				}
			}
		}
	}

	private String createGraphicAttachment(String traderName) {
		String picFolder = String.format("%s/%s", ".pics", traderName);
		File dir = new File(picFolder);
		if (!dir.exists() && !dir.mkdirs()) {
			LOGGER.error(String.format("Couldn't create path ./%s", picFolder));
		}
		String resultFile = String.format("%s/%s", picFolder, TMP_FILE_NAME);
		try (OutputStream os = new FileOutputStream(resultFile)) {
			createGraphics(os, traderName);
		} catch (Exception e) {
			LOGGER.error(e);
			return null;
		}
		return resultFile;
	}

	protected void createGraphics(OutputStream os, String caption) throws IOException {
		List<Float> seq = model.getRates().getRates();
		List<Float> lowerSeq = new LinkedList<>();
		List<Float> upperSeq = new LinkedList<>();
		for (BollBand band : lastBands) {
			lowerSeq.add(band.getLower());
			upperSeq.add(band.getUpper());
		}
		List<Float> finalSeq = seq.subList(Math.max(seq.size() - lastBands.size(), 0), seq.size());

		SeriesDrawer d = new SeriesDrawer(640, 480, 20);
		d.addSerie("rates", finalSeq, Color.BLACK);
		d.addSerie("lowBand", lowerSeq, Color.BLUE);
		d.addSerie("upBand", upperSeq, Color.BLUE);
		d.setCaption(caption);
		d.setStart(model.getRates().first().getTime());
		d.setEnd(model.getRates().last().getTime());
		d.drawSeries(os);
	}

	public boolean buy(float dealRate) {
		return buy(AbstractTrader.DEFAULT_TRADER, dealRate, null, null);
	}

	public boolean buy(String traderName, float dealRate, Integer trend, String attachmentFile) {
		LOGGER.info(String.format("Opening BUY deal. Trader: %s. Rate: %s", traderName, dealRate));
		boolean result = getTrader(traderName).openBuy(dealRate,
				new AbstractTrader.TrendAndFileInfo(trend, attachmentFile));
		String balance = getTrader(traderName).getBalance();
		if (balance != null) {
			LOGGER.info(String.format("Balance: %s", balance));
		}
		return result;
	}
	
	public boolean notifyLow(String traderName, float dealRate, Integer trend, String attachmentFile) {
		LOGGER.info(String.format("Notifying about low price. Trader: %s. Rate: %s", traderName, dealRate));
		boolean result = getTrader(traderName).preOpenBuy(dealRate,
				new AbstractTrader.TrendAndFileInfo(trend, attachmentFile));
		return result;
	}

	public boolean sell(float dealRate) {
		return sell(AbstractTrader.DEFAULT_TRADER, dealRate, null, null);
	}

	public boolean sell(String traderName, float dealRate, Integer trend, String attachmentFile) {
		LOGGER.info(String.format("Opening SELL deal. Trader: %s. Rate: %s", traderName, dealRate));
		boolean result = getTrader(traderName).openSell(dealRate,
				new AbstractTrader.TrendAndFileInfo(trend, attachmentFile));
		String balance = getTrader(traderName).getBalance();
		if (balance != null) {
			LOGGER.info(String.format("Balance: %s", balance));
		}
		return result;
	}

	public boolean notifyHigh(String traderName, float dealRate, Integer trend, String attachmentFile) {
		LOGGER.info(String.format("Notifying about high price. Trader: %s. Rate: %s", traderName, dealRate));
		boolean result = getTrader(traderName).preOpenSell(dealRate,
				new AbstractTrader.TrendAndFileInfo(trend, attachmentFile));
		return result;
	}
	
	public boolean closeBuy(float dealRate) {
		return closeBuy(AbstractTrader.DEFAULT_TRADER, dealRate, null);
	}

	public boolean closeBuy(String traderName, float dealRate, Integer trend) {
		LOGGER.info(String.format("Closing buy deal. Trader: %s. Rate: %s", traderName, dealRate));
		boolean result = getTrader(traderName).closeBuy(dealRate, new AbstractTrader.TrendInfo(trend));
		String balance = getTrader(traderName).getBalance();
		if (balance != null) {
			LOGGER.info(String.format("Balance: %s", balance));
		}
		return result;
	}

	public boolean closeSell(float dealRate) {
		return closeSell(AbstractTrader.DEFAULT_TRADER, dealRate, null);
	}

	public boolean closeSell(String traderName, float dealRate, Integer trend) {
		LOGGER.info(String.format("Closing sell deal. Trader: %s. Rate: %s", traderName, dealRate));
		boolean result = getTrader(traderName).closeSell(dealRate, new AbstractTrader.TrendInfo(trend));
		String balance = getTrader(traderName).getBalance();
		if (balance != null) {
			LOGGER.info(String.format("Balance: %s", balance));
		}
		return result;
	}

	protected void setSold() {
		sold = true;
	}

	protected void setBought() {
		bought = true;
	}

	protected void resetBougthFlag() {
		bought = false;
	}

	protected void resetSoldFlag() {
		bought = false;
		sold = false;
	}

	protected void resetDealFlags() {
		bought = false;
		sold = false;
	}

	public boolean ableToBuy() {
		return direction >= Directions.BUY_AND_SELL && !bought;
	}

	public boolean ableToSell() {
		return direction <= Directions.BUY_AND_SELL && !sold;
	}

	protected AbstractTrader getDefaultTrader() {
		return getTrader(AbstractTrader.DEFAULT_TRADER);
	}

	public void setTrader(AbstractTrader trader) {
		traders.put(AbstractTrader.DEFAULT_TRADER, trader);
		if (traderNames.isEmpty()) {
			traderNames.add(AbstractTrader.DEFAULT_TRADER);
		}
	}

	public void addTrader(AbstractTrader trader) {
		traders.put(trader.getName(), trader);
		traderNames.add(trader.getName());
	}

	public AbstractTrader getTrader(String name) {
		if (!traders.containsKey(name))
			return null;
		return traders.get(name);
	}

	public void setModel(AbstractModel model) {
		this.model = model;
		lastBands.adjustCapacity(model.capacity());
	}

	public float getBandWidth() {
		return bandWidth;
	}

	public void setBandWidth(float bandWidth) {
		this.bandWidth = bandWidth;
	}

	public int getAvgLength() {
		return avgLength;
	}

	public void setAvgLength(int avgLength) {
		this.avgLength = avgLength;
	}
}
