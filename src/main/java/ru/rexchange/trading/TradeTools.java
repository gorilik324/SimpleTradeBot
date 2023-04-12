package ru.rexchange.trading;

public class TradeTools {
	public static float convert(float rate, float amount) {
		return amount * rate;
	}

	public static float inverse(float rate, float amount) {
		return amount / rate;
	}
}
