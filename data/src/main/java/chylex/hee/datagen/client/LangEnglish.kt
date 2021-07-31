package chylex.hee.datagen.client

import chylex.hee.HEE
import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.IHeeBlock
import chylex.hee.game.command.ClientCommandHandler
import chylex.hee.game.item.IHeeItem
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthStatus
import chylex.hee.game.potion.IHeeEffect
import chylex.hee.game.territory.TerritoryType
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModCommands
import chylex.hee.init.ModEffects
import chylex.hee.init.ModEntities
import chylex.hee.init.ModItems
import chylex.hee.init.ModLanguage
import chylex.hee.init.ModPotions
import chylex.hee.init.ModSounds
import chylex.hee.system.getRegistryEntries
import chylex.hee.system.path
import net.minecraft.data.DataGenerator
import net.minecraft.potion.Effect
import net.minecraft.potion.Potion
import net.minecraftforge.common.data.LanguageProvider

class LangEnglish(generator: DataGenerator, modid: String) : LanguageProvider(generator, modid, "en_us") {
	private val addedKeys = mutableMapOf<String, String>()
	
	override fun addTranslations() {
		for (block in ModBlocks.ALL) {
			if (block is IHeeBlock) {
				block.localization.localize(block.translationKey.removePrefix("block.hee."))?.let { add(block, it) }
				addAll(block.localizationExtra)
			}
		}
		
		for (item in ModItems.ALL) {
			if (item is IHeeItem) {
				item.localization.localize(item.translationKey.removePrefix("item.hee.").removePrefix("block.hee."))?.let { add(item, it) }
				addAll(item.localizationExtra)
			}
		}
		
		for (fluid in ModBlocks.FLUIDS) {
			add("fluid.hee." + fluid.registryName, fluid.localizedName)
		}
		
		for ((type, properties) in ModEntities.ALL) {
			properties.localization.localize(type.translationKey.removePrefix("entity.hee."))?.let { add(type, it) }
		}
		
		for ((sound, localized) in ModSounds.SUBTITLES) {
			add("subtitles.hee." + sound.name.path, localized)
		}
		
		for (effect in getRegistryEntries<Effect>(ModEffects)) {
			if (effect is IHeeEffect) {
				effect.localization.localize(effect.name.removePrefix("effect.hee."))?.let { add(effect, it) }
			}
		}
		
		for (potion in getRegistryEntries<Potion>(ModPotions)) {
			val name = potion.path
			val localized = LocalizationStrategy.Default.localize(name)
			add("item.minecraft.potion.effect.$name", "Potion of $localized")
			add("item.minecraft.splash_potion.effect.$name", "Splash Potion of $localized")
			add("item.minecraft.lingering_potion.effect.$name", "Lingering Potion of $localized")
			add("item.minecraft.tipped_arrow.effect.$name", "Arrow of $localized")
		}
		
		for (territoryType in TerritoryType.ALL) {
			add(territoryType.translationKey, LocalizationStrategy.Default.localize(territoryType.title))
		}
		
		for (infusion in Infusion.values()) {
			add(infusion.translationKey, infusion.localizedName)
		}
		
		for (clusterHealth in listOf(*HealthStatus.values(), *HealthOverride.values())) {
			add(clusterHealth.translationKey, clusterHealth.localizedName)
		}
		
		for (command in ModCommands.admin + ModCommands.debug) {
			val name = command.name
			add("commands.hee.$name.info", command.description)
			
			for ((key, translation) in command.localization) {
				add("commands.hee.$name.$key", translation)
			}
		}
		
		for (command in ClientCommandHandler.nonHelpCommands.values) {
			val name = command.name
			add("commands.hee.$name.info", command.description)
		}
		
		for ((key, localizedText) in ModLanguage.GENERIC) {
			add(key, localizedText)
		}
	}
	
	private fun addAll(map: Map<String, String>) {
		for ((key, value) in map) {
			add(key, value)
		}
	}
	
	override fun add(key: String, value: String) {
		val previous = addedKeys.put(key, value)
		if (previous == null) {
			super.add(key, value)
		}
		else if (value != previous) {
			HEE.log.warn("[LangEnglish] Duplicate translation: $key -> $previous -> $value")
		}
	}
}
