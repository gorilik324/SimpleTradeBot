package ru.rexchange.data.model;

import org.apache.log4j.Logger;

import ru.rexchange.apis.binance.BinanceApiProvider;
import ru.rexchange.data.storage.RateValue;

public class BinanceModel extends AbstractModel {
	private static final Logger LOGGER = Logger.getLogger(BinanceModel.class);

	public BinanceModel(String baseCurrency, String quotedCurrency, int size) {
		super(baseCurrency, quotedCurrency, size);
	}

	@Override
	protected RateValue readNext() {
		try {
			Double price = BinanceApiProvider.getPrice(new String[] { baseCurrency, quotedCurrency });
			if (price == null)
				return null;
			LOGGER.debug(String.format("readNext %s:%s - %s", baseCurrency, quotedCurrency, price));
			return new RateValue(System.currentTimeMillis(), price.floatValue());
		} catch (Exception e) {
			LOGGER.error(String.format("Ошибка при попытке получить данные для пары %s:%s с сервера", baseCurrency, quotedCurrency), e);
			if (!getRates().isEmpty() && ignoreGetValueErrors)
				return getRates().get(getRates().size() - 1);
			else
				return null;
		}
	}

}
