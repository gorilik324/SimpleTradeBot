package ru.rexchange.data.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class LimitedQueue<T> implements Iterable<T> {
	protected int size;
	protected List<T> values;
	
	public LimitedQueue(int size) {
		this.size = size;
		initContainer();
	}

	public T get(int i) {
		return values.get(i);
	}

	public int size() {
		return values.size();
	}

	public int capacity() {
		return size;
	}

	public void adjustCapacity(int newSize) {
		if (newSize > size)
			size = newSize;
	}

	protected void initContainer() {
		values = new LinkedList<>();
	}

	public void addToQueue(T newR) {
		int curSize = values.size();
		if (newR == null)
			return;
		if (curSize == size) {
			shiftRates(1, false);
			values.set(curSize - 1, newR);
		} else {
			values.add(newR);
		}
	}

	private void shiftRates(int shift, boolean needTrim) {
		int curSize = values.size();
		for (int i = shift; i < curSize; i++) {
			values.set(i - shift, values.get(i));
		}
		if (needTrim) {
			for (int i = curSize - 1; i > curSize - shift - 1; i--) {
				values.remove(i);
			}
		}
	}

	@Override
	public Iterator<T> iterator() {
		return values.iterator();
	}
}
