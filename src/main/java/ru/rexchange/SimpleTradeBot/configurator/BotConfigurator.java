package ru.rexchange.SimpleTradeBot.configurator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import ru.rexchange.data.model.AbstractModel;
import ru.rexchange.data.model.AdaptiveModel;
import ru.rexchange.tools.FileUtils;
import ru.rexchange.trading.AbstractTrader;
import ru.rexchange.trading.TradeMailNotifier;
import ru.rexchange.trading.bot.AbstractBot;
import ru.rexchange.trading.bot.SecondBot;

public class BotConfigurator implements Runnable {
	protected static Logger LOGGER = Logger.getLogger(BotConfigurator.class);

	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(12);
	private static final String CONFIG_FILE = "config.json";
	private static final Gson GSON_PROCESSOR = new Gson();

	protected static boolean initialized = false;

	Map<String, AbstractModel> models = new HashMap<>();
	Map<String, AbstractTrader> traders = new HashMap<>();
	Map<String, AbstractBot> bots = new HashMap<>();

	@Override
	public void run() {
		LOGGER.info("Reading config file...");
		try (InputStream is = new FileInputStream(CONFIG_FILE)) {
			Properties props = GSON_PROCESSOR.fromJson(FileUtils.readToString(is, "UTF-8"),
					Properties.class);
			if (!initialized) {
				scheduler.scheduleAtFixedRate(this, props.periodicity, props.periodicity,
						TimeUnit.SECONDS);
				initialized = true;
			}
			processConfig(props);
		} catch (IOException | JsonSyntaxException e) {
			LOGGER.error(e);
			return;
		}
	}

	protected void processConfig(Properties props) {
		// ==============Модели===============
		List<String> configModels = new ArrayList<>();
		for (int i = 0; i < props.modelProps.length; i++) {
			ModelProperties mp = props.modelProps[i];
			configModels.add(mp.name);
			if (models.containsKey(mp.name)) {
				AbstractModel xm = models.get(mp.name);
				if (!xm.getBaseCurrency().equals(mp.baseCurrency)) {
					xm.setBaseCurrency(mp.baseCurrency);
				}
				if (!xm.getQuotedCurrency().equals(mp.quotedCurrency)) {
					xm.setQuotedCurrency(mp.quotedCurrency);
				}
				if (xm.capacity() > mp.length) {
					xm.adjustCapacity(mp.length);
				}
			} else {
				AbstractModel newM;
				newM = AdaptiveModel.createDefault(mp.baseCurrency, mp.quotedCurrency, mp.length);
				newM.setName(mp.name);
				models.put(mp.name, newM);
			}
		}

		for (String xModel : new HashSet<>(models.keySet())) {
			if (!configModels.contains(xModel)) {
				models.remove(xModel);
			}
		}

		// ==============Трейдеры===============
		List<String> configTraders = new ArrayList<>();
		for (int i = 0; i < props.traderProps.length; i++) {
			TraderProperties tp = props.traderProps[i];
			configTraders.add(tp.name);
			if (traders.containsKey(tp.name)) {
				AbstractTrader xt = traders.get(tp.name);
				if (xt.isActive() != tp.active) {
					if (tp.active)
						xt.setActive();
					else
						xt.setInactive();
					LOGGER.info(String.format("Trader %s changed its state. Active = %s", tp.active));
				}
				xt.customRuntimeReconfig();
				// todo реконфиг
			} else {
				AbstractTrader newT = new TradeMailNotifier(tp.name, tp.signalsOnly);
				((TradeMailNotifier) newT).configureDefault();
				if (!tp.active)
					newT.setInactive();
				traders.put(tp.name, newT);
			}
		}

		for (String xTrader : new HashSet<>(traders.keySet())) {
			if (!configTraders.contains(xTrader)) {
				traders.remove(xTrader);
			}
		}

		// ==============Боты===============
		List<String> configBots = new ArrayList<>();
		for (int i = 0; i < props.botProps.length; i++) {
			BotProperties bp = props.botProps[i];
			if (!models.containsKey(bp.model)) {
				LOGGER.error(String.format("Cannot find model %s in config", bp.model));
				continue;
			}
			if (!traders.containsKey(bp.trader)) {
				LOGGER.error(String.format("Cannot find trader %s in config", bp.trader));
				continue;
			}
			configBots.add(bp.name);
			if (bots.containsKey(bp.name)) {
				SecondBot xb = (SecondBot) bots.get(bp.name);
				if (xb.getBandWidth() != bp.bandsWidth) {
					xb.setBandWidth(bp.bandsWidth);
				}
				if (xb.getAvgLength() != bp.bandsLength) {
					xb.setAvgLength(bp.bandsLength);
				}
				// todo реконфиг
			} else {
				SecondBot newB = new SecondBot(models.get(bp.model), bp.tradeDirection);
				newB.addTrader(traders.get(bp.trader));
				newB.setBandWidth(bp.bandsWidth);
				newB.setAvgLength(bp.bandsLength);
				bots.put(bp.name, newB);
				LOGGER.info(String.format("New bot %s started. Model: %s, Trader: %s", bp.name,
						bp.model, bp.trader));
				LOGGER.debug(String.format("%s's BandWidth: %s. MALength: %s", bp.name,
						bp.bandsWidth, bp.bandsLength));
				scheduler.scheduleAtFixedRate(newB, 0, props.periodicity, TimeUnit.SECONDS);
			}
		}

		for (String xBot : new HashSet<>(bots.keySet())) {
			if (!configBots.contains(xBot)) {
				// bots.remove(xBot);
				LOGGER.warn(String.format(
						"Bot instance %s is deleted from config "
								+ "but cannot be deleted from running app",
						xBot));
			}
		}
	}

	public ScheduledExecutorService getScheduler() {
		return scheduler;
	}

	protected static class ModelProperties {
		String name;
		String baseCurrency;
		String quotedCurrency;
		int length;
	}

	protected static class TraderProperties {
		String name;
		boolean signalsOnly;
		boolean active;
	}

	protected static class BotProperties {
		String name;
		String trader;// todo возможность ставить много
		String model;
		int bandsLength;
		float bandsWidth;
		int tradeDirection;
	}

	protected static class Properties {
		public ModelProperties[] modelProps;
		public TraderProperties[] traderProps;
		public BotProperties[] botProps;
		int periodicity;
	}
}
