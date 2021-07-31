package chylex.hee.init

import chylex.hee.game.territory.TerritoryType

object ModLanguage {
	val GENERIC
		get() = mapOf(
			"itemGroup.hee" to "Hardcore Ender Expansion",
			TerritoryType.FALLBACK_TRANSLATION_KEY to "Unknown",
			
			"gui.hee.amulet_of_recovery.move_all" to "Move All",
			"gui.hee.loot_chest.title" to "Loot Chest",
			"gui.hee.loot_chest.title.creative" to "Loot Chest (Editing)",
			"gui.hee.portal_token_storage.title" to "Portal Tokens",
			"gui.hee.enhanced_brewing_stand.title" to "Enhanced Brewing Stand",
			
			"hee.infusions.list.title" to "§aInfusions",
			"hee.infusions.list.item" to "§2- %s",
			"hee.infusions.list.none" to "§7None",
			"hee.infusions.applicable.title" to "§aApplicable To",
			"hee.infusions.applicable.item" to "§2- %s §3[%s]",
			
			"hee.energy.overlay.health" to "%s ENERGY CLUSTER",
			"hee.energy.overlay.level" to "HOLDING %s OUT OF %s ENERGY",
			"hee.energy.overlay.ignored" to "IGNORED",
		)
}
