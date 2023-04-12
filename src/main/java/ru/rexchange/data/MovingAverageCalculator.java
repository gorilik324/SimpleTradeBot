package ru.rexchange.data;

import java.util.ArrayList;
import java.util.List;

public class MovingAverageCalculator {
	public static List<Float> getMovingAverage(List<Float> values, int length) {
		List<Float> result = new ArrayList<Float>();
		for (int i = 0; i < values.size() - length; i++) {
			result.add(StatisticCalculator.getAverage(values, i, length));
		}
		return result;
	}
}
