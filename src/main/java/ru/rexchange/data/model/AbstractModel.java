package ru.rexchange.data.model;

import ru.rexchange.data.storage.RateSequence;
import ru.rexchange.data.storage.RateValue;

public abstract class AbstractModel extends LimitedQueue<RateValue> {
	protected String name = null;
	protected String baseCurrency = null;
	protected String quotedCurrency = null;
	protected boolean ignoreGetValueErrors = true;

	public AbstractModel(String baseCurrency, String quotedCurrency, int size) {
		super(size);
		this.baseCurrency = baseCurrency;
		this.quotedCurrency = quotedCurrency;
	}

	@Override
	protected void initContainer() {
		values = new RateSequence();
	}

	public synchronized RateValue increment() {
		RateValue newR = readNext();
		addToQueue(newR);
		return newR;
	}

	protected abstract RateValue readNext();

	public RateSequence getRates() {
		return (RateSequence) values;
	}

	public void setIgnoreGetValueErrors(boolean ignoreGetValueErrors) {
		this.ignoreGetValueErrors = ignoreGetValueErrors;
	}

	public String getBaseCurrency() {
		return baseCurrency;
	}

	public void setBaseCurrency(String baseCurrency) {
		this.baseCurrency = baseCurrency;
	}

	public String getQuotedCurrency() {
		return quotedCurrency;
	}

	public void setQuotedCurrency(String quotedCurrency) {
		this.quotedCurrency = quotedCurrency;
	}

	public String getName() {
		return name == null ? String.format("%s:%s", baseCurrency, quotedCurrency) : name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
