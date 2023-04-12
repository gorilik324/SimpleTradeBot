package ru.rexchange.data.model;

import org.apache.log4j.Logger;

import ru.rexchange.apis.exmo.ExmoApiProvider;
import ru.rexchange.apis.exmo.ExmoApiProvider.OrderRecord;
import ru.rexchange.apis.exmo.ExmoApiProvider.OrdersResponse;
import ru.rexchange.data.storage.RateValue;

public class ExmoModel extends AbstractModel {
	private static final Logger LOGGER = Logger.getLogger(ExmoModel.class);

	public ExmoModel(String baseCurrency, String quotedCurrency, int size) {
		super(baseCurrency, quotedCurrency, size);
	}

	@Override
	protected RateValue readNext() {
		try {
			OrdersResponse orders = ExmoApiProvider
					.getOrders(new String[] { baseCurrency, quotedCurrency }, 3);
			if (orders == null || orders.pairInfo == null)
				return null;
			float buyPrice = getWeighedPrice(orders.pairInfo.getBid());
			float sellPrice = getWeighedPrice(orders.pairInfo.getAsk());
			LOGGER.debug(String.format("readNext %s:%s - buy %s, sell %s", baseCurrency,
					quotedCurrency, buyPrice, sellPrice));
			return new RateValue(System.currentTimeMillis(), (buyPrice + sellPrice) / 2);
		} catch (Exception e) {
			LOGGER.error(String.format("Ошибка при попытке получить данные для пары %s:%s с сервера", baseCurrency, quotedCurrency), e);
			if (!getRates().isEmpty() && ignoreGetValueErrors)
				return getRates().get(getRates().size() - 1);
			else
				return null;
		}
	}

	private float getWeighedPrice(OrderRecord[] orders) {
		float buyPrice = 0f;
		float buyPriceFactor = 0f;
		for (int i = 0; i < orders.length; i++) {
			buyPrice += orders[i].amount;
			buyPriceFactor += orders[i].quantity;
		}
		buyPrice /= buyPriceFactor;
		return buyPrice;
	}
}
