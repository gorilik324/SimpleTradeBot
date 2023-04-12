package ru.rexchange.apis.exmo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import ru.rexchange.apis.ApiConnectionManager;

public class ExmoApiProvider {
	private static final Logger LOGGER = Logger.getLogger(ExmoApiProvider.class);
	private static ApiConnectionManager apiManager = new ApiConnectionManager(
      "https://api.exmo.com/v1.1");

	public static class OrdersResponse {
		public PairInfo pairInfo;
	}

	public static class PairInfo {
		public double ask_quantity;
		public double ask_amount;
		public double ask_top;
		public double bid_quantity;
		public double bid_amount;
		public double bid_top;
		public double[][] ask;
		public double[][] bid;

		public OrderRecord[] getAsk() {
			OrderRecord[] result = new OrderRecord[ask.length];
			for (int i = 0; i < ask.length; i++) {
				result[i] = new OrderRecord(ask[i][0], ask[i][1], ask[i][2]);
			}
			return result;
		}

		public OrderRecord[] getBid() {
			OrderRecord[] result = new OrderRecord[bid.length];
			for (int i = 0; i < bid.length; i++) {
				result[i] = new OrderRecord(bid[i][0], bid[i][1], bid[i][2]);
			}
			return result;
		}
	}

	public static class OrderRecord {
		public double price;
		public double quantity;
		public double amount;

		public OrderRecord(double price, double quantity, double amount) {
			this.amount = amount;
			this.quantity = quantity;
			this.price = price;
		}
	}

	public static synchronized OrdersResponse getOrders(String[] pair, int limit)
			throws IOException {
		if (pair.length < 2) {
			LOGGER.error("Некорректный параметр валютной пары");
			return null;
		}
		String pairDescriptor = String.format("%s_%s", pair[0], pair[1]);
		Map<String, String> params = new HashMap<>();
		params.put("pair", pairDescriptor);
		if (limit != 0) {
			params.put("limit", String.valueOf(limit));
		}
		String result = apiManager.sendGet("order_book", params);
		result = result.replaceAll(String.format("\"%s\"", pairDescriptor), "\"pairInfo\"");
		try {
			Gson gson = new Gson();
			OrdersResponse cont = gson.fromJson(result, OrdersResponse.class);
			return cont;
		} catch (JsonSyntaxException e) {
			LOGGER.error(String.format("Ошибка разбора ответа:%n%s", result), e);
		}
		return null;
	}
}
