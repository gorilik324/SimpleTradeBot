package ru.rexchange.data.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ru.rexchange.apis.coinmarketcap.CoinmarketApiDepProvider;
import ru.rexchange.apis.coinmarketcap.CoinmarketApiDepProvider.TickerInfo;
import ru.rexchange.data.storage.RateValue;

public class CoinmarketDepModel extends AbstractModel {
	private static final Logger LOGGER = Logger.getLogger(CoinmarketModel.class);
	private static final long POLL_INTERVAL = 60000;
  private static Map<String, CoinmarketApiDepProvider.TickerInfo[]> lastData = new HashMap<>();
	private static Map<String, Long> lastDataTime = new HashMap<>();

  public CoinmarketDepModel(String baseCurrency, String quotedCurrency, int size) {
		super(baseCurrency, quotedCurrency, size);
	}

	@Override
	protected RateValue readNext() {
		try {
			synchronized (CoinmarketModel.class) {
				if (!lastData.containsKey(quotedCurrency) || System.currentTimeMillis()
						- lastDataTime.get(quotedCurrency) > POLL_INTERVAL) {
					LOGGER.debug(
							String.format("Updating cached data for currency %s", quotedCurrency));
					lastData.put(quotedCurrency,
              CoinmarketApiDepProvider.getTickersInfo(quotedCurrency));
					lastDataTime.put(quotedCurrency, System.currentTimeMillis());
				}
			}
			for (TickerInfo data : lastData.get(quotedCurrency)) {
				if (baseCurrency.equals(data.symbol)) {
					LOGGER.debug(String.format("readNext %s:%s - price %s", baseCurrency,
							quotedCurrency, data.price));
					return new RateValue(System.currentTimeMillis(), data.price);
				}
			}

			return null;
		} catch (Exception e) {
			LOGGER.error("Ошибка при попытке получить данные с сервера", e);
			if (!getRates().isEmpty() && ignoreGetValueErrors)
				return getRates().get(getRates().size() - 1);
			else
				return null;
		}
	}

}
