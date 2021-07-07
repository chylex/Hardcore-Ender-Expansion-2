package chylex.hee.datagen

import chylex.hee.HEE
import chylex.hee.datagen.client.BlockItemModels
import chylex.hee.datagen.client.BlockModels
import chylex.hee.datagen.client.BlockStates
import chylex.hee.datagen.client.ItemModels
import chylex.hee.datagen.server.BlockLootTables
import chylex.hee.datagen.server.BlockTags
import chylex.hee.datagen.server.ItemTags
import chylex.hee.util.forge.SubscribeAllEvents
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object DataGen {
	@SubscribeEvent
	fun register(e: GatherDataEvent) {
		val modid = HEE.ID
		val helper = e.existingFileHelper
		
		with(e.generator) {
			if (e.includeClient()) {
				addProvider(BlockStates(this, modid, helper))
				addProvider(BlockModels(this, modid, helper))
				addProvider(BlockItemModels(this, modid, helper))
				addProvider(ItemModels(this, modid, helper))
			}
			
			if (e.includeServer()) {
				val blockTags = BlockTags(this, modid, helper)
				addProvider(BlockLootTables(this))
				addProvider(blockTags)
				addProvider(ItemTags(this, blockTags, modid, helper))
			}
		}
	}
}
