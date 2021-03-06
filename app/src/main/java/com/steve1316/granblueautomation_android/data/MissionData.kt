package com.steve1316.granblueautomation_android.data

/**
 * This class simply holds the Missions and associated Maps if applicable that are supported by this application.
 */
class MissionData {
	companion object {
		val mapsForQuest = mapOf(
			"Port Breeze Archipelago" to arrayListOf("Scattered Cargo"),
			"Valtz Duchy" to arrayListOf("Lucky Charm Hunt", "Special Op's Request"),
			"Auguste Isles" to arrayListOf("Threat to the Fisheries"),
			"Lumacie Archipelago" to arrayListOf("The Fruit of Lumacie", "Whiff of Danger"),
			"Albion Citadel" to arrayListOf("I Challenge You!"),
			"Mist-Shrouded Isle" to arrayListOf("For Whom the Bell Tolls"),
			"Golonzo Island" to arrayListOf("Golonzo's Battles of Old"),
			"Amalthea Island" to arrayListOf("The Dungeon Diet"),
			"Former Capital Mephorash" to arrayListOf("Trust Busting Dustup", "Erste Kingdom Episode 4"),
			"Agastia" to arrayListOf("Imperial Wanderer's Soul")
		)
		
		val mapsForSpecial = mapOf(
			"Basic Treasure Quests" to arrayListOf(
				"N Scarlet Trial", "H Scarlet Trial", "VH Scarlet Trial", "N Cerulean Trial", "H Cerulean Trial",
				"VH Cerulean Trial", "N Violet Trial", "H Violet Trial", "VH Violet Trial"
			),
			"Shiny Slime Search!" to arrayListOf("N Shiny Slime Search!", "H Shiny Slime Search!", "VH Shiny Slime Search!"),
			"Six Dragon Trial" to arrayListOf("N Six Dragon Trial", "H Six Dragon Trial", "VH Six Dragon Trial"),
			"Elemental Treasure Quests" to arrayListOf("The Hellfire Trial", "The Deluge Trial", "The Wasteland Trial", "The Typhoon Trial"),
			"Showdowns" to arrayListOf("EX Ifrit Showdown", "EX Cocytus Showdown", "EX Vohu Manah Showdown", "EX Sagittarius Showdown", "EX Corow Showdown", "EX Diablo Showdown"),
			"Angel Halo" to arrayListOf("N Angel Halo", "H Angel Halo", "VH Angel Halo"),
			"Campaign-Exclusive Quest" to arrayListOf("Campaign-Exclusive Quest")
		)
		
		val mapsForRaid = mapOf(
			"Tiamat Omega" to arrayListOf("Lvl 50 Tiamat Omega", "Lvl 100 Tiamat Omega Ayr"),
			"Colossus Omega" to arrayListOf("Lvl 70 Colossus Omega", "Lvl 100 Colossus Omega"),
			"Leviathan Omega" to arrayListOf("Lvl 60 Leviathan Omega", "Lvl 100 Leviathan Omega"),
			"Yggdrasil Omega" to arrayListOf("Lvl 60 Yggdrasil Omega", "Lvl 100 Yggdrasil Omega"),
			"Luminiera Omega" to arrayListOf("Lvl 75 Luminiera Omega", "Lvl 100 Luminiera Omega"),
			"Celeste Omega" to arrayListOf("Lvl 75 Celeste Omega", "Lvl 100 Celeste Omega"),
			
			"Shiva" to arrayListOf("Lvl 120 Shiva"),
			"Europa" to arrayListOf("Lvl 120 Europa"),
			"Godsworn Alexiel" to arrayListOf("Lvl 120 Godsworn Alexiel"),
			"Grimnir" to arrayListOf("Lvl 120 Grimnir"),
			"Metatron" to arrayListOf("Lvl 120 Metatron"),
			"Avatar" to arrayListOf("Lvl 120 Avatar"),
			
			"Michael" to arrayListOf("Lvl 100 Michael"),
			"Gabriel" to arrayListOf("Lvl 100 Gabriel"),
			"Uriel" to arrayListOf("Lvl 100 Uriel"),
			"Raphael" to arrayListOf("Lvl 100 Raphael"),
			"The Four Primarchs" to arrayListOf("The Four Primarchs"),
			
			"Grand Order" to arrayListOf("Lvl 100 Grand Order", "Lvl 200 Grand Order"),
			"Proto Bahamut" to arrayListOf("Lvl 100 Proto Bahamut", "Lvl 150 Proto Bahamut"),
			"Ultimate Bahamut" to arrayListOf("Lvl 150 Ultimate Bahamut", "Lvl 200 Ultimate Bahamut"),
			"Rose Queen" to arrayListOf("Lvl 110 Rose Queen"),
			"Akasha" to arrayListOf("Lvl 200 Akasha"),
			"Lucilius" to arrayListOf("Lvl 150 Lucilius"),
			"Lindwurm" to arrayListOf("Lvl 200 Lindwurm"),
			
			"Twin Elements" to arrayListOf("Lvl 100 Twin Elements", "Lvl 120 Twin Elements"),
			"Macula Marius" to arrayListOf("Lvl 100 Macula Marius", "Lvl 120 Macula Marius"),
			"Medusa" to arrayListOf("Lvl 100 Medusa", "Lvl 120 Medusa"),
			"Nezha" to arrayListOf("Lvl 100 Nezha", "Lvl 120 Nezha"),
			"Apollo" to arrayListOf("Lvl 100 Apollo", "Lvl 120 Apollo"),
			"Dark Angel Olivia" to arrayListOf("Lvl 100 Dark Angel Olivia", "Lvl 120 Dark Angel Olivia"),
			
			"Athena" to arrayListOf("Lvl 100 Athena"),
			"Grani" to arrayListOf("Lvl 100 Grani"),
			"Baal" to arrayListOf("Lvl 100 Baal"),
			"Garuda" to arrayListOf("Lvl 100 Garuda"),
			"Odin" to arrayListOf("Lvl 100 Odin"),
			"Lich" to arrayListOf("Lvl 100 Lich"),
			
			"Prometheus" to arrayListOf("Lvl 120 Prometheus"),
			"Ca Ong" to arrayListOf("Lvl 120 Ca Ong"),
			"Gilgamesh" to arrayListOf("Lvl 120 Gilgamesh"),
			"Morrigna" to arrayListOf("Lvl 120 Morrigna"),
			"Hector" to arrayListOf("Lvl 120 Hector"),
			"Anubis" to arrayListOf("Lvl 120 Anubis"),
			
			"Tiamat Malice" to arrayListOf("Lvl 150 Tiamat Malice"),
			"Leviathan Malice" to arrayListOf("Lvl 150 Leviathan Malice"),
			"Phronesis" to arrayListOf("Lvl 150 Phronesis"),
			"Luminiera Malice" to arrayListOf("Lvl 150 Luminiera Malice"),
			
			"Wilnas" to arrayListOf("Lvl 200 Wilnas"),
			"Wamdus" to arrayListOf("Lvl 200 Wamdus"),
			"Galleon" to arrayListOf("Lvl 200 Galleon"),
			"Ewiyar" to arrayListOf("Lvl 200 Ewiyar"),
			"Lu Woh" to arrayListOf("Lvl 200 Lu Woh"),
			"Fediel" to arrayListOf("Lvl 200 Fediel"),
			
			"Xeno Ifrit" to arrayListOf("Lvl 100 Xeno Ifrit"),
			"Xeno Cocytus" to arrayListOf("Lvl 100 Xeno Cocytus"),
			"Xeno Vohu Manah" to arrayListOf("Lvl 100 Xeno Vohu Manah"),
			"Xeno Sagittarius" to arrayListOf("Lvl 100 Xeno Sagittarius"),
			"Xeno Corow" to arrayListOf("Lvl 100 Xeno Corow"),
			"Xeno Diablo" to arrayListOf("Lvl 100 Xeno Diablo"),
		)
		
		val mapsForEvent = mapOf(
			"Event Quest" to arrayListOf("EX Event Quest"),
			"Event Raid" to arrayListOf("VH Event Raid", "EX Event Raid")
		)
		
		val mapsForEventTokenDrawboxes = mapOf(
			"Event Quest" to arrayListOf("EX Event Quest"),
			"Event Raid" to arrayListOf("VH Event Raid", "EX Event Raid", "IM Event Raid")
		)
		
		val mapsForROTB = mapOf(
			"Event Quest" to arrayListOf("VH Zhuque", "VH Xuanwu", "VH Baihu", "VH Qinglong"),
			"Event Raid" to arrayListOf("EX Zhuque", "EX Xuanwu", "EX Baihu", "EX Qinglong")
		)
		
		val mapsForGuildWars = mapOf(
			"Nightmare" to arrayListOf("NM90", "NM95", "NM100", "NM150"),
			"Meat" to arrayListOf("Extreme", "Extreme+")
		)
		
		val mapsForDreadBarrage = mapOf(
			"Dread Barrage Raid" to arrayListOf("1 Star", "2 Star", "3 Star", "4 Star", "5 Star")
		)
		
		val mapsForProvingGrounds = mapOf(
			"Proving Ground Missions" to arrayListOf("Extreme", "Extreme+")
		)
		
		val mapsForXenoClash = mapOf(
			"Xeno Clash Extreme" to arrayListOf("Xeno Clash Extreme"),
			"Xeno Clash Raid" to arrayListOf("Xeno Clash Raid")
		)
	}
}