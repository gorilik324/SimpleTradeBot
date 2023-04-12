package ru.rexchange.data.model;

import java.util.ArrayList;
import java.util.List;

import ru.rexchange.data.storage.RateValue;

public class AdaptiveModel extends AbstractModel {
	protected List<AbstractModel> models = new ArrayList<>();
	protected AbstractModel secondModel = null;

	public AdaptiveModel(String baseCurrency, String quotedCurrency, int size) {
		super(baseCurrency, quotedCurrency, size);
	}

	public static AdaptiveModel createDefault(String baseCurrency, String quotedCurrency,
			int size) {
		AdaptiveModel instance = new AdaptiveModel(baseCurrency, quotedCurrency, size);
		instance.addModel(new BinanceModel(baseCurrency, quotedCurrency, size));
		instance.addModel(new CoingeckoModel(baseCurrency, quotedCurrency, size));
		instance.addModel(new CoinmarketModel(baseCurrency, quotedCurrency, size));
		instance.addModel(new ExmoModel(baseCurrency, quotedCurrency, size));
		// instance.addModel(new BitflipModel(baseCurrency, quotedCurrency,
		// size));
		return instance;
	}

	public void addModel(AbstractModel model) {
		models.add(model);
		if (models.size() > 1) {
			models.get(models.size() - 1).setIgnoreGetValueErrors(false);
		}
	}

	@Override
	protected RateValue readNext() {
		RateValue nextRate = null;
		for (AbstractModel model : models) {
			nextRate = model.readNext();
			if (nextRate != null)
				break;
		}
		return nextRate;
	}

	@Override
	public void adjustCapacity(int newSize) {
		for (AbstractModel model : models) {
			model.adjustCapacity(newSize);
		}
	}

	@Override
	public void setBaseCurrency(String baseCurrency) {
		for (AbstractModel model : models) {
			model.setBaseCurrency(baseCurrency);
		}
	}

	@Override
	public void setQuotedCurrency(String quotedCurrency) {
		for (AbstractModel model : models) {
			model.setQuotedCurrency(quotedCurrency);
		}
	}
}
