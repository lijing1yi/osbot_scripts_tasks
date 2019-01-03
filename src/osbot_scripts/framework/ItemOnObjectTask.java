package osbot_scripts.framework;

import java.util.Objects;
import java.util.Optional;

import org.osbot.rs07.api.Chatbox;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.MethodProvider;

import osbot_scripts.task.AreaInterface;
import osbot_scripts.task.Task;
import osbot_scripts.task.TaskSkeleton;
import osbot_scripts.util.Sleep;

public class ItemOnObjectTask extends TaskSkeleton implements Task, AreaInterface {

	private boolean ranOnStart = false;

	private Area area;

	private int objectId;

	private String itemName;

	private String waitForItemString;

	private boolean clickedObject;

	private String[] chatboxContainingText;

	/**
	 * 
	 * @param scriptName
	 * @param questProgress
	 * @param questConfig
	 * @param prov
	 * @param area
	 * @param objectId
	 */
	public ItemOnObjectTask(String scriptName, int questProgress, int questConfig, MethodProvider prov, Area area,
			int objectId, String itemName) {
		setScriptName(scriptName);
		setProv(prov);
		setArea(area);
		setCurrentQuestProgress(questProgress);
		setObjectId(objectId);
		setItemName(itemName);
	}

	public ItemOnObjectTask(String scriptName, int questProgress, int questConfig, MethodProvider prov, Area area,
			int objectId, String[] names, String itemName) {
		setScriptName(scriptName);
		setProv(prov);
		setArea(area);
		setCurrentQuestProgress(questProgress);
		setObjectId(objectId);
		setItemName(itemName);
		setChatboxContainingText(names);
	}

	public boolean contains() {
		if (!getApi().getClient().isLoggedIn()) {
			return false;
		}
		for (String a : getChatboxContainingText()) {
			if (getApi().getChatbox().contains(Chatbox.MessageType.GAME, a)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub

		// If player is not in the selected field, then walk to it
		if (getArea() != null && !getApi().myPlayer().getArea(10).contains(getArea().getRandomPosition())) {
			// getProv().getWalking().webWalk(getArea());
		}
		ranOnStart = true;
	}

	@Override
	public Area getArea() {
		// TODO Auto-generated method stub
		return this.area;
	}

	@Override
	public String scriptName() {
		// TODO Auto-generated method stub
		return super.getScriptName();
	}

	@Override
	public boolean startCondition() {
		Optional<RS2Object> object = getApi().getObjects().getAll().stream().filter(Objects::nonNull)
				.filter(obj -> obj.getId() == getObjectId()).findFirst();

		if (!object.isPresent()) {
			return false;
		}
		if (getApi().myPlayer().getArea(20).contains(object.get()) && correctStepInQuest()) {
			return true;
		}
		return false;
	}

	@Override
	public void loop() {
		if (!ranOnStart()) {
			onStart();
		}
		if (getArea() != null) {
			// Waiting before player is in an area
			Sleep.sleepUntil(() -> getArea().contains(getApi().myPlayer()), 10000);
		}
		RS2Object object = getApi().getObjects().closest(getObjectId());
		long amountBefore = getApi().getInventory().getAmount(getItemName());
		// Optional<RS2Object> object =
		// getProv().getObjects().getAll().stream().filter(Objects::nonNull)
		// .filter(obj -> obj.getId() == getObjectId()).findFirst();

		if (object != null) {
			Item item = getApi().getInventory().getItem(getItemName());
			// while (item != null && !getProv().getInventory().isItemSelected()) {
			if (item != null) {
				item.interact("Use");
			}
			// }
			object.interact("Use");

			Sleep.sleepUntil(() -> amountBefore != getApi().getInventory().getAmount(getItemName()), 10000);

			if (amountBefore != getApi().getInventory().getAmount(getItemName())) {
				setClickedObject(true);
				getApi().log(isClickedObject());
			}
		}

		if (getWaitForItemString() != null && getWaitForItemString().length() > 0) {
			Sleep.sleepUntil(() -> getApi().getInventory().contains(getWaitForItemString()), 60000);
		}

	}

	@Override
	public boolean finished() {
		if (getApi().getInventory().getAmount(getItemName()) <= 0) {
			return true;
		}
		if (getChatboxContainingText() != null && getChatboxContainingText().length > 0) {
			return contains();
		}
		if (getWaitForItemString() != null && getWaitForItemString().length() > 0) {
			return isClickedObject() && getApi().getInventory().contains(getWaitForItemString());
		}
		if (getArea() != null) {
			return getArea().contains(getApi().myPlayer()) && isClickedObject();
		}
		return isClickedObject();
	}

	public void setArea(Area area) {
		this.area = area;
	}

	@Override
	public int requiredConfigQuestStep() {
		// TODO Auto-generated method stub
		return getCurrentQuestProgress();
	}

	@Override
	public boolean ranOnStart() {
		// TODO Auto-generated method stub
		return ranOnStart;
	}

	/**
	 * @param ranOnStart
	 *            the ranOnStart to set
	 */
	public void setRanOnStart(boolean ranOnStart) {
		this.ranOnStart = ranOnStart;
	}

	/**
	 * @return the objectId
	 */
	public int getObjectId() {
		return objectId;
	}

	/**
	 * @param objectId
	 *            the objectId to set
	 */
	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}

	public String getWaitForItemString() {
		return waitForItemString;
	}

	public void setWaitForItemString(String waitForItemString) {
		this.waitForItemString = waitForItemString;
	}

	/**
	 * @return the clickedObject
	 */
	public boolean isClickedObject() {
		return clickedObject;
	}

	/**
	 * @param clickedObject
	 *            the clickedObject to set
	 */
	public void setClickedObject(boolean clickedObject) {
		this.clickedObject = clickedObject;
	}

	/**
	 * @return the itemName
	 */
	public String getItemName() {
		return itemName;
	}

	/**
	 * @param itemName
	 *            the itemName to set
	 */
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	/**
	 * @return the chatboxContainingText
	 */
	public String[] getChatboxContainingText() {
		return chatboxContainingText;
	}

	/**
	 * @param chatboxContainingText
	 *            the chatboxContainingText to set
	 */
	public void setChatboxContainingText(String[] chatboxContainingText) {
		this.chatboxContainingText = chatboxContainingText;
	}

}
