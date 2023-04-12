package ru.rexchange.SimpleTradeBot;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ru.rexchange.SimpleTradeBot.configurator.BotConfigurator;

/**
 * Hello world!
 *
 */
public class App {
	private static Logger LOGGER = Logger.getLogger(App.class);
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		// BasicConfigurator.configure();
		LOGGER.debug("Hello World!");
		// todo: 1. Вынести scheduler и задавать его конфигуратору извне?
		// todo: 2. Выводить конфиг в консоль
		// todo: 3. Сделать трейдера стоп-лосса
		BotConfigurator bc = new BotConfigurator();
		bc.run();
		try {
			bc.getScheduler().awaitTermination(60, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO: handle exception
			LOGGER.error(e);
		}

		/*AbstractModel model1 = new AdaptiveModel("XRP", "USD", 50);
		AbstractTrader trader1 = new TradeMailNotifier("Ripple", true);
		((TradeMailNotifier) trader1).configureDefault();
		AbstractModel model2 = new AdaptiveModel("ETH", "USD", 50);
		AbstractTrader trader2 = new TradeMailNotifier("Etherium", true);
		((TradeMailNotifier) trader2).configureDefault();
		AbstractModel model3 = new BitflipModel("TRX", "USD", 50);
		AbstractTrader trader3 = new TradeMailNotifier("Tron", true);
		((TradeMailNotifier) trader3).configureDefault();

		AbstractBot bot1 = new SecondBot(model1, Directions.BUY_AND_SELL);
		((SecondBot) bot1).addTrader(trader1);
		AbstractBot bot2 = new SecondBot(model2, Directions.BUY_AND_SELL);
		((SecondBot) bot2).addTrader(trader2);
		AbstractBot bot3 = new SecondBot(model3, Directions.BUY_AND_SELL);
		((SecondBot) bot3).addTrader(trader3);

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
		scheduler.scheduleAtFixedRate(bot1, 0, 300, TimeUnit.SECONDS);
		scheduler.scheduleAtFixedRate(bot2, 0, 300, TimeUnit.SECONDS);
		scheduler.scheduleAtFixedRate(bot3, 0, 300, TimeUnit.SECONDS);
		try {
			scheduler.awaitTermination(60, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO: handle exception
			LOGGER.error(e);
		}*/
	}
}
