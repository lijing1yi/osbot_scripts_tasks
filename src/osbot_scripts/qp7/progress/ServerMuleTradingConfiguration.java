package osbot_scripts.qp7.progress;

import java.util.HashMap;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import osbot_scripts.bot.utils.BotCommands;
import osbot_scripts.bot.utils.RandomUtil;
import osbot_scripts.database.DatabaseUtilities;
import osbot_scripts.events.LoginEvent;
import osbot_scripts.events.WidgetActionFilter;
import osbot_scripts.framework.AccountStage;
import osbot_scripts.sections.total.progress.MainState;
import osbot_scripts.taskhandling.TaskHandler;
import osbot_scripts.util.Sleep;

public class ServerMuleTradingConfiguration extends QuestStep {

	public ServerMuleTradingConfiguration(LoginEvent event, Script script) {
		super(-1, -1, AccountStage.UNKNOWN, event, script, false);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onStart() {
		timeout = System.currentTimeMillis();

		demo = new ThreadDemo();
		demo.exchangeContext(this.getBot());
		demo.setLoginEvent(getEvent());
		new Thread(demo).start();
	}

	@Override
	public void timeOutHandling(TaskHandler tasks) {
		// TODO Auto-generated method stub

	}

	private String accountStatus;

	private ThreadDemo demo;

	// private static final Area GRAND_EXCHANGE = new Area(
	// new int[][] { { 3160, 3489 }, { 3169, 3489 }, { 3169, 3483 }, { 3160, 3483 }
	// });

	private HashMap<String, Integer> itemMap = new HashMap<String, Integer>();

	private boolean tradingDone = false;

	// private boolean update = true;

	private long timeout = -1;

	private int tries = 0;

	private String lastTradedPlayer = null;

	private int coinsAmount = -1;

	@Override
	public void onLoop() throws InterruptedException {
		if (getAccountStatus() == null) {
			setAccountStatus(DatabaseUtilities.getAccountStatus(this, getEvent().getUsername(), getEvent()));

			if (getAccountStatus() == null) {
				setAccountStatus(DatabaseUtilities.getAccountStatus("server_muling", this, getEvent().getUsername(),
						getEvent()));

				if (getAccountStatus() == null) {
					return;
				}
			}
		}

		if (getEvent().hasFinished() && !isLoggedIn()) {
			BotCommands.killProcess(this, getScript(), "BECAUSE NOT LOGGED IN 01 MULE TRADING", getEvent());
		}

		log("Running the side loop..");

		System.out.println("STATUS: " + getAccountStatus());

		// If the player is not in the grand exchange area, then walk to it
		if (!new Area(new int[][] { { 3161, 3492 }, { 3168, 3492 }, { 3168, 3485 }, { 3161, 3485 } })
				.contains(myPlayer())) {
			getWalking()
					.webWalk(new Area(new int[][] { { 3161, 3492 }, { 3168, 3492 }, { 3168, 3485 }, { 3161, 3485 } }));
			log("The player has a grand exchange task but isn't there, walking to there");
		}
		// Not the mule

		if (tradingDone) {

			if (getAccountStatus().equalsIgnoreCase("SUPER_MULE")) {
				// if (update) {
				DatabaseUtilities.updateAccountValue(this, getEvent().getUsername(), 0, getEvent());
				DatabaseUtilities.updateStageProgress(this, "UNKNOWN", 0, getEvent().getUsername(), getEvent());
				DatabaseUtilities.updateStageProgress("server_muling", this, "UNKNOWN", 0,
						getEvent().getEmailTradeWith(), getEvent());

				DatabaseUtilities.insertLoggingMessage(this, getEvent(), "GOLD_TRANSFER",
						Integer.toString(coinsAmount));

				// }
				BotCommands.killProcess(this, getScript(), "BECAUSE OF DONE WITH SUPER MULE TRADING", getEvent());
				getScript().stop();
			} else {
				// Successfull trading
				DatabaseUtilities.updateStageProgress("server_muling", this, "UNKNOWN", 0, getEvent().getUsername(),
						getEvent());

				int totalAccountValue = (int) getBank().getAmount(995) + (int) getInventory().getAmount(995);

				log("[ESTIMATED MULE VALUE] account value is: " + totalAccountValue);
				if (getEvent() != null && getEvent().getUsername() != null && totalAccountValue > 0) {
					DatabaseUtilities.updateAccountValue("server_muling", this, getEvent().getUsername(),
							totalAccountValue, getEvent());
				}

				int newPartnerFindTries = 0;
				boolean newPartner = false;
				String tradeWith = null;
				int doubletrade = 0;

				while (!newPartner) {

					if (getTrade().getLastRequestingPlayer() != null
							&& getTrade().getLastRequestingPlayer().getName() != null) {
						tradeWith = getTrade().getLastRequestingPlayer().getName();
					}

					log("Found partner: " + tradeWith + " got current partner: " + getEvent().getTradeWith());
					if (tradeWith != null && getEvent().getTradeWith() != null
							&& (!tradeWith.equalsIgnoreCase(getEvent().getTradeWith()) || doubletrade >= 5)) {
						newPartner = true;
					}

					if (tradeWith != null && getEvent().getTradeWith() != null
							&& tradeWith.equalsIgnoreCase(getEvent().getTradeWith())) {
						doubletrade++;
					}

					log("Currently looking for a new partner to trade with " + newPartnerFindTries);
					log("Currently at : " + newPartnerFindTries + " / 250 before logging out due to timeout");
					Thread.sleep(1500);

					// This is so the account doesn't stay logged in for more than 4 minutes at a
					// time and not finding a new partner to trade with
					if (newPartnerFindTries > 50) {
						DatabaseUtilities.updateStageProgress(this, "UNKNOWN", 0, getEvent().getUsername(), getEvent());
						BotCommands.killProcess(this, getScript(), "BECAUSE OF DONE WITH UNKNOWN TRADING", getEvent());
						getScript().stop();
					}

					if (getBank().isOpen()) {
						getBank().close();
					}

					newPartnerFindTries++;
				}
				lastTradedPlayer = null;
				tries = 0;
				timeout = System.currentTimeMillis();
				tradingDone = false;
				newPartnerFindTries = 0;
				log("Found a new partner to trade with!");
				log("Set trading with to: " + tradeWith);
				getEvent().setTradeWith(tradeWith);
			}
			return;

		}

		log(getEvent().getAccountStage());

		tries++;

		if (tries > (getAccountStatus().equalsIgnoreCase("SUPER_MULE") ? 300 : 300)) {
			tradingDone = true;
			// update = true;
			log("Failed to trade it over");
		}

		if (getAccountStatus().equalsIgnoreCase("SUPER_MULE")) {

			log("currently trading: " + getTrade().isCurrentlyTrading());
			if ((getTrade().isCurrentlyTrading() || getTrade().isFirstInterfaceOpen()
					|| getTrade().isSecondInterfaceOpen()) && itemMap.size() > 0) {
				trade(getEvent().getTradeWith(), itemMap, false);

				log("currently trading");
				return;
			}

			log("getting to trade...");

			// Not in g.e.? Walk to it
			walkToGrandExchange();

			// Not having the coins right now, getting it from the bank
			if (!getInventory().contains(995) && !getTrade().isCurrentlyTrading()) {

				// Open bank
				if (!getBank().isOpen()) {
					getBank().open();
					Sleep.sleepUntil(() -> getBank().isOpen(), 5000);

					int totalAccountValue = (int) getBank().getAmount(995);

					log("[ESTIMATED MULE VALUE] account value is: " + totalAccountValue);
					if (getEvent() != null && getEvent().getUsername() != null && totalAccountValue > 0) {
						DatabaseUtilities.updateAccountValue(this, getEvent().getUsername(), totalAccountValue,
								getEvent());
					}
				}

				getBank().depositAll();
				Sleep.sleepUntil(() -> getInventory().isEmpty(), 5000);

				// Getting the cash from the bank
				getBank().withdraw(995, (int) getBank().getAmount(995));
				Sleep.sleepUntil(() -> !getInventory().isEmpty(), 5000);

			} else {
				// Putting items into the list to trade
				if (itemMap.size() == 0 && (int) getInventory().getAmount(995) > 0) {
					log("items to trade set to: coins " + (int) getInventory().getAmount(995));
					itemMap.put("Coins", (int) getInventory().getAmount(995));
				}
			}

			if (itemMap.size() > 0 && getInventory().contains(995) && !getTrade().isFirstInterfaceOpen()
					&& !getTrade().isSecondInterfaceOpen() && !getTrade().isCurrentlyTrading()) {
				trade(getEvent().getTradeWith(), itemMap, false);
			}

			// Open bank
			if (!getInventory().contains(995) && !getTrade().isCurrentlyTrading() && !getTrade().isFirstInterfaceOpen()
					&& !getTrade().isSecondInterfaceOpen()) {

				boolean done = false;
				while (!done) {
					if (getBank().isOpen()) {
						log("Bank was already open");
						break;
					}
					getBank().open();

					Sleep.sleepUntil(() -> getBank().isOpen(), 5000);

					done = getBank().isOpen();

					Thread.sleep(1500);
				}

				log("coins inv: " + getInventory().getAmount("Coins") + " coins bank: " + getBank().getAmount("Coins"));

				if (getBank().isOpen() && getInventory().getAmount("Coins") <= 0 && getBank().getAmount("Coins") <= 0) {
					tradingDone = true;
					log("Trading is done!");
				}
			}
		}

		// The mule itself
		else

		{

			log("time: " + (System.currentTimeMillis() - timeout));

			// Not in g.e.? Walk to it
			walkToGrandExchange();

			log("last traded player: " + getTrade().getLastRequestingPlayer());

			if (!getInventory().contains("Coins")) {

				// Player is trading you
				if (getTrade().getLastRequestingPlayer() != null
						&& getTrade().getLastRequestingPlayer().getName() != null) {
					lastTradedPlayer = getTrade().getLastRequestingPlayer().getName();
				}

				// When that player is not null
				if (lastTradedPlayer != null) { // && lastTradedPlayer.equalsIgnoreCase(getEvent().getTradeWith())) {
					trade(lastTradedPlayer, new HashMap<String, Integer>(), true);

					log("Accepting trade request... doing actions...");
				} else if (getTrade().isCurrentlyTrading()) {
					trade(lastTradedPlayer, new HashMap<String, Integer>(), true);
					log("Trading....");
				} else if (getPlayers().closest(getEvent().getTradeWith()) != null && getBank().isOpen()) {
					log("Player is near and having bank open, closing...");
					getBank().close();
				}

			} else {
				log("Waiting for the other player to send a request...");
			}

			if (getInventory().contains(995) && !getTrade().isCurrentlyTrading()) {

				// Open bank
				if (!getBank().isOpen()) {
					getBank().open();
					Sleep.sleepUntil(() -> getBank().isOpen(), 20000);
				}

				if (getBank().isOpen()) {

					// Depositing the cash from the bank
					// if (getBank().deposit(995, (int) getBank().getAmount(995))) {
					if (getBank().depositAll()) {
						Sleep.sleepUntil(() -> getInventory().isEmpty(), 20000);
						if (getInventory().isEmpty()) {
							log("Trading is done!");
							tradingDone = true;
						}
					}
					Sleep.sleepUntil(() -> !getInventory().contains(995), 20000);
				}
			}
			// When the mule doesn't have coins and trading isnt done and the other player
			// is not near then check if it is already completed or not and hasn't completed
			// in 5 minutes
			else if (!getInventory().contains(995) && !tradingDone
					&& getPlayers().closest(getEvent().getTradeWith()) == null
					&& (System.currentTimeMillis() - timeout > 400_000)) {

				if (!getBank().isOpen()) {
					getBank().open();
					Sleep.sleepUntil(() -> getBank().isOpen(), 20000);
				}
				if (getBank().isOpen()) {

					int inv = (int) getInventory().getAmount(995);
					int bank = (int) getBank().getAmount(995);

					if (inv <= 0 && bank > 0) {
						if (getBank().depositAll()) {
							Sleep.sleepUntil(() -> getInventory().isEmpty(), 20000);
							if (getInventory().isEmpty()) {
								log("Trading timeout is done!");

								tradingDone = true;
							}
						}
					}
				}
			}
		}
	}

	public void trade(String name, HashMap<String, Integer> itemSet, boolean acceptLast) throws InterruptedException {
		if (getEvent().getAccountStage().equalsIgnoreCase("SUPER_MULE")) {
			Thread.sleep(4000);
		}
		
		String cleanName = name.replaceAll(" ", "\\u00a0");
		Player player = getScript().getPlayers().closest(cleanName);

		if (!getAccountStatus().equalsIgnoreCase("SUPER_MULE")) {
			if (lastTradedPlayer != null) {
				if (player != null && !isTrading() && player.interact("trade with")) {
					log("1");
					new ConditionalSleep(10000) {
						@Override
						public boolean condition() {
							return isTrading();
						}
					}.sleep();
				}
			}
		} else {
			if (player != null && !isTrading() && player.interact("trade with")) {
				log("1");
				new ConditionalSleep(10000) {

					@Override
					public boolean condition() {
						return isTrading();
					}
				}.sleep();
			}
		}

		if (isTrading() && getTrade().isFirstInterfaceOpen()) {
			log("2");
			if (!tradeOfferMatches(itemSet)) {
				log("3");
				for (

				String item : itemSet.keySet()) {
					if (!getTrade().getOurOffers().contains(item)) {
						if (getTrade().offer(item, itemSet.get(item))) {
							log("4");
							new ConditionalSleep(10000) {

								@Override
								public boolean condition() {
									return getTrade().getOurOffers().contains(item);
								}
							}.sleep();
						}
					}
				}
			} else {
				if (acceptLast && getTrade().didOtherAcceptTrade()) {
					log("5");
					if (WidgetActionFilter.interactTil(this, "Accept", 335, 11, new ConditionalSleep(10000) {

						@Override
						public boolean condition() {
							return getTrade().isSecondInterfaceOpen();
						}
					})) {
						log("6");
						new ConditionalSleep(10000) {
							@Override
							public boolean condition() {
								return getTrade().isSecondInterfaceOpen();
							}
						}.sleep();
					}

				} else if (!acceptLast && !hasAccepted()) {
					log("7");
					if (WidgetActionFilter.interactTil(this, "Accept", 335, 11, new ConditionalSleep(10000) {

						@Override
						public boolean condition() {
							return hasAccepted();
						}
					})) {
						log("8");
						new ConditionalSleep(10000) {

							@Override
							public boolean condition() {
								return getTrade().isSecondInterfaceOpen();
							}
						}.sleep();
					}
				}

			}
		} else if (isTrading() && getTrade().isSecondInterfaceOpen()) {

			if (acceptLast && getTrade().didOtherAcceptTrade()) {
				log("9");
				if (WidgetActionFilter.interactTil(this, "Accept", 334, 25, new ConditionalSleep(10000) {

					@Override
					public boolean condition() {
						return !isTrading();
					}
				})) {
					log("10");
					new ConditionalSleep(10000) {

						@Override
						public boolean condition() {
							return getTrade().isSecondInterfaceOpen();
						}
					}.sleep();
				}

			} else if (!acceptLast && !hasAccepted()) {
				log("11");
				if (WidgetActionFilter.interactTil(this, "Accept", 334, 25, new ConditionalSleep(10000) {

					@Override
					public boolean condition() {
						return !isTrading();
					}
				})) {
					log("12");
					new ConditionalSleep(10000) {
						@Override
						public boolean condition() {
							return getTrade().isSecondInterfaceOpen();
						}
					}.sleep();
				}

			}
		}
	}

	private boolean hasAccepted() {
		return WidgetActionFilter.containsText(this, 335, 30, "Waiting for other player...")
				|| WidgetActionFilter.containsText(this, 334, 4, "Waiting for other player...");
	}

	private boolean tradeOfferMatches(HashMap<String, Integer> itemSet) throws InterruptedException {
		for (String item : itemSet.keySet()) {
			Sleep.sleepUntil(() -> isTrading() && getTrade().getOurOffers().getItem(item) != null, 2000);
			if (isTrading() && getTrade().getOurOffers().getItem(item) == null) {
				log("Trade Offer Missing: " + item);
				return false;
			}
		}
		return true;
	}

	public boolean isTrading() {
		return getTrade().isCurrentlyTrading() || getTrade().isFirstInterfaceOpen()
				|| getTrade().isSecondInterfaceOpen();
	}

	private void walkToGrandExchange() {
		if (!new Area(new int[][] { { 3144, 3508 }, { 3144, 3471 }, { 3183, 3470 }, { 3182, 3509 } })
				.contains(myPlayer())) {
			getWalking()
					.webWalk(new Area(new int[][] { { 3160, 3494 }, { 3168, 3494 }, { 3168, 3485 }, { 3160, 3485 } }));
			log("The player has a grand exchange task but isn't there, walking to there");
		}
	}

	@Override
	public boolean isCompleted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MainState getNextMainState() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the accountStatus
	 */
	public String getAccountStatus() {
		return accountStatus;
	}

	/**
	 * @param accountStatus
	 *            the accountStatus to set
	 */
	public void setAccountStatus(String accountStatus) {
		this.accountStatus = accountStatus;
	}

}