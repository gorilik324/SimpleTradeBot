package ru.rexchange.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SeriesGenerator {
	private float startValue = 0f;
	private float step = 0f;
	private float shift = 0f;

	private Random r = new Random();
	public SeriesGenerator(float start, float maxStep) {
		this.startValue = start;
		this.step = maxStep;
	}

	public SeriesGenerator(float start, float maxStep, float shift) {
		this.startValue = start;
		this.step = maxStep;
		this.shift = shift;
	}

	public SeriesGenerator() {
		this(50f, 1f);
	}

	public List<Float> generate(int size) {
		List<Float> result = new ArrayList<Float>();
		float prevValue = startValue;
		result.add(startValue);
		for (int i = 1; i < size; i++) {
			prevValue += ((r.nextFloat() - 0.5f) * 2 * step + shift);
			result.add(prevValue);
		}
		return result;
	}

	public float generateNext(float value) {
		return value + ((r.nextFloat() - 0.5f) * 2 * step + shift);
	}
}
