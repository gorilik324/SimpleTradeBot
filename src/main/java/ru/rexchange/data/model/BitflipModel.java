package ru.rexchange.data.model;

import org.apache.log4j.Logger;

import ru.rexchange.apis.bitflip.BitflipApiProvider;
import ru.rexchange.apis.bitflip.BitflipApiProvider.OrderRecord;
import ru.rexchange.apis.bitflip.BitflipApiProvider.OrdersResponse;
import ru.rexchange.data.storage.RateValue;

public class BitflipModel extends AbstractModel {
	private static final Logger LOGGER = Logger.getLogger(BitflipModel.class);

	public BitflipModel(String baseCurrency, String quotedCurrency, int size) {
		super(baseCurrency, quotedCurrency, size);
	}

	@Override
	protected RateValue readNext() {
		try {
			OrdersResponse orders = BitflipApiProvider
					.getOrders(new String[] { baseCurrency, quotedCurrency }, 3);
			if (orders == null)
				return null;
			float buyPrice = getWeighedPrice(orders.buy);
			float sellPrice = getWeighedPrice(orders.sell);
			LOGGER.debug(String.format("readNext %s:%s - buy %s, sell %s", baseCurrency,
					quotedCurrency, buyPrice, sellPrice));
			return new RateValue(System.currentTimeMillis(), (buyPrice + sellPrice) / 2);
		} catch (Exception e) {
			LOGGER.error("Ошибка при попытке получить данные с сервера", e);
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
			buyPrice += orders[i].rate * orders[i].amount;
			buyPriceFactor += orders[i].amount;
		}
		buyPrice /= buyPriceFactor;
		return buyPrice;
	}
}
