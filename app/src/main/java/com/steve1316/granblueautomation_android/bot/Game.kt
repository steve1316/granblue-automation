package com.steve1316.granblueautomation_android.bot

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.steve1316.granblueautomation_android.MainActivity
import com.steve1316.granblueautomation_android.data.SummonData
import com.steve1316.granblueautomation_android.ui.settings.SettingsFragment
import com.steve1316.granblueautomation_android.utils.ImageUtils
import com.steve1316.granblueautomation_android.utils.MediaProjectionService
import com.steve1316.granblueautomation_android.utils.MessageLog
import com.steve1316.granblueautomation_android.utils.MyAccessibilityService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.opencv.core.Point
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Main driver for bot activity and navigation for the web browser game, Granblue Fantasy.
 */
class Game(private val myContext: Context) {
	private val TAG: String = "[${MainActivity.loggerTag}]Game"
	
	private var debugMode: Boolean = SettingsFragment.getBooleanSharedPreference(myContext, "debugMode")
	
	val imageUtils: ImageUtils = ImageUtils(myContext, this)
	val gestureUtils: MyAccessibilityService = MyAccessibilityService.getInstance()
	private var twitterRoomFinder: TwitterRoomFinder? = null
	private lateinit var mapSelection: MapSelection
	private val combatMode: CombatMode = CombatMode(this, debugMode)
	
	private val startTime: Long = System.currentTimeMillis()
	
	private var enableDelayBetweenRuns: Boolean = false
	private var delayBetweenRuns: Int = 1
	private var enableRandomizedDelayBetweenRuns: Boolean = false
	private var randomizedDelayBetweenRuns: Int = 1
	
	private var enableDimensionalHalo: Boolean = false
	private var dimensionalHaloSummonList: List<String> = arrayListOf()
	private var dimensionalHaloGroupNumber: Int = 0
	private var dimensionalHaloPartyNumber: Int = 0
	private var dimensionalHaloAmount: Int = 0
	
	private var enableEventNightmare: Boolean = false
	private var eventNightmareSummonList: List<String> = arrayListOf()
	private var eventNightmareGroupNumber: Int = 0
	private var eventNightmarePartyNumber: Int = 0
	
	private var enableROTBExtremePlus: Boolean = false
	private var rotbExtremePlusSummonList: List<String> = arrayListOf()
	private var rotbExtremePlusGroupNumber: Int = 0
	private var rotbExtremePlusPartyNumber: Int = 0
	
	private var enableXenoClashNightmare: Boolean = false
	private var xenoClashNightmareSummonList: List<String> = arrayListOf()
	private var xenoClashNightmareGroupNumber: Int = 0
	private var xenoClashNightmarePartyNumber: Int = 0
	
	var farmingMode: String = ""
	private var mapName: String = ""
	var missionName: String = ""
	private var difficulty: String = ""
	private var itemName: String = ""
	private var itemAmount: Int = 0
	private var itemAmountFarmed: Int = 0
	private var amountOfRuns: Int = 0
	private var combatScriptName: String = ""
	private var combatScript: List<String> = arrayListOf()
	private var summonList: List<String> = arrayListOf()
	private var groupNumber: Int = 0
	private var partyNumber: Int = 0
	
	private var coopFirstRun: Boolean = true
	private var provingGroundsFirstRun: Boolean = true
	
	/**
	 * Returns a formatted string of the elapsed time since the bot started as HH:MM:SS format.
	 *
	 * Source is from https://stackoverflow.com/questions/9027317/how-to-convert-milliseconds-to-hhmmss-format/9027379
	 *
	 * @return String of HH:MM:SS format of the elapsed time.
	 */
	private fun printTime(): String {
		val elapsedMillis: Long = System.currentTimeMillis() - startTime
		
		return String.format(
			"%02d:%02d:%02d",
			TimeUnit.MILLISECONDS.toHours(elapsedMillis),
			TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedMillis)),
			TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedMillis))
		)
	}
	
	/**
	 * Print the specified message to debug console and then saves the message to the log.
	 *
	 * @param message Message to be saved.
	 * @param MESSAGE_TAG TAG to distinguish between messages for where they came from. Defaults to Game's TAG.
	 * @param isError Flag to determine whether to display log message in console as debug or error.
	 */
	fun printToLog(message: String, MESSAGE_TAG: String = TAG, isError: Boolean = false) {
		if (!isError) {
			Log.d(MESSAGE_TAG, message)
		} else {
			Log.e(MESSAGE_TAG, message)
		}
		
		// Remove the newline prefix if needed and place it where it should be.
		if (message.startsWith("\n")) {
			val newMessage = message.removePrefix("\n")
			MessageLog.messageLog.add("\n" + printTime() + " " + newMessage)
		} else {
			MessageLog.messageLog.add(printTime() + " " + message)
		}
	}
	
	/**
	 * Go back to the Home screen by tapping the "Home" button.
	 *
	 * @param confirmLocationCheck Whether or not the bot should confirm that it has arrived at the Home screen.
	 */
	fun goBackHome(confirmLocationCheck: Boolean = false) {
		if (!imageUtils.confirmLocation("home")) {
			printToLog("[INFO] Moving back to the Home screen...")
			findAndClickButton("home")
		} else {
			printToLog("[INFO] Bot is already at the Home screen.")
		}
		
		printToLog("\n[INFO] Screen Width: ${MediaProjectionService.displayWidth}, Screen Height: ${MediaProjectionService.displayHeight}, Screen DPI: ${MediaProjectionService.displayDPI}")
		
		if (confirmLocationCheck) {
			imageUtils.confirmLocation("home")
		}
	}
	
	/**
	 * Wait the specified seconds to account for ping or loading.
	 *
	 * @param seconds Number of seconds to pause execution.
	 */
	fun wait(seconds: Double) {
		runBlocking {
			delay((seconds * 1000).toLong())
		}
	}
	
	/**
	 * Find and click button
	 *
	 * @param buttonName Name of the button image file in the /assets/buttons/ folder.
	 * @param tries Number of tries to find the specified button.
	 * @param suppressError Whether or not to suppress saving error messages to the log in failing to find the button.
	 * @return True if the button was found and clicked. False otherwise.
	 */
	fun findAndClickButton(buttonName: String, tries: Int = 2, suppressError: Boolean = false): Boolean {
		if (debugMode) {
			printToLog("[DEBUG] Now attempting to find and click the \"$buttonName\" button.")
		}
		
		var tempLocation: Point?
		var newButtonName = buttonName
		
		if (buttonName.lowercase() == "quest") {
			tempLocation = imageUtils.findButton("quest_blue", tries = 1, suppressError = suppressError)
			newButtonName = "quest_blue"
			
			if (tempLocation == null) {
				tempLocation = imageUtils.findButton("quest_red", tries = 1, suppressError = suppressError)
				newButtonName = "quest_red"
			}
			
		} else if (buttonName.lowercase() == "raid") {
			tempLocation = imageUtils.findButton("raid_flat", tries = tries, suppressError = suppressError)
			newButtonName = "raid_flat"
			
			if (tempLocation == null) {
				tempLocation = imageUtils.findButton("raid_bouncing", tries = tries, suppressError = suppressError)
				newButtonName = "raid_bouncing"
			}
			
		} else if (buttonName.lowercase() == "coop_start") {
			tempLocation = imageUtils.findButton("coop_start_flat", tries = tries, suppressError = suppressError)
			newButtonName = "coop_start_flat"
			
			if (tempLocation == null) {
				tempLocation = imageUtils.findButton("coop_start_faded", tries = tries, suppressError = suppressError)
				newButtonName = "coop_start_faded"
			}
			
		} else if (buttonName.lowercase() == "event_special_quest") {
			tempLocation = imageUtils.findButton("event_special_quest_flat", tries = tries, suppressError = suppressError)
			newButtonName = "event_special_quest_flat"
			
			if (tempLocation == null) {
				tempLocation = imageUtils.findButton("event_special_quest_bouncing", tries = tries, suppressError = suppressError)
				newButtonName = "event_special_quest_bouncing"
			}
			
		} else if (buttonName.lowercase() == "world") {
			tempLocation = imageUtils.findButton("world", tries = tries, suppressError = suppressError)
			newButtonName = "world"
			
			if (tempLocation == null) {
				tempLocation = imageUtils.findButton("world2", tries = tries, suppressError = suppressError)
				newButtonName = "world2"
			}
			
		} else {
			tempLocation = imageUtils.findButton(buttonName, tries = tries, suppressError = suppressError)
		}
		
		return if (tempLocation != null) {
			gestureUtils.tap(tempLocation.x, tempLocation.y, newButtonName)
		} else {
			false
		}
	}
	
	/**
	 * Checks for CAPTCHA right after selecting a Summon. If detected, alert the user and stop the bot.
	 */
	private fun checkForCAPTCHA() {
		wait(2.0)
		
		if (imageUtils.confirmLocation("captcha", tries = 1)) {
			throw(Exception("[CAPTCHA] CAPTCHA has been detected! Stopping the bot now."))
		} else {
			printToLog("\n[CAPTCHA] CAPTCHA not detected.")
		}
	}
	
	/**
	 * Execute a delay after every run completed based on user settings from config.yaml.
	 */
	private fun delayBetweenRuns() {
		if (enableDelayBetweenRuns) {
			// Check if the provided delay is valid.
			if (delayBetweenRuns < 0) {
				printToLog("\n[INFO] Provided delay in seconds for the resting period is not valid. Defaulting to 15 seconds.")
				delayBetweenRuns = 15
			}
			
			printToLog("\n[INFO] Now waiting for $delayBetweenRuns seconds as the resting period. Please do not navigate from the current screen.")
			
			wait(delayBetweenRuns.toDouble())
		} else if (!enableDelayBetweenRuns && enableRandomizedDelayBetweenRuns) {
			// Check if the lower and upper bounds are valid.
			if (delayBetweenRuns < 0 || delayBetweenRuns > randomizedDelayBetweenRuns) {
				printToLog("\n[INFO] Provided lower bound delay in seconds for the resting period is not valid. Defaulting to 15 seconds.")
				delayBetweenRuns = 15
			}
			
			if (randomizedDelayBetweenRuns < 0 || randomizedDelayBetweenRuns < delayBetweenRuns) {
				printToLog("\n[INFO] Provided upper bound delay in seconds for the resting period is not valid. Defaulting to 60 seconds.")
				randomizedDelayBetweenRuns = 60
			}
			
			val newSeconds = Random().nextInt(randomizedDelayBetweenRuns - delayBetweenRuns) + delayBetweenRuns
			printToLog(
				"\n[INFO] Given the bounds of ($delayBetweenRuns, $randomizedDelayBetweenRuns), bot will now wait for $newSeconds seconds as a resting period. Please do not navigate from the " +
						"current screen.")
			
			wait(newSeconds.toDouble())
		}
		
		printToLog("\n[INFO] Resting period complete.")
	}
	
	/**
	 * Find and select the specified Summon based on the current index on the Summon Selection screen. It will then check for CAPTCHA right
	 * afterwards.
	 *
	 * @param optionalSummonList Overrides the Summon list used. Defaults to the ones selected for Farming Mode.
	 * @return True if the Summon was found and selected. False otherwise.
	 */
	private fun selectSummon(optionalSummonList: List<String> = arrayListOf()): Boolean {
		// Format the Summon strings.
		val newSummonList = mutableListOf<String>()
		val unformattedSummonList = if (optionalSummonList.isNotEmpty()) {
			optionalSummonList
		} else {
			summonList
		}
		
		unformattedSummonList.forEach {
			val newSummonName = it.lowercase().replace(" ", "_")
			newSummonList.add(newSummonName)
		}
		
		// Set up the list of Summon elements.
		val summonElementList = arrayListOf<String>()
		unformattedSummonList.forEach {
			when {
				SummonData.fireSummons.contains(it) -> {
					summonElementList.add("fire")
				}
				SummonData.waterSummons.contains(it) -> {
					summonElementList.add("water")
				}
				SummonData.earthSummons.contains(it) -> {
					summonElementList.add("earth")
				}
				SummonData.windSummons.contains(it) -> {
					summonElementList.add("wind")
				}
				SummonData.lightSummons.contains(it) -> {
					summonElementList.add("light")
				}
				SummonData.darkSummons.contains(it) -> {
					summonElementList.add("dark")
				}
				SummonData.miscSummons.contains(it) -> {
					summonElementList.add("misc")
				}
			}
		}
		
		printToLog("Summon list: $newSummonList")
		printToLog("Summon Element list: $summonElementList")
		
		// Find the location of one of the Summons.
		val summonLocation = imageUtils.findSummon(newSummonList, summonElementList)
		
		return if (summonLocation != null) {
			// Select the Summon.
			gestureUtils.tap(summonLocation.x, summonLocation.y, "template_summon")
			
			// Check for CAPTCHA.
			checkForCAPTCHA()
			
			true
		} else {
			// Reset Summons if not found.
			resetSummons()
			
			false
		}
	}
	
	/**
	 * Reset the available Summons by starting and then retreating from an Old Lignoid Trial Battle.
	 */
	private fun resetSummons() {
		printToLog("[INFO] Resetting Summons...")
		
		// Go back Home.
		goBackHome(confirmLocationCheck = true)
		
		// Scroll the screen down to attempt to see the "Gameplay Extras" button.
		gestureUtils.swipe(500f, 1000f, 500f, 400f)
		
		val listOfSteps: ArrayList<String> = arrayListOf(
			"gameplay_extras", "trial_battles", "trial_battles_old_lignoid", "play_round_button",
			"choose_a_summon", "ok", "close", "menu", "retreat", "retreat_confirmation", "next"
		)
		
		listOfSteps.forEach {
			if (it == "trial_battles_old_lignoid") {
				// Make sure to confirm that the bot arrived at the Trial Battles screen.
				wait(2.0)
				imageUtils.confirmLocation("trial_battles")
			}
			
			if (it == "close") {
				// Wait a few seconds and then confirm its location.
				wait(5.0)
				imageUtils.confirmLocation("trial_battles_description")
			}
			
			var imageLocation: Point? = imageUtils.findButton(it)
			
			while ((it == "gameplay_extras" || it == "trial_battles") && imageLocation == null) {
				// Keep swiping the screen down until the bot finds the specified button.
				if (it == "gameplay_extras") {
					gestureUtils.swipe(500f, 1500f, 500f, 1000f)
				} else if (it == "trial_battles") {
					findAndClickButton("gameplay_extras", tries = 1, suppressError = true)
					gestureUtils.swipe(500f, 1500f, 500f, 1400f)
				}
				
				wait(2.0)
				imageLocation = imageUtils.findButton(it, tries = 1)
			}
			
			if (it == "choose_a_summon" && imageLocation != null) {
				gestureUtils.tap(imageLocation.x, imageLocation.y + 400, "template_summon")
			} else if (it != "choose_a_summon" && imageLocation != null) {
				gestureUtils.tap(imageLocation.x, imageLocation.y, it)
			}
			
			wait(2.0)
		}
	}
	
	/**
	 * Selects the specified Group and Party. It will then start the mission.
	 *
	 * @param optionalGroupNumber Overrides the Group Number. Defaults to the one selected for Farming Mode.
	 * @param optionalPartyNumber Overrides the Party Number. Defaults to the one selected for Farming Mode.
	 * @param tries Number of tries to select a Set before failing. Defaults to 3.
	 * @return True if the mission was successfully started. False otherwise.
	 */
	private fun selectPartyAndStartMission(optionalGroupNumber: Int = 0, optionalPartyNumber: Int = 0, tries: Int = 3): Boolean {
		var setLocation: Point? = null
		var numberOfTries = tries
		
		val selectedGroupNumber = if (optionalGroupNumber == 0) {
			groupNumber
		} else {
			optionalGroupNumber
		}
		
		val selectedPartyNumber = if (optionalPartyNumber == 0) {
			partyNumber
		} else {
			optionalPartyNumber
		}
		
		// Search for the location of the "Set" button based on the Group number.
		while (setLocation == null) {
			setLocation = if (selectedGroupNumber < 8) {
				imageUtils.findButton("party_set_a", tries = 1)
			} else {
				imageUtils.findButton("party_set_b", tries = 1)
			}
			
			if (setLocation == null) {
				numberOfTries -= 1
				
				if (numberOfTries <= 0) {
					if (selectedGroupNumber < 8) {
						throw(Resources.NotFoundException("Could not find Set A."))
					} else {
						throw(Resources.NotFoundException("Could not find Set B."))
					}
				}
				
				// Switch over and search for the other Set.
				setLocation = if (selectedGroupNumber < 8) {
					imageUtils.findButton("party_set_b", tries = 1)
				} else {
					imageUtils.findButton("party_set_a", tries = 1)
				}
			}
		}
		
		// Select the Group.
		var equation: Double = if (selectedGroupNumber == 1) {
			787.0
		} else {
			787.0 - (140 * (selectedGroupNumber - 1))
		}
		
		gestureUtils.tap(setLocation.x - equation, setLocation.y + 140.0, "template_group")
		wait(1.0)
		
		// Select the Party.
		equation = if (selectedPartyNumber == 1) {
			690.0
		} else {
			690.0 - (130 * (selectedPartyNumber - 1))
		}
		
		gestureUtils.tap(setLocation.x - equation, setLocation.y + 740.0, "template_party")
		wait(1.0)
		
		printToLog("[SUCCESS] Selected Group and Party successfully.")
		
		// Start the mission by clicking "OK".
		findAndClickButton("ok")
		wait(2.0)
		
		// Detect if a "This raid battle has already ended" popup appeared.
		if (farmingMode == "Raid" && imageUtils.confirmLocation("raid_just_ended_home_redirect")) {
			printToLog("[WARNING] Raid unfortunately just ended. Backing out now...")
			findAndClickButton("ok")
			return false
		}
		
		return true
	}
	
	/**
	 * Checks if the user has available AP. If not, then it will refill it.
	 *
	 * @param useFullElixir Will use Full Elixir instead of Half Elixir. Defaults to false.
	 * @param tries Number of tries to try to refill AP. Defaults to 3.
	 */
	fun checkAP(useFullElixir: Boolean = false, tries: Int = 3) {
		var numberOfTries = tries
		
		wait(2.0)
		
		if (!imageUtils.confirmLocation("auto_ap_recovered", tries = 1) && !imageUtils.confirmLocation("auto_ap_recovered2", tries = 1)) {
			while ((farmingMode != "Coop" && !imageUtils.confirmLocation("select_a_summon", tries = 1)) ||
				(farmingMode == "Coop" && !imageUtils.confirmLocation("coop_without_support_summon", tries = 1))) {
				if (imageUtils.confirmLocation("not_enough_ap", tries = 1)) {
					if (!useFullElixir) {
						printToLog("[INFO] AP ran out! Using Half Elixir...")
						val location = imageUtils.findButton("refill_half_elixir")!!
						gestureUtils.tap(location.x, location.y + 370, "use")
					} else {
						printToLog("[INFO] AP ran out! Using Full Elixir...")
						val location = imageUtils.findButton("refill_full_elixir")!!
						gestureUtils.tap(location.x, location.y + 370, "use")
					}
					
					wait(1.0)
					
					// Press the "OK" button to confirm the item usage.
					findAndClickButton("ok")
				} else if (farmingMode == "Coop" && !coopFirstRun && imageUtils.findButton("attack") != null) {
					break
				} else {
					numberOfTries -= 1
					if (numberOfTries <= 0) {
						break
					}
				}
			}
		} else {
			findAndClickButton("ok")
		}
		
		printToLog("[INFO] AP is available.")
	}
	
	/**
	 * Checks if the user has available EP. If not, then it will refill it.
	 *
	 * @param useSoulBalm Will use Soul Balm instead of Soul Berry. Defaults to false.
	 * @param tries Number of tries to try to refill AP. Defaults to 3.
	 */
	fun checkEP(useSoulBalm: Boolean = false, tries: Int = 3) {
		var numberOfTries = tries
		
		wait(2.0)
		
		if (!imageUtils.confirmLocation("auto_ep_recovered", tries = 1)) {
			while (farmingMode == "Raid" && !imageUtils.confirmLocation("select_a_summon", tries = 1)) {
				if (imageUtils.confirmLocation("not_enough_ep", tries = 1)) {
					if (!useSoulBalm) {
						printToLog("[INFO] EP ran out! Using Soul Berry...")
						val location = imageUtils.findButton("refill_soul_berry")!!
						gestureUtils.tap(location.x, location.y + 370, "use")
					} else {
						printToLog("[INFO] EP ran out! Using Soul Balm...")
						val location = imageUtils.findButton("refill_soul_balm")!!
						gestureUtils.tap(location.x, location.y + 370, "use")
					}
					
					wait(1.0)
					
					// Press the "OK" button to confirm the item usage.
					findAndClickButton("ok")
				}
				
				numberOfTries -= 1
				if (numberOfTries <= 0) {
					break
				}
			}
		} else {
			findAndClickButton("ok")
		}
		
		printToLog("[INFO] EP is available.")
	}
	
	/**
	 * Detect any dropped loot from the Loot Collected screen while clicking away any dialog popups.
	 *
	 * @param isPendingBattle Skip the incrementation of runs attempted if this was a Pending Battle. Defaults to false.
	 * @param isEventNightmare Skip the incrementation of runs attempted if this was a Event Nightmare. Defaults to false.
	 */
	fun collectLoot(isPendingBattle: Boolean = false, isEventNightmare: Boolean = false) {
		var amountGained = 0
		
		// Close all popups until the bot reaches the Loot Collected screen.
		while (!imageUtils.confirmLocation("loot_collected", tries = 1)) {
			findAndClickButton("close", tries = 1, suppressError = true)
			findAndClickButton("cancel", tries = 1, suppressError = true)
			findAndClickButton("ok", tries = 1, suppressError = true)
			findAndClickButton("new_extended_mastery_level", tries = 1, suppressError = true)
		}
		
		// Now that the bot is at the Loot Collected screen, detect any user-specified items.
		if (!isPendingBattle && !isEventNightmare) {
			printToLog("\n[INFO] Detecting if any user-specified loot dropped this run...")
			amountGained = if (!listOf("EXP", "Angel Halo Weapons", "Repeated Runs").contains(itemName)) {
				imageUtils.findFarmedItems(itemName)
			} else {
				1
			}
			
			itemAmountFarmed += amountGained
			
			// Only increment number of runs for Proving Grounds when the bot acquires the Completion Rewards.
			// Currently for Proving Grounds, completing 2 battles per difficulty nets you the Completion Rewards.
			if (farmingMode == "Proving Grounds") {
				if (itemAmountFarmed != 0 && itemAmountFarmed % 2 == 0) {
					itemAmountFarmed = 0
					amountOfRuns += 1
				}
			} else {
				amountOfRuns += 1
			}
		}
		
		if (!isPendingBattle && !isEventNightmare) {
			if (!listOf("EXP", "Angel Halo Weapons", "Repeated Runs").contains(itemName)) {
				printToLog("\n********************************************************************************")
				printToLog("********************************************************************************")
				printToLog("[INFO] Farming Mode: $farmingMode")
				printToLog("[INFO] Mission: $missionName")
				printToLog("[INFO] Summons: $summonList")
				printToLog("[INFO] # of $itemName gained this run: $amountGained")
				printToLog("[INFO] # of $itemName gained in total: $itemAmountFarmed/$itemAmount")
				printToLog("[INFO] # of runs completed: $amountOfRuns")
				printToLog("********************************************************************************")
				printToLog("********************************************************************************")
			} else {
				printToLog("\n********************************************************************************")
				printToLog("********************************************************************************")
				printToLog("[INFO] Farming Mode: $farmingMode")
				printToLog("[INFO] Mission: $missionName")
				printToLog("[INFO] Summons: $summonList")
				printToLog("[INFO] # of runs completed: $amountOfRuns")
				printToLog("********************************************************************************")
				printToLog("********************************************************************************")
			}
		}
	}
	
	/**
	 * Detect any popups and attempt to close them all with the final destination being the Summon Selection screen.
	 */
	private fun checkForPopups() {
		while (!imageUtils.confirmLocation("select_a_summon", tries = 3)) {
			if (imageUtils.confirmLocation("auto_ap_recovered", tries = 1) || imageUtils.confirmLocation("auto_ap_recovered2", tries = 1)) {
				break
			}
			
			// Break out of the loop if the bot detected the "Not Enough AP" popup.
			if (imageUtils.confirmLocation("not_enough_ap", tries = 1)) {
				break
			}
			
			if (farmingMode == "Rise of the Beasts" && imageUtils.confirmLocation("rotb_proud_solo_quest", tries = 1)) {
				// Scroll down the screen a little bit because the popup itself is too long.
				gestureUtils.scroll()
			}
			
			// Check for certain popups for certain Farming Modes.
			if ((farmingMode == "Rise of the Beasts" && checkROTBExtremePlus()) ||
				(farmingMode == "Special" && missionName == "VH Angel Halo" && itemName == "Angel Halo Weapons" && checkDimensionalHalo()) ||
				(farmingMode == "Event" || farmingMode == "Event (Token Drawboxes)") && checkEventNightmare() ||
				(farmingMode == "Xeno Clash" && checkForXenoClashNightmare())) {
				// Make sure the bot goes back to the Home screen so that the "Play Again" functionality comes back.
				mapSelection.selectMap(farmingMode, mapName, missionName, difficulty)
				break
			}
			
			// If the bot tried to repeat a Extreme/Impossible difficulty Event Raid and it lacked the treasures to host it, go back to the Mission again.
			if ((farmingMode == "Event (Token Drawboxes)" || farmingMode == "Guild Wars") && imageUtils.confirmLocation("not_enough_treasure", tries = 1)) {
				findAndClickButton("ok")
				delayBetweenRuns()
				mapSelection.selectMap(farmingMode, mapName, missionName, difficulty)
				break
			}
			
			// Attempt to close the popup by clicking on any detected "Close" and "Cancel" buttons.
			if (!findAndClickButton("close", tries = 1, suppressError = true)) {
				findAndClickButton("cancel", tries = 1, suppressError = true)
			}
			
			wait(1.0)
		}
	}
	
	/**
	 * Detects any "Friend Request" popups and close them.
	 */
	fun checkFriendRequest() {
		if (imageUtils.confirmLocation("friend_request", tries = 1)) {
			findAndClickButton("cancel")
		}
	}
	
	/**
	 * Checks for Dimensional Halo and if it appeared and the user enabled it in settings, start it.
	 *
	 * @return True if Dimensional Halo was detected and successfully completed. False otherwise.
	 */
	private fun checkDimensionalHalo(): Boolean {
		if (enableDimensionalHalo && imageUtils.confirmLocation("limited_time_quests", tries = 1)) {
			printToLog("\n[D.HALO] Detected Dimensional Halo. Starting it now...")
			dimensionalHaloAmount += 1
			
			printToLog("\n********************************************************************************")
			printToLog("********************************************************************************")
			printToLog("[D.HALO] Dimensional Halo")
			printToLog("[D.HALO] Dimensional Halo Summons: $dimensionalHaloSummonList")
			printToLog("[D.HALO] Dimensional Halo Group Number: $dimensionalHaloGroupNumber")
			printToLog("[D.HALO] Dimensional Halo Party Number: $dimensionalHaloPartyNumber")
			printToLog("[D.HALO] Amount of Dimensional Halos encountered: $dimensionalHaloAmount")
			printToLog("********************************************************************************")
			printToLog("\n********************************************************************************")
			
			// Tap the "Play Next" button to head to the Summon Selection screen.
			findAndClickButton("play_next")
			
			wait(1.0)
			
			// Once the bot is at the Summon Selection screen, select your Summon and Party and start the mission.
			if (imageUtils.confirmLocation("select_a_summon")) {
				selectSummon(optionalSummonList = dimensionalHaloSummonList)
				val startCheck: Boolean = selectPartyAndStartMission(optionalGroupNumber = dimensionalHaloGroupNumber, optionalPartyNumber = dimensionalHaloPartyNumber)
				
				// Once preparations are completed, start Combat Mode.
				if (startCheck && combatMode.startCombatMode(combatScript)) {
					collectLoot()
					return true
				}
			}
		} else if (!enableDimensionalHalo && imageUtils.confirmLocation("limited_time_quests", tries = 1)) {
			printToLog("\n[D.HALO] Dimensional Halo detected but user opted to not run it. Moving on...")
			findAndClickButton("close")
		} else {
			printToLog("\n[D.HALO] No Dimensional Halo detected. Moving on...")
		}
		
		return false
	}
	
	/**
	 * Checks for Event Nightmare and if it appeared and the user enabled it in settings, start it.
	 *
	 * @return True if Event Nightmare was detected and successfully completed. False otherwise.
	 */
	private fun checkEventNightmare(): Boolean {
		if (enableEventNightmare && imageUtils.confirmLocation("limited_time_quests", tries = 1)) {
			// First check if the Nightmare is skippable.
			if (findAndClickButton("event_claim_loot", tries = 1)) {
				printToLog("\n[EVENT] Skippable Event Nightmare detected. Claiming it now...")
				collectLoot(isEventNightmare = true)
				return true
			} else {
				printToLog("\n[EVENT] Detected Event Nightmare. Starting it now...")
				
				printToLog("\n********************************************************************************")
				printToLog("********************************************************************************")
				printToLog("[EVENT] Event Nightmare")
				printToLog("[EVENT] Event Nightmare Summons: $eventNightmareSummonList")
				printToLog("[EVENT] Event Nightmare Group Number: $eventNightmareGroupNumber")
				printToLog("[EVENT] Event Nightmare Party Number: $eventNightmarePartyNumber")
				printToLog("********************************************************************************")
				printToLog("\n********************************************************************************")
				
				// Tap the "Play Next" button to head to the Summon Selection screen.
				findAndClickButton("play_next")
				
				wait(1.0)
				
				// Once the bot is at the Summon Selection screen, select your Summon and Party and start the mission.
				if (imageUtils.confirmLocation("select_a_summon")) {
					selectSummon(optionalSummonList = eventNightmareSummonList)
					val startCheck: Boolean = selectPartyAndStartMission(optionalGroupNumber = eventNightmareGroupNumber, optionalPartyNumber = eventNightmarePartyNumber)
					
					// Once preparations are completed, start Combat Mode.
					if (startCheck && combatMode.startCombatMode(combatScript)) {
						collectLoot()
						return true
					}
				}
			}
		} else if (!enableEventNightmare && imageUtils.confirmLocation("limited_time_quests", tries = 1)) {
			// First check if the Nightmare is skippable.
			if (findAndClickButton("event_claim_loot", tries = 1)) {
				printToLog("\n[EVENT] Skippable Event Nightmare detected. Claiming it now...")
				collectLoot(isEventNightmare = true)
				return true
			} else {
				printToLog("\n[EVENT] Event Nightmare detected but user opted to not run it. Moving on...")
				findAndClickButton("close")
			}
		} else {
			printToLog("\n[EVENT] No Event Nightmare detected. Moving on...")
		}
		
		return false
	}
	
	/**
	 * Checks for Extreme Plus during Rise of the Beasts and if it appeared and the user enabled it in settings, start it.
	 *
	 * @return True if Extreme Plus was detected and successfully completed. False otherwise.
	 */
	private fun checkROTBExtremePlus(): Boolean {
		if (enableROTBExtremePlus && imageUtils.confirmLocation("rotb_extreme_plus", tries = 1)) {
			printToLog("\n[ROTB] Detected Extreme+. Starting it now...")
			dimensionalHaloAmount += 1
			
			printToLog("\n********************************************************************************")
			printToLog("********************************************************************************")
			printToLog("[ROTB] Rise of the Beasts Extreme+")
			printToLog("[ROTB] Rise of the Beasts Extreme+ Summons: $rotbExtremePlusSummonList")
			printToLog("[ROTB] Rise of the Beasts Extreme+ Group Number: $rotbExtremePlusGroupNumber")
			printToLog("[ROTB] Rise of the Beasts Extreme+ Party Number: $rotbExtremePlusPartyNumber")
			printToLog("********************************************************************************")
			printToLog("\n********************************************************************************")
			
			// Tap the "Play Next" button to head to the Summon Selection screen.
			findAndClickButton("play_next")
			
			wait(1.0)
			
			// Once the bot is at the Summon Selection screen, select your Summon and Party and start the mission.
			if (imageUtils.confirmLocation("select_a_summon")) {
				selectSummon(optionalSummonList = rotbExtremePlusSummonList)
				val startCheck: Boolean = selectPartyAndStartMission(optionalGroupNumber = rotbExtremePlusGroupNumber, optionalPartyNumber = rotbExtremePlusPartyNumber)
				
				// Once preparations are completed, start Combat Mode.
				if (startCheck && combatMode.startCombatMode(combatScript)) {
					collectLoot()
					return true
				}
			}
		} else if (!enableROTBExtremePlus && imageUtils.confirmLocation("rotb_extreme_plus", tries = 1)) {
			printToLog("\n[ROTB] Rise of the Beasts Extreme+ detected but user opted to not run it. Moving on...")
			findAndClickButton("close")
		} else {
			printToLog("\n[ROTB] No Rise of the Beasts Extreme+ detected. Moving on...")
		}
		
		return false
	}
	
	/**
	 * Checks for Xeno Clash Nightmare and if it appeared and the user enabled it in settings, start it.
	 *
	 * @return True if Xeno Clash Nightmare was detected and successfully completed. False otherwise.
	 */
	private fun checkForXenoClashNightmare(): Boolean {
		if (enableXenoClashNightmare && imageUtils.confirmLocation("limited_time_quests", tries = 1)) {
			// First check if the Nightmare is skippable.
			if (findAndClickButton("event_claim_loot", tries = 1)) {
				printToLog("\n[XENO] Skippable Xeno Clash Nightmare detected. Claiming it now...")
				collectLoot(isEventNightmare = true)
				return true
			} else {
				printToLog("\n[XENO] Detected Event Nightmare. Starting it now...")
				
				printToLog("\n********************************************************************************")
				printToLog("********************************************************************************")
				printToLog("[XENO] Xeno Clash Nightmare")
				printToLog("[XENO] Xeno Clash Nightmare Summons: $xenoClashNightmareSummonList")
				printToLog("[XENO] Xeno Clash Nightmare Group Number: $xenoClashNightmareGroupNumber")
				printToLog("[XENO] Xeno Clash Nightmare Party Number: $xenoClashNightmarePartyNumber")
				printToLog("********************************************************************************")
				printToLog("\n********************************************************************************")
				
				// Tap the "Play Next" button to head to the Summon Selection screen.
				findAndClickButton("play_next")
				
				wait(1.0)
				
				// Select only the first Nightmare.
				val playRoundButtons = imageUtils.findAll("play_round_buttons")
				gestureUtils.tap(playRoundButtons[0].x, playRoundButtons[0].y, "play_round_buttons")
				
				wait(1.0)
				
				// Once the bot is at the Summon Selection screen, select your Summon and Party and start the mission.
				if (imageUtils.confirmLocation("select_a_summon")) {
					selectSummon(optionalSummonList = xenoClashNightmareSummonList)
					val startCheck: Boolean = selectPartyAndStartMission(optionalGroupNumber = xenoClashNightmareGroupNumber, optionalPartyNumber = xenoClashNightmarePartyNumber)
					
					// Once preparations are completed, start Combat Mode.
					if (startCheck && combatMode.startCombatMode(combatScript)) {
						collectLoot()
						return true
					}
				}
			}
		} else if (!enableXenoClashNightmare && imageUtils.confirmLocation("limited_time_quests", tries = 1)) {
			// First check if the Nightmare is skippable.
			if (findAndClickButton("event_claim_loot", tries = 1)) {
				printToLog("\n[XENO] Skippable Xeno Clash Nightmare detected. Claiming it now...")
				collectLoot(isEventNightmare = true)
				return true
			} else {
				printToLog("\n[XENO] Xeno Clash Nightmare detected but user opted to not run it. Moving on...")
				findAndClickButton("close")
			}
		} else {
			printToLog("\n[XENO] No Xeno Clash Nightmare detected. Moving on...")
		}
		
		return false
	}
	
	/**
	 * Performs additional setup for special fights outlined in config.ini like Dimensional Halo and Event Nightmares.
	 */
	private fun advancedSetup() {
		enableDimensionalHalo = SettingsFragment.getBooleanSharedPreference(myContext, "enableDimensionalHalo")
		dimensionalHaloSummonList = SettingsFragment.getStringSetSharedPreference(myContext, "dimensionalHaloSummonList").toList()
		dimensionalHaloGroupNumber = SettingsFragment.getIntSharedPreference(myContext, "dimensionalHaloGroupNumber")
		dimensionalHaloPartyNumber = SettingsFragment.getIntSharedPreference(myContext, "dimensionalHaloPartyNumber")
		
		enableEventNightmare = SettingsFragment.getBooleanSharedPreference(myContext, "enableEventNightmare")
		eventNightmareSummonList = SettingsFragment.getStringSetSharedPreference(myContext, "eventNightmareSummonList").toList()
		eventNightmareGroupNumber = SettingsFragment.getIntSharedPreference(myContext, "eventNightmareGroupNumber")
		eventNightmarePartyNumber = SettingsFragment.getIntSharedPreference(myContext, "eventNightmarePartyNumber")
		
		enableROTBExtremePlus = SettingsFragment.getBooleanSharedPreference(myContext, "enableROTBExtremePlus")
		rotbExtremePlusSummonList = SettingsFragment.getStringSetSharedPreference(myContext, "rotbExtremePlusSummonList").toList()
		rotbExtremePlusGroupNumber = SettingsFragment.getIntSharedPreference(myContext, "rotbExtremePlusGroupNumber")
		rotbExtremePlusPartyNumber = SettingsFragment.getIntSharedPreference(myContext, "rotbExtremePlusPartyNumber")
		
		enableXenoClashNightmare = SettingsFragment.getBooleanSharedPreference(myContext, "enableXenoClashNightmare")
		xenoClashNightmareSummonList = SettingsFragment.getStringSetSharedPreference(myContext, "xenoClashNightmareSummonList").toList()
		xenoClashNightmareGroupNumber = SettingsFragment.getIntSharedPreference(myContext, "xenoClashNightmareGroupNumber")
		xenoClashNightmarePartyNumber = SettingsFragment.getIntSharedPreference(myContext, "xenoClashNightmarePartyNumber")
		
		if (farmingMode == "Special" && missionName == "VH Angel Halo" && enableDimensionalHalo && (itemName == "EXP" || itemName == "Angel Halo Weapons")) {
			printToLog("\n[INFO] Initializing settings for Dimensional Halo...")
			
			if (dimensionalHaloSummonList.isEmpty()) {
				printToLog("[INFO] Summons for Dimensional Halo will reuse the ones for Farming Mode.")
				dimensionalHaloSummonList = summonList
			}
			
			if (dimensionalHaloGroupNumber == 0) {
				printToLog("[INFO] Group Number for Dimensional Halo will reuse the ones for Farming Mode.")
				dimensionalHaloGroupNumber = groupNumber
			}
			
			if (dimensionalHaloPartyNumber == 0) {
				printToLog("[INFO] Party Number for Dimensional Halo will reuse the ones for Farming Mode.")
				dimensionalHaloPartyNumber = partyNumber
			}
		} else if ((farmingMode == "Event" || farmingMode == "Event (Token Drawboxes)") && itemName == "Repeated Runs" && enableEventNightmare) {
			printToLog("\n[INFO] Initializing settings for Event Nightmare...")
			
			if (eventNightmareSummonList.isEmpty()) {
				printToLog("[INFO] Summons for Event Nightmare will reuse the ones for Farming Mode.")
				eventNightmareSummonList = summonList
			}
			
			if (eventNightmareGroupNumber == 0) {
				printToLog("[INFO] Group Number for Event Nightmare will reuse the ones for Farming Mode.")
				eventNightmareGroupNumber = groupNumber
			}
			
			if (eventNightmarePartyNumber == 0) {
				printToLog("[INFO] Party Number for Event Nightmare will reuse the ones for Farming Mode.")
				eventNightmarePartyNumber = partyNumber
			}
		} else if (farmingMode == "Rise of the Beasts" && itemName == "Repeated Runs" && enableROTBExtremePlus) {
			printToLog("\n[INFO] Initializing settings for Rise of the Beasts Extreme+...")
			
			if (rotbExtremePlusSummonList.isEmpty()) {
				printToLog("[INFO] Summons for Rise of the Beasts Extreme+ will reuse the ones for Farming Mode.")
				rotbExtremePlusSummonList = summonList
			}
			
			if (rotbExtremePlusGroupNumber == 0) {
				printToLog("[INFO] Group Number for Rise of the Beasts Extreme+ will reuse the ones for Farming Mode.")
				rotbExtremePlusGroupNumber = groupNumber
			}
			
			if (rotbExtremePlusPartyNumber == 0) {
				printToLog("[INFO] Party Number for Rise of the Beasts Extreme+ will reuse the ones for Farming Mode.")
				rotbExtremePlusPartyNumber = partyNumber
			}
		} else if (farmingMode == "Xeno Clash" && itemName == "Repeated Runs" && enableXenoClashNightmare) {
			printToLog("\n[INFO] Initializing settings for Xeno Clash Nightmare...")
			
			if (xenoClashNightmareSummonList.isEmpty()) {
				printToLog("[INFO] Summons for Xeno Clash Nightmare will reuse the ones for Farming Mode.")
				xenoClashNightmareSummonList = summonList
			}
			
			if (xenoClashNightmareGroupNumber == 0) {
				printToLog("[INFO] Group Number for Xeno Clash Nightmare will reuse the ones for Farming Mode.")
				xenoClashNightmareGroupNumber = groupNumber
			}
			
			if (xenoClashNightmarePartyNumber == 0) {
				printToLog("[INFO] Party Number for Xeno Clash Nightmare will reuse the ones for Farming Mode.")
				xenoClashNightmarePartyNumber = partyNumber
			}
		}
	}
	
	/**
	 * Start Farming Mode with the provided parameters from the user's choices in the settings.
	 *
	 * @param context: The context for the application.
	 * @return True if Farming Mode completed successfully. False otherwise.
	 */
	fun startFarmingMode(context: Context): Boolean {
		// Grab all necessary information from SharedPreferences.
		
		enableDelayBetweenRuns = SettingsFragment.getBooleanSharedPreference(context, "enableDelayBetweenRuns")
		delayBetweenRuns = SettingsFragment.getIntSharedPreference(context, "delayBetweenRuns")
		enableRandomizedDelayBetweenRuns = SettingsFragment.getBooleanSharedPreference(context, "enableRandomizedDelayBetweenRuns")
		randomizedDelayBetweenRuns = SettingsFragment.getIntSharedPreference(context, "randomizedDelayBetweenRuns")
		
		farmingMode = SettingsFragment.getStringSharedPreference(context, "farmingMode")
		mapName = SettingsFragment.getStringSharedPreference(context, "mapName")
		missionName = SettingsFragment.getStringSharedPreference(context, "missionName")
		itemName = SettingsFragment.getStringSharedPreference(context, "itemName")
		itemAmount = SettingsFragment.getIntSharedPreference(context, "itemAmount")
		combatScriptName = SettingsFragment.getStringSharedPreference(context, "combatScriptName")
		combatScript = SettingsFragment.getStringSharedPreference(context, "combatScript").split("|")
		summonList = SettingsFragment.getStringSharedPreference(context, "summon").split("|")
		groupNumber = SettingsFragment.getIntSharedPreference(context, "groupNumber")
		partyNumber = SettingsFragment.getIntSharedPreference(context, "partyNumber")
		
		if (farmingMode == "Raid") {
			twitterRoomFinder = TwitterRoomFinder(myContext, this)
		}
		
		mapSelection = MapSelection(this, twitterRoomFinder)
		
		if (itemName != "EXP") {
			printToLog("\n################################################################################")
			printToLog("################################################################################")
			printToLog("[FARM] Starting Farming Mode for $farmingMode.")
			printToLog("[FARM] Farming ${itemAmount}x $itemName at $missionName.")
			printToLog("################################################################################")
			printToLog("################################################################################")
		} else {
			printToLog("\n################################################################################")
			printToLog("################################################################################")
			printToLog("[FARM] Starting Farming Mode for $farmingMode.")
			printToLog("[FARM] Doing ${itemAmount}x runs for $itemName at $missionName.")
			printToLog("################################################################################")
			printToLog("################################################################################")
		}
		
		// Parse the difficulty for the chosen Mission.
		if (farmingMode == "Special" || farmingMode == "Event" || farmingMode == "Event (Token Drawboxes)" || farmingMode == "Rise of the Beasts") {
			when {
				missionName.indexOf("N ") == 0 -> {
					difficulty = "Normal"
				}
				missionName.indexOf("H ") == 0 -> {
					difficulty = "Hard"
				}
				missionName.indexOf("VH ") == 0 -> {
					difficulty = "Very Hard"
				}
				missionName.indexOf("EX ") == 0 -> {
					difficulty = "Extreme"
				}
				missionName.indexOf("IM ") == 0 -> {
					difficulty = "Impossible"
				}
			}
		} else if (farmingMode == "Dread Barrage") {
			when {
				missionName.indexOf("1 Star") == 0 -> {
					difficulty = "1 Star"
				}
				missionName.indexOf("2 Star") == 0 -> {
					difficulty = "2 Star"
				}
				missionName.indexOf("3 Star") == 0 -> {
					difficulty = "3 Star"
				}
				missionName.indexOf("4 Star") == 0 -> {
					difficulty = "4 Star"
				}
				missionName.indexOf("5 Star") == 0 -> {
					difficulty = "5 Star"
				}
			}
		} else if (farmingMode == "Guild Wars") {
			when {
				missionName.indexOf("Very Hard") == 0 -> {
					difficulty = "Very Hard"
				}
				missionName.indexOf("Extreme+") == 0 -> {
					difficulty = "Extreme+"
				}
				missionName.indexOf("Extreme") == 0 -> {
					difficulty = "Extreme"
				}
				missionName.indexOf("NM90") == 0 -> {
					difficulty = "NM90"
				}
				missionName.indexOf("NM95") == 0 -> {
					difficulty = "NM95"
				}
				missionName.indexOf("NM100") == 0 -> {
					difficulty = "NM100"
				}
				missionName.indexOf("NM150") == 0 -> {
					difficulty = "NM150"
				}
			}
		}
		
		// Perform advanced setup for the special fights like Dimensional Halo, Event Nightmares, and Dread Barrage's Unparalleled Foes.
		advancedSetup()
		
		// If the user did not select a combat script, use the default Full Auto combat script.
		if (combatScript.isEmpty() || combatScript[0] == "") {
			printToLog("\n[INFO] User did not provide their own combat script. Defaulting to Full Auto combat script.")
			
			combatScript = listOf(
				"Turn 1:",
				"enableFullAuto",
				"end"
			)
		}
		
		val eventQuests = arrayListOf("N Event Quest", "H Event Quest", "VH Event Quest", "EX Event Quest")
		var startCheckFlag = false
		var summonCheckFlag: Boolean
		
		printToLog("\n[INFO] Now selecting the Mission...")
		
		if (farmingMode != "Raid") {
			mapSelection.selectMap(farmingMode, mapName, missionName, difficulty)
		} else {
			mapSelection.joinRaid(missionName)
		}
		
		// Primary workflow loop for Farming Mode.
		while (itemAmountFarmed < itemAmount) {
			// Reset the Summon Selection flag.
			summonCheckFlag = false
			
			// Loop and attempt to select a Summon. Reset Summons if necessary.
			while (!summonCheckFlag && farmingMode != "Coop") {
				summonCheckFlag = selectSummon()
				
				// If the return came back as false, that means the Summons were reset.
				if (!summonCheckFlag && farmingMode != "Raid") {
					printToLog("\n[INFO] Selecting Mission again after resetting Summons.")
					mapSelection.selectMap(farmingMode, mapName, missionName, difficulty)
				} else if (!summonCheckFlag && farmingMode == "Raid") {
					printToLog("\n[INFO] Joining Raids again after resetting Summons.")
					mapSelection.joinRaid(missionName)
				}
			}
			
			// Perform Party Selection and then start the Mission. If Farming Mode is Coop, skip this as Coop reuses the same Party.
			if (farmingMode != "Coop" && farmingMode != "Proving Grounds") {
				startCheckFlag = selectPartyAndStartMission()
			} else if (farmingMode == "Coop" && coopFirstRun) {
				startCheckFlag = selectPartyAndStartMission()
				coopFirstRun = false
				
				// Click the "Start" button to start the Coop Mission.
				findAndClickButton("coop_start")
			} else if (farmingMode == "Coop" && !coopFirstRun) {
				printToLog("\n[INFO] Starting Coop Mission again.")
				startCheckFlag = true
			} else if (farmingMode == "Proving Grounds") {
				// Parties are assumed to have already been formed by the player prior to starting. In addition, no need to select a Summon again as it is reused.
				if (provingGroundsFirstRun) {
					checkAP()
					findAndClickButton("ok")
					provingGroundsFirstRun = false
				}
				
				startCheckFlag = true
			}
			
			if (startCheckFlag && farmingMode != "Raid") {
				wait(3.0)
				
				// Check for "Items Picked Up" popup that appears after starting a Quest Mission.
				if (farmingMode == "Quest" && imageUtils.confirmLocation("items_picked_up", tries = 1)) {
					findAndClickButton("ok")
				}
				
				// Finally, start Combat Mode. If it ended successfully, detect loot and do it again if necessary.
				if (combatMode.startCombatMode(combatScript)) {
					// If it ended successfully, detect loot and repeat if acquired item amount has not been reached.
					collectLoot()
					
					if (itemAmountFarmed < itemAmount) {
						// Generate a resting period if the user enabled it.
						delayBetweenRuns()
						
						if (farmingMode != "Coop" && farmingMode != "Proving Grounds" && !findAndClickButton("play_again")) {
							// Clear away any Pending Battles.
							mapSelection.checkPendingBattles(farmingMode)
							
							// Now that Pending Battles have been cleared away, select the Mission again.
							mapSelection.selectMap(farmingMode, mapName, missionName, difficulty)
						} else if (farmingMode == "Event (Token Drawboxes)" && eventQuests.contains(missionName)) {
							// Select the Mission again since Event Quests do not have "Play Again" functionality.
							mapSelection.selectMap(farmingMode, mapName, missionName, difficulty)
						} else if (farmingMode == "Coop") {
							// Head back to the Coop Room.
							findAndClickButton("coop_room")
							
							wait(1.0)
							
							// Check for "Daily Missions" popup for Coop.
							if (imageUtils.confirmLocation("coop_daily_missions", tries = 1)) {
								findAndClickButton("close")
							}
							
							wait(1.0)
							
							// Now that the bot is back at the Coop Room, check if it is closed due to time running out.
							if (imageUtils.confirmLocation("coop_room_closed", tries = 1)) {
								printToLog("\n[INFO] Coop room has closed due to time running out.")
								break
							}
							
							// Now start the Coop Mission again.
							findAndClickButton("coop_start")
							
							wait(1.0)
						} else if (farmingMode == "Proving Grounds") {
							// Tap the "Next Battle" button if there are any battles left.
							if (findAndClickButton("proving_grounds_next_battle", suppressError = true)) {
								printToLog("\n[INFO] Moving onto the next battle for Proving Grounds...")
								
								// Then tap the "OK" button to play the next battle.
								findAndClickButton("ok")
							} else {
								// Otherwise, all battles for the Mission has been completed. Collect the Completion Rewards at the end.
								printToLog("\n[INFO] Proving Grounds Mission has been completed.")
								findAndClickButton("event")
								
								wait(2.0)
								
								findAndClickButton("proving_grounds_open_chest", tries = 5)
								
								if (imageUtils.confirmLocation("proving_grounds_completion_loot")) {
									printToLog("\n[INFO] Completion rewards has been acquired.")
									
									// Reset the First Time flag so the bot can select a Summon and select the Mission again.
									if (itemAmountFarmed < itemAmount) {
										printToLog("\\n[INFO] Starting Proving Grounds Mission again...")
										provingGroundsFirstRun = true
										findAndClickButton("play_again")
									}
								}
							}
						}
						
						// For every other Farming Mode other than Coop and Proving Grounds, handle all popups and perform AP check until the bot reaches the Summon Selection screen.
						if (farmingMode != "Proving Grounds") {
							checkForPopups()
							checkAP()
						}
					}
				} else {
					// Restart the Mission if the Party wiped or exited prematurely during Combat Mode.
					printToLog("\n[INFO] Restarting the Mission due to retreating...")
					mapSelection.selectMap(farmingMode, mapName, missionName, difficulty)
				}
			} else if (startCheckFlag && farmingMode == "Raid") {
				// Cover the occasional case where joining the Raid after selecting the Summon and Party led to the Quest Results screen with no loot to collect.
				if (imageUtils.confirmLocation("no_loot", tries = 1)) {
					printToLog("\n[INFO] Seems that the Raid just ended. Moving back to the Home screen and joining another Raid...")
					goBackHome(confirmLocationCheck = true)
				} else {
					// At this point, the Summon and Party have already been selected and the Mission has started. Start Combat Mode.
					if (combatMode.startCombatMode(combatScript)) {
						collectLoot()
						
						if (itemAmountFarmed < itemAmount) {
							// Generate a resting period if the user enabled it.
							delayBetweenRuns()
							
							// Clear away any Pending Battles.
							mapSelection.checkPendingBattles(farmingMode)
							
							// Now join a new Raid.
							mapSelection.joinRaid(missionName)
						}
					} else {
						// Join a new Raid.
						mapSelection.joinRaid(missionName)
					}
				}
			} else if (!startCheckFlag && farmingMode == "Raid") {
				// If the bot reaches here, that means that the Raid ended before the bot could start the Mission after selecting the Summon and Party.
				printToLog("\n[INFO] Seems that the Raid ended before the bot was able to join. Now looking for another Raid to join...")
				mapSelection.joinRaid(missionName)
			} else if (!startCheckFlag) {
				throw Exception("Failed to arrive at the Summon Selection screen after selecting the Mission.")
			}
		}
		
		printToLog("\n********************************************************************************")
		printToLog("********************************************************************************")
		printToLog("[INFO] Farming Mode has ended")
		printToLog("********************************************************************************")
		printToLog("********************************************************************************")
		
		return true
	}
}