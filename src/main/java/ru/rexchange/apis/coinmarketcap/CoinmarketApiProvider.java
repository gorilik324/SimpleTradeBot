package ru.rexchange.apis.coinmarketcap;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import ru.rexchange.apis.ApiConnectionManager;

public class CoinmarketApiProvider {
	private static final Logger LOGGER = Logger.getLogger(CoinmarketApiProvider.class);
	private static final String SUPER_KEY = "091093b1-5921-4df9-a67d-8e1f8dc2328e";
  private static final String SUPER_KEY_NEW = "MzliZDFlYzItYmI5Zi00YThkLTg5OWEtMWE1ODYwMDlmMTMy";
	private static ApiConnectionManager apiManager = new ApiConnectionManager(
			"https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings");

	// https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?start=1&limit=5000&convert=USD&CMC_PRO_API_KEY=091093b1-5921-4df9-a67d-8e1f8dc2328e
	public static synchronized Data[] getTickersInfo() throws IOException {
		return getTickersInfo("USD");
	}

	public static synchronized Data[] getTickersInfo(String quotedCurrency) throws IOException {
		Map<String, String> params = new HashMap<>();
		params.put("convert", quotedCurrency);
		params.put("CMC_PRO_API_KEY", SUPER_KEY);
		params.put("limit", "1000");//todo менять адаптивно
		String result = apiManager.sendGet("latest", params);
		
		result = result.replaceAll(String.format("\"%S\":", quotedCurrency.toLowerCase()), "\"price_info\":");
		 
		Gson gson = new Gson();
		try {
			OrdersResponse resp = gson.fromJson(result, OrdersResponse.class);
			return resp.data;
		} catch (JsonSyntaxException e) {
			LOGGER.error(String.format("Ошибка разбора ответа:%n%s", result), e);
			return null;
		}
	}

	public static class OrdersResponse {
		public Status status;
		public Data[] data;
	}

	public static class Status {

	}

	public static class Data {
		public int id;
		public String name;
		public String symbol;
		public Date last_updated;
		public QuoteInfo quote;
	}

	public static class QuoteInfo {
		public PriceInfo price_info;
	}

	public static class PriceInfo {
		public float price;
		public double market_cap;
		public Date last_updated;
	}

	/*
	 * public static void main(String[] args) { try {
	 * CoinmarketApiProvider.getTickersInfo("USD"); } catch (IOException e) {
	 * e.printStackTrace(); } }
	 */
}