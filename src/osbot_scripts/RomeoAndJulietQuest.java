package osbot_scripts;

import java.awt.Graphics2D;
import java.util.Map.Entry;

import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import osbot_scripts.database.DatabaseUtilities;
import osbot_scripts.events.LoginEvent;
import osbot_scripts.framework.AccountStage;
import osbot_scripts.login.LoginHandler;
import osbot_scripts.qp7.progress.RomeoAndJuliet;
import osbot_scripts.task.Task;
import osbot_scripts.taskhandling.TaskHandler;

@ScriptManifest(author = "pim97", info = "RomeoAndJulietQuest", logo = "", name = "QUEST_ROMEO_AND_JULIET", version = 1.0)
public class RomeoAndJulietQuest extends Script {

	private RomeoAndJuliet romeoAndJuliet = new RomeoAndJuliet();

	private LoginEvent login;

	@Override
	public int onLoop() throws InterruptedException {

		RS2Widget closeQuestCompleted = getWidgets().get(277, 15);
		if (getRomeoAndJuliet().getQuestProgress() == 100 || closeQuestCompleted != null) {
			log("Successfully completed quest romeo & juliet");
			if (closeQuestCompleted != null) {
				closeQuestCompleted.interact();
			}
			DatabaseUtilities.updateStageProgress(this, AccountStage.UNKNOWN.name(), 0, login.getUsername());
			stop();
			return -1;
		}

		boolean foundTask = false;
		// Tasks romeo & juilet
		for (Entry<Integer, Task> entry : getRomeoAndJuliet().getRomeoAndJulietTasks().entrySet()) {
			int questStepRequired = entry.getKey();
			Task task = entry.getValue();

			// //At the maximum of tasks, quest complete
			// if (questStepRequired == 23) {
			// log("Quest ended, logging out!");
			// stop();
			// break;
			// }

			// Finding new task when starting
			if (getRomeoAndJuliet().getCurrentTask() == null
					&& questStepRequired == getRomeoAndJuliet().getQuestStageStep()
					&& task.requiredConfigQuestStep() == getRomeoAndJuliet().getQuestProgress()) {
				getRomeoAndJuliet().setCurrentTask(task);
				log("On next task new: " + task.scriptName() + " " + task.requiredConfigQuestStep() + " "
						+ getRomeoAndJuliet().getQuestProgress() + " " + getRomeoAndJuliet().getQuestStageStep() + " "
						+ questStepRequired);
				foundTask = true;
				// Set a task when null
			}

			// Has the script found a task already?
			if (!foundTask && getRomeoAndJuliet().getCurrentTask() == null) {
				log("Not current task, finding next one! " + task.scriptName() + " " + task.requiredConfigQuestStep()
						+ " " + getRomeoAndJuliet().getQuestProgress() + " " + getRomeoAndJuliet().getQuestStageStep()
						+ " " + questStepRequired);
				// Not this task, next one
				continue;
			}
			// for (Task task : getRomeoAndJuliet().getRomeoAndJulietTasks()) {
			if (getRomeoAndJuliet().getCurrentTask() == null) {
				log("System couldnt find a next action, logging out");
				break;
			}
			if (task.requiredConfigQuestStep() == getRomeoAndJuliet().getQuestProgress()
					&& (questStepRequired == getRomeoAndJuliet().getQuestStageStep()
							|| questStepRequired == getRomeoAndJuliet().getQuestStageStep() - 1)) {

				// Waiting for task to finish
				log("finish: " + getRomeoAndJuliet().getCurrentTask().finished());

				// Waiting on task to get finished
				while (!getRomeoAndJuliet().getCurrentTask().finished()) {
					// if (getDialogues().isPendingContinuation() &&
					// getRomeoAndJuliet().isInQuestCutscene()) {
					// getDialogues().clickContinue();
					// } else
					if (getDialogues().isPendingContinuation()) {
						getDialogues().clickContinue();
					} else {
						getRomeoAndJuliet().getCurrentTask().loop();
					}

					log("performing task" + getRomeoAndJuliet().getCurrentTask().getClass().getSimpleName());
					Thread.sleep(1000, 1500);
				}

				// Task is finished
				log("On next task: " + task.scriptName());
				getRomeoAndJuliet().setCurrentTask(task);

				// Step increased with 1 in database
				getRomeoAndJuliet().setQuestStageStep(questStepRequired + 1);

				// Updating stage in database
				if (login != null && login.getUsername() != null) {
					DatabaseUtilities.updateStageProgress(this, getRomeoAndJuliet().getStage().name(),
							getRomeoAndJuliet().getQuestStageStep() - 1, login.getUsername());
				}
			}
		}

		return

		random(500, 600);
	}

	@Override
	public void onPaint(Graphics2D g) {
		// g.drawString("" + getCooksAssistant().getCurrentTask().scriptName() + " "
		// + getCooksAssistant().getCurrentTask().getClass().getSimpleName(), 58, 400);
	}

	@Override
	public void onStart() throws InterruptedException {
		login = LoginHandler.login(this, getParameters());
		// TODO Auto-generated method stub

		if (login != null && login.getUsername() != null) {
			getRomeoAndJuliet()
					.setQuestStageStep(Integer.parseInt(DatabaseUtilities.getQuestProgress(this, login.getUsername())));
		}
		log("Quest progress: " + getRomeoAndJuliet().getQuestStageStep());

		getRomeoAndJuliet().exchangeContext(getBot());
		getRomeoAndJuliet().onStart();
		getRomeoAndJuliet().getTaskHandler().decideOnStartTask();
		// getRomeoAndJuliet().decideOnStartTask();

	}

	/**
	 * @return the romeoAndJuliet
	 */
	public RomeoAndJuliet getRomeoAndJuliet() {
		return romeoAndJuliet;
	}

	/**
	 * @param romeoAndJuliet
	 *            the romeoAndJuliet to set
	 */
	public void setRomeoAndJuliet(RomeoAndJuliet romeoAndJuliet) {
		this.romeoAndJuliet = romeoAndJuliet;
	}

}
