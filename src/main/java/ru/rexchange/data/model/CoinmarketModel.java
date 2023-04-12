package ru.rexchange.data.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ru.rexchange.apis.coinmarketcap.CoinmarketApiProvider;
import ru.rexchange.apis.coinmarketcap.CoinmarketApiProvider.Data;
import ru.rexchange.data.storage.RateValue;

public class CoinmarketModel extends AbstractModel {
  private static final Logger LOGGER = Logger.getLogger(CoinmarketModel.class);
  private static final long POLL_INTERVAL = 60000;
  private static Map<String, CoinmarketApiProvider.Data[]> lastData = new HashMap<>();
  private static Map<String, Long> lastDataTime = new HashMap<>();

  public CoinmarketModel(String baseCurrency, String quotedCurrency, int size) {
    super(baseCurrency, quotedCurrency, size);
  }

  @Override
  protected RateValue readNext() {
    try {
      synchronized (CoinmarketModel.class) {
        if (!lastData.containsKey(quotedCurrency)
            || (System.currentTimeMillis() - lastDataTime.get(quotedCurrency) > POLL_INTERVAL)
            || lastData.get(quotedCurrency) == null) {
          LOGGER.debug(String.format("Updating cached data for currency %s", quotedCurrency));
          lastData.put(quotedCurrency, CoinmarketApiProvider.getTickersInfo(quotedCurrency));
          lastDataTime.put(quotedCurrency, System.currentTimeMillis());
        }
      }
      if (lastData.get(quotedCurrency) == null) {
        LOGGER.warn("Last price data wasn't received");
        return null;
      }
      for (Data data : lastData.get(quotedCurrency)) {
        if (baseCurrency.equals(data.symbol)) {
          LOGGER.debug(String.format("readNext %s:%s - price %s", baseCurrency, quotedCurrency,
              data.quote.price_info.price));
          return new RateValue(System.currentTimeMillis(), data.quote.price_info.price);
        }
      }

      return null;
    } catch (Exception e) {
      LOGGER.error("Ошибка при попытке получить данные с сервера", e);
	  LOGGER.debug(getRates().size());
	  LOGGER.debug(ignoreGetValueErrors);
      if (!getRates().isEmpty() && ignoreGetValueErrors) {
        return getRates().get(getRates().size() - 1);
      } else {
        return null;
      }
    }
  }

}
