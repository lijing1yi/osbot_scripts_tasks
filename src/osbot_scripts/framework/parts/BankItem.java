package osbot_scripts.framework.parts;

public class BankItem {

	private String name;

	private int amount;

	private int itemId;

	private long amountBeforeAction = -1;

	private int sellPrice;

	private boolean completedTask = false;

	private boolean withdrawNoted;

	private boolean inBankToBeSkipped;

	/**
	 * @param withdrawNoted
	 *            TODO
	 * 
	 */
	public BankItem(String name, int amount, boolean withdrawNoted) {
		setName(name);
		setAmount(amount);
		setWithdrawNoted(withdrawNoted);
	}

	public BankItem(String name, int itemId, int amount, int sellPrice, boolean withdrawNoted) {
		setName(name);
		setItemId(itemId);
		setAmount(amount);
		setPrice(sellPrice);
		setWithdrawNoted(withdrawNoted);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the amount
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * @param amount
	 *            the amount to set
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * @return the amountBeforeAction
	 */
	public long getAmountBeforeAction() {
		return amountBeforeAction;
	}

	/**
	 * @param l
	 *            the amountBeforeAction to set
	 */
	public void setAmountBeforeAction(long l) {
		this.amountBeforeAction = l;
	}

	/**
	 * @return the itemId
	 */
	public int getItemId() {
		return itemId;
	}

	/**
	 * @param itemId
	 *            the itemId to set
	 */
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	/**
	 * @return the sellPrice
	 */
	public int getPrice() {
		return sellPrice;
	}

	/**
	 * @param sellPrice
	 *            the sellPrice to set
	 */
	public void setPrice(int sellPrice) {
		if (sellPrice <= 0) {
			return;
		}
		this.sellPrice = sellPrice;
	}

	/**
	 * @return the completedTask
	 */
	public boolean isCompletedTask() {
		return completedTask;
	}

	/**
	 * @param completedTask
	 *            the completedTask to set
	 */
	public void setCompletedTask(boolean completedTask) {
		this.completedTask = completedTask;
	}

	/**
	 * @return the withdrawNoted
	 */
	public boolean isWithdrawNoted() {
		return withdrawNoted;
	}

	/**
	 * @param withdrawNoted
	 *            the withdrawNoted to set
	 */
	public void setWithdrawNoted(boolean withdrawNoted) {
		this.withdrawNoted = withdrawNoted;
	}

	/**
	 * @return the inBankToBeSkipped
	 */
	public boolean isInBankToBeSkipped() {
		return inBankToBeSkipped;
	}

	/**
	 * @param inBankToBeSkipped
	 *            the inBankToBeSkipped to set
	 */
	public void setInBankToBeSkipped(boolean inBankToBeSkipped) {
		this.inBankToBeSkipped = inBankToBeSkipped;
	}

}