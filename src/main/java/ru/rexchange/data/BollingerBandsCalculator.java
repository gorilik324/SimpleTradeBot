package ru.rexchange.data;

import java.util.List;

import ru.rexchange.data.storage.RateSequence;

public class BollingerBandsCalculator {
	private static final int LENGTH = 20;
	private static final float WIDTH = 2f;
	public static class BollBand {
		float upper = 0f;
		float lower = 0f;

		public BollBand(float upper, float lower) {
			this.upper = upper;
			this.lower = lower;
		}

		public float getUpper() {
			return upper;
		}

		public float getLower() {
			return lower;
		}

		public boolean within(float value) {
			return value <= upper && value >= lower;
		}

		public boolean over(float value) {
			return value > upper;
		}

		public boolean below(float value) {
			return value < lower;
		}
	}

	public static BollBand evaluateBand(RateSequence seq, float width, int length) {
		List<Float> rates = seq.getRates();
		float avg = StatisticCalculator.getLastAverageAdaptive(rates, length);
		float dev = StatisticCalculator.getLastDeviationAdaptive(rates, avg, length);
		return new BollBand(avg + width * dev, avg - width * dev);
	}

	public static BollBand evaluateBand(RateSequence seq) {
		return evaluateBand(seq, WIDTH, LENGTH);
	}

	public static BollBand evaluateBand(RateSequence seq, float width) {
		return evaluateBand(seq, width, LENGTH);
	}

}
