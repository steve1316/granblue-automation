package com.steve1316.granblueautomation_android.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.sksamuel.hoplite.ConfigLoader
import com.steve1316.granblueautomation_android.MainActivity
import com.steve1316.granblueautomation_android.R
import com.steve1316.granblueautomation_android.data.ConfigData
import com.steve1316.granblueautomation_android.ui.settings.SettingsFragment
import com.steve1316.granblueautomation_android.utils.MediaProjectionService
import com.steve1316.granblueautomation_android.utils.MessageLog
import com.steve1316.granblueautomation_android.utils.MyAccessibilityService
import java.io.File

class HomeFragment : Fragment() {
	private val TAG: String = "[${MainActivity.loggerTag}]HomeFragment"
	private val SCREENSHOT_PERMISSION_REQUEST_CODE: Int = 100
	private var firstBoot = false
	
	private lateinit var myContext: Context
	private lateinit var homeFragmentView: View
	private lateinit var startButton: Button
	
	@SuppressLint("SetTextI18n")
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		myContext = requireContext()
		
		homeFragmentView = inflater.inflate(R.layout.fragment_home, container, false)
		
		// Start or stop the MediaProjection service via this button.
		startButton = homeFragmentView.findViewById(R.id.start_button)
		startButton.setOnClickListener {
			val readyCheck = startReadyCheck()
			if (readyCheck && !MediaProjectionService.isRunning) {
				startProjection()
				startButton.text = getString(R.string.bot_stop)
				
				// This is needed because onResume() is immediately called right after accepting the MediaProjection and it has not been properly
				// initialized yet so it would cause the button's text to revert back to "Start".
				firstBoot = true
			} else if (MediaProjectionService.isRunning) {
				stopProjection()
				startButton.text = getString(R.string.bot_start)
			}
		}
		
		// Check if the application created the config.yaml file yet and if not, create it.
		val file = File(myContext.getExternalFilesDir(null), "config.yaml")
		if (!file.exists()) {
			file.createNewFile()
			
			val content = "---\n" +
					"########################################\n" +
					"# Read the instructions on the GitHub repository README.md on how to get these keys in order to allow the bot to farm Raids via Twitter.\n" +
					"########################################\n" +
					"\"twitter\":\n" +
					"  \"apiKey\": \n" +
					"  \"apiKeySecret\": \n" +
					"  \"accessToken\": \n" +
					"  \"accessTokenSecret\": \n" +
					"\n" +
					"########################################\n" +
					"# Enable using Full Elixir or Soul Balms for refill.\n" +
					"########################################\n" +
					"\"refill\":\n" +
					"  \"fullElixir\": false\n" +
					"  \"soulBalm\": false\n" +
					"\n" +
					"############################################################\n" +
					"# The following settings below follow pretty much the same template provided. They default to the settings selected for Farming Mode if nothing is set.\n" +
					"\n" +
					"# Enables this fight or skip it if false.\n" +
					"# enable___ =\n" +
					"\n" +
					"# Select what Summon(s) separated by commas to use in order from highest priority to least. Example: Shiva, Colossus Omega, Varuna, Agni\n" +
					"# https://github.com/steve1316/granblue-automation-pyautogui/wiki/Selectable-Summons\n" +
					"# ___SummonList =\n" +
					"\n" +
					"# Set what Party to select and under what Group to run for the specified fight. Accepted values are: Group [1, 2, 3, 4, 5, 6, 7] and Party [1, 2, 3, 4, 5, 6].\n" +
					"# ___GroupNumber =\n" +
					"# ___PartyNumber =\n" +
					"############################################################\n" +
					"\n" +
					"########################################\n" +
					"# Settings for Dimensional Halo.\n" +
					"########################################\n" +
					"\"dimensionalHalo\":\n" +
					"  \"enableDimensionalHalo\": false\n" +
					"  \"dimensionalHaloSummonList\": []\n" +
					"  \"dimensionalHaloGroupNumber\": 0\n" +
					"  \"dimensionalHaloPartyNumber\": 0\n" +
					"\n" +
					"########################################\n" +
					"# Settings for Event Nightmares.\n" +
					"########################################\n" +
					"\"event\":\n" +
					"  \"enableEventNightmare\": false\n" +
					"  \"eventNightmareSummonList\": []\n" +
					"  \"eventNightmareGroupNumber\": 0\n" +
					"  \"eventNightmarePartyNumber\": 0\n" +
					"\n" +
					"########################################\n" +
					"# Settings for Rise of the Beasts Extreme+.\n" +
					"########################################\n" +
					"\"rotb\":\n" +
					"  \"enableROTBExtremePlus\": false\n" +
					"  \"rotbExtremePlusSummonList\": []\n" +
					"  \"rotbExtremePlusGroupNumber\": 0\n" +
					"  \"rotbExtremePlusPartyNumber\": 0\n" +
					"\n" +
					"########################################\n" +
					"# Settings for Xeno Clash Nightmare.\n" +
					"########################################\n" +
					"\"xenoClash\":\n" +
					"  \"enableXenoClashNightmare\": false\n" +
					"  \"xenoClashNightmareSummonList\": []\n" +
					"  \"xenoClashNightmareGroupNumber\": 0\n" +
					"  \"xenoClashNightmarePartyNumber\": 0"
			
			file.writeText(content)
			
			Log.d(TAG, "Created config.yaml in internal storage.")
		} else {
			Log.d(TAG, "config.yaml already exists.")
			
			val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
			
			// Save the Twitter API keys and tokens and every other settings in the config.yaml to SharedPreferences.
			try {
				if (file.exists()) {
					val config = ConfigLoader().loadConfigOrThrow<ConfigData>(file)
					
					if (config.twitter.apiKey != sharedPreferences.getString("apiKey", "")) {
						sharedPreferences.edit {
							putString("apiKey", config.twitter.apiKey)
							putString("apiKeySecret", config.twitter.apiKeySecret)
							putString("accessToken", config.twitter.accessToken)
							putString("accessTokenSecret", config.twitter.accessTokenSecret)
							commit()
						}
						
						Log.d(TAG, "Saved Twitter API credentials to SharedPreferences from config.")
					}
					
					sharedPreferences.edit {
						putBoolean("enableEventNightmare", config.event.enableEventNightmare)
						putStringSet("eventNightmareSummonList", config.event.eventNightmareSummonList.toMutableSet())
						putInt("eventNightmareGroupNumber", config.event.eventNightmareGroupNumber)
						putInt("eventNightmarePartyNumber", config.event.eventNightmarePartyNumber)
						
						putBoolean("enableDimensionalHalo", config.dimensionalHalo.enableDimensionalHalo)
						putStringSet("dimensionalHaloSummonList", config.dimensionalHalo.dimensionalHaloSummonList.toMutableSet())
						putInt("dimensionalHaloGroupNumber", config.dimensionalHalo.dimensionalHaloGroupNumber)
						putInt("dimensionalHaloPartyNumber", config.dimensionalHalo.dimensionalHaloPartyNumber)
						
						putBoolean("enableROTBExtremePlus", config.rotb.enableROTBExtremePlus)
						putStringSet("rotbExtremePlusSummonList", config.rotb.rotbExtremePlusSummonList.toMutableSet())
						putInt("rotbExtremePlusGroupNumber", config.rotb.rotbExtremePlusGroupNumber)
						putInt("rotbExtremePlusPartyNumber", config.rotb.rotbExtremePlusPartyNumber)
						
						putBoolean("enableXenoClashNightmare", config.rotb.enableROTBExtremePlus)
						putStringSet("xenoClashNightmareSummonList", config.rotb.rotbExtremePlusSummonList.toMutableSet())
						putInt("xenoClashNightmareGroupNumber", config.rotb.rotbExtremePlusGroupNumber)
						putInt("xenoClashNightmarePartyNumber", config.rotb.rotbExtremePlusPartyNumber)
						commit()
					}
					
					Log.d(TAG, "Saved config.ini settings to SharedPreferences.")
				}
			} catch (e: Exception) {
				Log.e(TAG, "Encountered error while saving Twitter API credentials to SharedPreferences from config: ${e.stackTraceToString()}")
				Log.e(TAG, "Clearing any existing Twitter API credentials from SharedPreferences...")
				
				sharedPreferences.edit {
					remove("apiKey")
					remove("apiKeySecret")
					remove("accessToken")
					remove("accessTokenSecret")
					commit()
				}
			}
		}
		
		// Update the TextView here based on the information of the SharedPreferences. Required preferences to check for are Farming Mode, Mission,
		// Item, and Summons. The rest are already given default values if the user never set them. Except Combat Script as if it is not set by the
		// user, set it for them to be Full/Semi Auto by default.
		val settingsStatusTextView: TextView = homeFragmentView.findViewById(R.id.settings_status)
		
		val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
		
		var combatScriptName = SettingsFragment.getStringSharedPreference(myContext, "combatScriptName")
		var farmingMode = SettingsFragment.getStringSharedPreference(myContext, "farmingMode")
		val mapName = SettingsFragment.getStringSharedPreference(myContext, "mapName")
		var missionName = SettingsFragment.getStringSharedPreference(myContext, "missionName")
		var itemName = SettingsFragment.getStringSharedPreference(myContext, "itemName")
		val itemAmount = SettingsFragment.getIntSharedPreference(myContext, "itemAmount")
		var summon = SettingsFragment.getStringSharedPreference(myContext, "summon").split("|")
		val groupNumber = SettingsFragment.getIntSharedPreference(myContext, "groupNumber")
		val partyNumber = SettingsFragment.getIntSharedPreference(myContext, "partyNumber")
		val debugModePreferences = sharedPreferences.getBoolean("debugMode", false)
		val enableDelayBetweenRunsPreferences = sharedPreferences.getBoolean("enableDelayBetweenRuns", false)
		val enableRandomizedDelayBetweenRunsPreferences = sharedPreferences.getBoolean("enableRandomizedDelayBetweenRuns", false)
		val delayBetweenRunsPreferences = sharedPreferences.getInt("delayBetweenRuns", 1)
		val randomizedDelayBetweenRunsPreferences = sharedPreferences.getInt("randomizedDelayBetweenRuns", 1)
		
		startButton.isEnabled = (farmingMode != "" && missionName != "" && itemName != "" && ((farmingMode != "Coop" && summon.isNotEmpty() && summon[0] != "") ||
				(farmingMode == "Coop" && summon.isNotEmpty() && summon[0] == "")))
		
		if (combatScriptName == "") {
			combatScriptName = "None selected. Using default Semi/Full Auto script."
		}
		
		if (farmingMode == "") {
			farmingMode = "Missing"
		}
		
		val mapString: String = if (mapName != "") {
			"Map: $mapName"
		} else {
			""
		}
		
		if (missionName == "") {
			missionName = "Missing"
		}
		
		if (itemName == "") {
			itemName = "Missing"
		}
		
		if (summon[0] == "" && farmingMode != "Coop") {
			summon = listOf("Requires at least 1 Summon")
		}
		
		val enableDebugModeString: String = if (debugModePreferences) {
			"Enabled"
		} else {
			"Disabled"
		}
		
		val enableDelayBetweenRunsString: String = if (enableDelayBetweenRunsPreferences) {
			"Enabled"
		} else {
			"Disabled"
		}
		
		val enableRandomizedDelayBetweenRunsString: String = if (enableRandomizedDelayBetweenRunsPreferences) {
			"Enabled"
		} else {
			"Disabled"
		}
		
		settingsStatusTextView.setTextColor(Color.WHITE)
		settingsStatusTextView.text = "---------- Farming Mode Settings ----------\n" +
				"Mode: $farmingMode\n" +
				mapString +
				"Mission: $missionName\n" +
				"Item: x$itemAmount $itemName\n" +
				"---------- Combat Mode Settings ----------\n" +
				"Combat Script: $combatScriptName\n" +
				"Summon: $summon\n" +
				"Group: $groupNumber\n" +
				"Party: $partyNumber\n" +
				"---------- Misc Settings ----------\n" +
				"Debug Mode: $enableDebugModeString\n" +
				"Delay Between Runs: $enableDelayBetweenRunsString\n" +
				"Randomized Between Runs: $enableRandomizedDelayBetweenRunsString\n" +
				"Delay Between Runs Lower Bound: $delayBetweenRunsPreferences seconds\n" +
				"Delay Between Runs Upper Bound: $randomizedDelayBetweenRunsPreferences seconds"
		
		return homeFragmentView
	}
	
	override fun onResume() {
		super.onResume()
		
		// Update the button's text depending on if the MediaProjection service is running.
		if (!firstBoot) {
			if (MediaProjectionService.isRunning) {
				startButton.text = getString(R.string.bot_stop)
			} else {
				startButton.text = getString(R.string.bot_start)
			}
		}
		
		// Setting this false here will ensure that stopping the MediaProjection Service outside of this application will update this button's text.
		firstBoot = false
		
		// Now update the Message Log inside the ScrollView with the latest logging messages from the bot.
		Log.d(TAG, "Now updating the Message Log TextView...")
		val messageLogTextView = homeFragmentView.findViewById<TextView>(R.id.message_log)
		messageLogTextView.text = ""
		var index = 0
		
		// Get local copies of the message log.
		val messageLog = MessageLog.messageLog
		val messageLogSize = MessageLog.messageLog.size
		while (index < messageLogSize) {
			messageLogTextView.append("\n" + messageLog[index])
			index += 1
		}
		
		// Set up the app updater to check for the latest update from GitHub.
		AppUpdater(myContext)
			.setUpdateFrom(UpdateFrom.XML)
			.setUpdateXML("https://raw.githubusercontent.com/steve1316/granblue-automation-android/main/app/update.xml")
			.start()
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == SCREENSHOT_PERMISSION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			// Start up the MediaProjection service after the user accepts the onscreen prompt.
			myContext.startService(data?.let { MediaProjectionService.getStartIntent(myContext, resultCode, data) })
		}
	}
	
	/**
	 * Checks to see if the application is ready to start.
	 *
	 * @return True if the application has overlay permission and has enabled the Accessibility Service for it. Otherwise, return False.
	 */
	private fun startReadyCheck(): Boolean {
		if (!checkForOverlayPermission() || !checkForAccessibilityPermission()) {
			return false
		}
		
		return true
	}
	
	/**
	 * Starts the MediaProjection Service.
	 */
	private fun startProjection() {
		val mediaProjectionManager = context?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
		startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), SCREENSHOT_PERMISSION_REQUEST_CODE)
	}
	
	/**
	 * Stops the MediaProjection Service.
	 */
	private fun stopProjection() {
		context?.startService(MediaProjectionService.getStopIntent(requireContext()))
	}
	
	/**
	 * Checks if the application has permission to draw overlays. If not, it will direct the user to enable it.
	 *
	 * Source is from https://github.com/Fate-Grand-Automata/FGA/blob/master/app/src/main/java/com/mathewsachin/fategrandautomata/ui/MainFragment.kt
	 *
	 * @return True if it has permission. False otherwise.
	 */
	private fun checkForOverlayPermission(): Boolean {
		if (!Settings.canDrawOverlays(requireContext())) {
			Log.d(TAG, "Application is missing overlay permission.")
			
			AlertDialog.Builder(requireContext()).apply {
				setTitle(R.string.overlay_disabled)
				setMessage(R.string.overlay_disabled_message)
				setPositiveButton(R.string.go_to_settings) { _, _ ->
					// Send the user to the Overlay Settings.
					val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireContext().packageName}"))
					startActivity(intent)
				}
				setNegativeButton(android.R.string.cancel, null)
			}.show()
			
			return false
		}
		
		Log.d(TAG, "Application has permission to draw overlay.")
		return true
	}
	
	/**
	 * Checks if the Accessibility Service for this application is enabled. If not, it will direct the user to enable it.
	 *
	 * Source is from https://stackoverflow.com/questions/18094982/detect-if-my-accessibility-service-is-enabled/18095283#18095283
	 *
	 * @return True if it is enabled. False otherwise.
	 */
	private fun checkForAccessibilityPermission(): Boolean {
		val prefString = Settings.Secure.getString(myContext.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
		
		if (prefString != null && prefString.isNotEmpty()) {
			// Check the string of enabled accessibility services to see if this application's accessibility service is there.
			val enabled = prefString.contains(myContext.packageName.toString() + "/" + MyAccessibilityService::class.java.name)
			
			if (enabled) {
				Log.d(TAG, "This application's Accessibility Service is currently turned on.")
				return true
			}
		}
		
		// Moves the user to the Accessibility Settings if the service is not detected.
		AlertDialog.Builder(myContext).apply {
			setTitle(R.string.accessibility_disabled)
			setMessage(R.string.accessibility_disabled_message)
			setPositiveButton(R.string.go_to_settings) { _, _ ->
				Log.d(TAG, "Accessibility Service is not detected. Moving user to Accessibility Settings.")
				val accessibilitySettingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
				myContext.startActivity(accessibilitySettingsIntent)
			}
			setNegativeButton(android.R.string.cancel, null)
			show()
		}
		
		return false
	}
}
