package ru.rexchange.apis.coingecko;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ru.rexchange.apis.ApiConnectionManager;

public class CoingeckoApiProvider {
	private static final Logger LOGGER = Logger.getLogger(CoingeckoApiProvider.class);
	private static Map<String, List<String>> EXCLUSIONS = new HashMap<String, List<String>>();
	static {
		EXCLUSIONS.put("ATLAS", Arrays.asList("Atlantis", "Atlas Cloud"));
		EXCLUSIONS.put("CAKE", Collections.singletonList("CakeDAO"));
		EXCLUSIONS.put("GMT", Arrays.asList("Gambit", "GoMining Token", "GMT Token"));
	};
	private static Map<String, String> INCLUSIONS = new HashMap<String, String>();
	static {
		INCLUSIONS.put("Star Atlas", "ATLAS");
		INCLUSIONS.put("CakeSwap", "CAKE");
		INCLUSIONS.put("STEPN", "GMT");
		INCLUSIONS.put("Green Metaverse", "GMT");
		INCLUSIONS.put("Rangers Protocol", "RPG");
		INCLUSIONS.put("Clover", "CLV");
		INCLUSIONS.put("Seedify.fund", "SFUND");
		INCLUSIONS.put("CryptoMines Eternal", "ETERNAL");
		INCLUSIONS.put("Highstreet", "HIGH");
		INCLUSIONS.put("Raptoreum", "RTM");
		INCLUSIONS.put("Fractal", "FCL");
		INCLUSIONS.put("Waves", "WAVES");
		INCLUSIONS.put("GoldMiner", "GM");
};

	private static ApiConnectionManager apiManager = new ApiConnectionManager(
      "https://api.coingecko.com/api/v3");
	private static final Map<String, String> Currencies = new HashMap<String, String>();

	public static synchronized Double getPrice(String[] pair)
			throws IOException {
		if (pair.length < 2) {
			LOGGER.error("Некорректный параметр валютной пары");
			return null;
		}
		if (Currencies.isEmpty()) {
			fillCurrencies();
		}
		if (!Currencies.containsKey(pair[0])) {
			LOGGER.error("Неизвестная валюта - " + pair[0]);
			return null;
		}
		String currencyCode = Currencies.get(pair[0]);
		Map<String, String> params = new HashMap<>();
		params.put("ids", Currencies.get(pair[0]));
		params.put("vs_currencies", pair[1]);
		//https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd
		String result = apiManager.sendGet("simple/price", params);
		/*{
		  "bitcoin": {
		    "usd": 10283.21,
		    "rub": 770231
		  },
		  "ripple": {
		    "usd": 0.241437,
		    "rub": 18.08
		  }
		}*/
		try {
			JSONObject js = new JSONObject(result);
			JSONObject obj = js.getJSONObject(currencyCode);
			return obj.getDouble(pair[1].toLowerCase());
		} catch (JSONException e) {
			LOGGER.error(String.format("Ошибка разбора ответа:%n%s", result), e);
		}
		return null;
	}
	
	private static void fillCurrencies() throws IOException {
		String result = apiManager.sendGet("coins/list");
		try {
			JSONArray jsa = new JSONArray(result);
			for (int i = 0; i < jsa.length(); i++) {
				JSONObject obj = jsa.getJSONObject(i);
				String symbol = obj.getString("symbol").toUpperCase();
				if (INCLUSIONS.containsKey(obj.getString("name"))) {
					Currencies.put(symbol, obj.getString("id"));
					continue;
				} else if (EXCLUSIONS.containsKey(symbol))  {
					if (EXCLUSIONS.get(symbol).contains(obj.getString("name"))) {
						LOGGER.info("Skipping token " + symbol + "\n" + obj.toString());
						continue;
					}
				}
				if (!Currencies.containsKey(symbol))
					Currencies.put(symbol, obj.getString("id"));
				else
					LOGGER.warn("Повторный символ: " + symbol + " - " + obj.toString() +
							" - " + Currencies.get(symbol) + "был загружен ранее");
			}
		} catch (JSONException e) {
			LOGGER.error(String.format("Ошибка разбора ответа на запрос списка валют:%n%s", result), e);
		}

	}
	
	/*public static void main(String[] args) throws IOException {
		System.out.println(getPrice(new String[] {"btc", "usd"}));
	}*/

	/*private static class PriceResponse {
		
	}*/
}
