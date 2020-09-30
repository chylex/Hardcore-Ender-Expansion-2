package chylex.hee.datagen
import chylex.hee.HEE
import chylex.hee.datagen.client.BlockItemModels
import chylex.hee.datagen.client.BlockModels
import chylex.hee.datagen.client.ItemModels
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object DataGen{
	@SubscribeEvent
	fun register(e: GatherDataEvent){
		val modid = HEE.ID
		val helper = e.existingFileHelper
		
		with(e.generator){
			if (e.includeClient()){
				addProvider(BlockModels(this, modid, helper))
				addProvider(BlockItemModels(this, modid, helper))
				addProvider(ItemModels(this, modid, helper))
			}
		}
	}
}
