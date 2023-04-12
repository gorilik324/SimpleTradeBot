package ru.rexchange.data.model;

import java.util.List;

import ru.rexchange.data.SeriesGenerator;
import ru.rexchange.data.storage.RateValue;

public class SimpleModel extends AbstractModel {
	private static final float STARTING_RATE = 2f;
	private long lastNow;
	SeriesGenerator sg = null;

	public SimpleModel(int initSize, int size) {
		super("base", "quoted", size);
		fillInitial(initSize);
	}

	public SimpleModel(int size) {
		super("base", "quoted", size);
	}

	public void fillInitial(int initSize) {
		if (initSize > size)
			initSize = size;
		values.clear();
		List<Float> startValues = getGenerator().generate(initSize);
		lastNow = 0;
		for (Float value : startValues) {
			values.add(new RateValue(lastNow++, value));
		}
	}

	protected RateValue readNext() {
		int curSize = values.size();
		RateValue newR = null;
		if (!values.isEmpty()) {
			newR = new RateValue(lastNow++, getGenerator().generateNext(values.get(curSize - 1).getRate()));
		} else {
			newR = new RateValue(lastNow++, STARTING_RATE);
		}

		return newR;
	}

	private SeriesGenerator getGenerator() {
		if (sg == null) {
			sg = new SeriesGenerator(STARTING_RATE, 0.1f);
		}
		return sg;
	}

	public void setGenerator(SeriesGenerator generator) {
		sg = generator;
	}
}
