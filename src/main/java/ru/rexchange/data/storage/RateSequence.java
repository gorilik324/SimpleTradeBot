package ru.rexchange.data.storage;

import java.util.ArrayList;
import java.util.List;

public class RateSequence extends ArrayList<RateValue> {
	private static final long serialVersionUID = 1L;

	public List<Float> getRates() {
		List<Float> result = new ArrayList<Float>();
		for (RateValue value : this) {
			result.add(value.getRate());
		}
		return result;
	}

	public RateValue first() {
		if (isEmpty())
			return null;
		return get(0);
	}

	public RateValue last() {
		if (isEmpty())
			return null;
		return get(size() - 1);
	}
}
