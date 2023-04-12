package ru.rexchange.trading;

public class FakeTrader extends AbstractTrader {

	public FakeTrader(String name, int amountDeterminant, int amountValue) {
		super(name, amountDeterminant, amountValue);
	}

	public FakeTrader(int amountDeterminant, int amountValue) {
		super(AbstractTrader.DEFAULT_TRADER, amountDeterminant, amountValue);
	}

	@Override
	public void requestCurrenciesAmount() {
		baseCurrencyAmount = 100f;
		quotedCurrencyAmount = 100f;
	}

	@Override
	public boolean openBuy(float desiredRate, DealInfo info) {
		float baseAmount = getDealAmount(desiredRate);
		float neededAmount = TradeTools.convert(desiredRate, baseAmount);
		if (quotedCurrencyAmount < neededAmount)
			return false;
		openedDealAmount = baseAmount;
		quotedCurrencyAmount -= neededAmount;
		dealRate = desiredRate;
		return true;
	}
	
	@Override
	public boolean preOpenBuy(float desiredRate, DealInfo info) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean openSell(float desiredRate, DealInfo info) {
		/*
		 * float baseAmount = getDealAmount(desiredRate); if (baseCurrencyAmount <
		 * baseAmount) return false; float neededAmount =
		 * TradeTools.convert(desiredRate, baseAmount); openedDealAmount = neededAmount;
		 * baseCurrencyAmount -= baseAmount;
		 */
		return true;
	}
	
	@Override
	public boolean preOpenSell(float desiredRate, DealInfo info) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean closeBuy(float desiredRate, DealInfo info) {
		if (openedDealAmount == 0) {
			// todo: ругнуться?
			return true;
		}
		float resultAmount = TradeTools.convert(desiredRate, openedDealAmount);
		quotedCurrencyAmount += resultAmount;
		openedDealAmount = 0;
		dealRate = 0f;
		return true;
	}

	@Override
	public boolean closeSell(float desiredRate, DealInfo info) {
		/*
		 * if (openedDealAmount == 0) { // todo: ругнуться? return true; }
		 * baseCurrencyAmount += openedDealAmount; float neededAmount =
		 * TradeTools.convert(desiredRate, openedDealAmount); openedDealAmount = 0;
		 */
		return true;
	}

	@Override
	public void configureDefault() {
	}

	@Override
	public void customRuntimeReconfig() {
	}
}
