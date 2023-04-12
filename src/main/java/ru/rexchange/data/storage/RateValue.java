package ru.rexchange.data.storage;

import java.util.Date;

public class RateValue {
	private long time;
	private float rate;

	public RateValue(long time, float rate) {
		super();
		this.time = time;
		this.rate = rate;
	}

	public long getTime() {
		return time;
	}

	public float getRate() {
		return rate;
	}

	@Override
	public String toString() {
		return String.format("%s: %s", new Date(time), rate);
	}
}
