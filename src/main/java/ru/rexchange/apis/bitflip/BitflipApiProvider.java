package ru.rexchange.apis.bitflip;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import ru.rexchange.apis.ApiConnectionManager;
import ru.rexchange.crypt.CryptTools;

public class BitflipApiProvider {
	private static final Logger LOGGER = Logger.getLogger(BitflipApiProvider.class);
	private static ApiConnectionManager apiManager = new ApiConnectionManager(
			"https://api.bitflip.cc/method");
	private static final String API_TOKEN_PARAM = "X-API-Token";
	private static final String API_SIGN_PARAM = "X-API-Sign";
	private static final String API_SECRET_KEY = "b638f35677423798aa6905484887db74b7d6336d13e70c06006758c838bc281b";
	private static final String API_TOKEN = "383683e0-923c525c-0f0ca865-d2e4ccf6-912c8d64";

	protected static final String getPairString(String baseCurrency, String quotedCurrency) {
		return String.format("%s:%s", baseCurrency, quotedCurrency);
	}

	public static Date getTime() throws IOException {
		String result = apiManager.sendGet("server.getTime");
		Gson gson = new Gson();
		Long[] cont = gson.fromJson(result, Long[].class);
		if (cont.length > 1)
			return new Date(cont[1]);
		return null;
	}

	private static class OrdersRequest {
		public String pair;
		public String[] type = null;
		public Integer limit = null;

		public OrdersRequest(String pair) {
			this.pair = pair;
		}

		public OrdersRequest(String pair, int limit) {
			this.pair = pair;
			this.limit = limit;
		}
	}

	public static class OrderRecord {
		public double amount;
		public double rate;
		public double price;
	}

	public static class OrdersResponse {
		public OrderRecord[] buy;
		public OrderRecord[] sell;
	}

	public static synchronized OrdersResponse getOrders(String[] pair, int limit)
			throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, IOException {
		Gson gson = new Gson();
		String request = gson.toJson(new OrdersRequest(getPairString(pair[0], pair[1]), limit));

		String sign = CryptTools.calculateHMAC(request, API_SECRET_KEY);
		Map<String, String> props = new HashMap<>();
		props.put(API_TOKEN_PARAM, API_TOKEN);
		props.put(API_SIGN_PARAM, sign);

		String result = apiManager.sendPost("market.getOrderBook", request, props);
		try {
			OrdersResponse[] cont = gson.fromJson(result, OrdersResponse[].class);
			return cont[1];
		} catch (JsonSyntaxException e) {
			LOGGER.error(String.format("Ошибка разбора ответа:%n%s", result), e);
			return null;
		}
	}
}
