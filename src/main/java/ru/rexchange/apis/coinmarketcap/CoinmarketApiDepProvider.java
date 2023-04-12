package ru.rexchange.apis.coinmarketcap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import ru.rexchange.apis.ApiConnectionManager;

public class CoinmarketApiDepProvider {
	private static final Logger LOGGER = Logger.getLogger(CoinmarketApiProvider.class);
	private static ApiConnectionManager apiManager = new ApiConnectionManager(
			"https://api.coinmarketcap.com/v1");

	public static synchronized TickerInfo[] getTickersInfo() throws IOException {
		return getTickersInfo("USD");
	}

	public static synchronized TickerInfo[] getTickersInfo(String quotedCurrency)
			throws IOException {
		Map<String, String> params = new HashMap<>();
		params.put("convert", quotedCurrency);
		String result = apiManager.sendGet("ticker", params);
		result = result.replaceAll(String.format("price_%s", quotedCurrency.toLowerCase()),
				"price");
		Gson gson = new Gson();
		try {
			return gson.fromJson(result, TickerInfo[].class);
		} catch (JsonSyntaxException e) {
			LOGGER.error(String.format("Ошибка разбора ответа:%n%s", result), e);
			return null;
		}
	}

	public static class TickerInfo {
		public String id;
		public String name;
		public String symbol;

		public float price;
		public float price_btc;

		public long last_updated;
	}

	/*public static void main(String[] args) {
		try {
			CoinmarketApiProvider.getTickersInfo("RUB");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
}
