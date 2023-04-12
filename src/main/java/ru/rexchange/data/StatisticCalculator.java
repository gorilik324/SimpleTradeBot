package ru.rexchange.data;

import java.util.List;

public class StatisticCalculator {
	public static float getAverage(List<Float> data) {
		float result = 0;
		for (float value : data) {
			result += value;
		}
		return result / data.size();
	}

	public static float getAverage(List<Float> data, int startIndex) {
		float result = 0;
		for (int i = startIndex; i < data.size(); i++) {
			result += data.get(i);
		}
		return result / (data.size() - startIndex);
	}

	public static float getLastAverage(List<Float> data, int length) {
		if (data.size() < length)
			return 0f;
		float result = 0;
		for (int i = data.size() - length; i < data.size(); i++) {
			result += data.get(i);
		}
		return result / length;
	}

	public static float getLastAverageAdaptive(List<Float> data, int length) {
		int dataSize = data.size();
		if (dataSize == 0)
			return 0f;
		float result = 0;
		for (int i = Math.max(0, dataSize - length); i < dataSize; i++) {
			result += data.get(i);
		}
		return result / Math.min(length, dataSize);
	}

	public static float getAverage(List<Float> data, int startIndex, int length) {
		if (data.size() < length + startIndex)
			return 0f;
		float result = 0;
		for (int i = startIndex; i < startIndex + length; i++) {
			result += data.get(i);
		}
		return result / length;
	}

	public static float getFirstAverageAdaptive(List<Float> data, int length) {
		int dataSize = data.size();
		if (dataSize == 0)
			return 0f;
		float result = 0;
		for (int i = 0; i < Math.min(length, dataSize); i++) {
			result += data.get(i);
		}
		return result / Math.min(length, dataSize);
	}

	public static float getDeviation(List<Float> data) {
		float avg = getAverage(data);
		float result = 0;
		for (float value : data) {
			result += Math.pow((value - avg), 2);
		}
		return (float) Math.sqrt(result / data.size());
	}

	public static float getDeviation(List<Float> data, int startIndex) {
		float avg = getAverage(data, startIndex);
		float result = 0;
		for (int i = startIndex; i < data.size(); i++) {
			result += Math.pow((data.get(i) - avg), 2);
		}
		return (float) Math.sqrt(result / (data.size() - startIndex));
	}

	public static float getLastDeviation(List<Float> data, int length) {
		if (data.size() < length)
			return 0f;
		float avg = getLastAverage(data, length);
		return getLastDeviation(data, avg, length);
	}

	public static float getLastDeviation(List<Float> data, float average, int length) {
		if (data.size() < length)
			return 0f;
		float result = 0;
		for (int i = data.size() - length; i < data.size(); i++) {
			result += Math.pow((data.get(i) - average), 2);
		}
		return (float) Math.sqrt(result / length);
	}

	public static float getLastDeviationAdaptive(List<Float> data, float average, int length) {
		int dataSize = data.size();
		if (dataSize == 0)
			return 0f;
		float result = 0;
		for (int i = Math.max(0, dataSize - length); i < dataSize; i++) {
			result += Math.pow((data.get(i) - average), 2);
		}
		return (float) Math.sqrt(result / Math.min(length, dataSize));
	}

	public static float getDeviation(List<Float> data, int startIndex, int length) {
		if (data.size() < length + startIndex)
			return 0f;
		float avg = getDeviation(data, startIndex, length);
		float result = 0;
		for (int i = startIndex; i < startIndex + length; i++) {
			result += Math.pow((data.get(i) - avg), 2);
		}
		return (float) Math.sqrt(result / length);
	}
}
