package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.container.ContainerAmuletOfRecovery
import chylex.hee.game.container.ContainerBrewingStandCustom
import chylex.hee.game.container.ContainerLootChest
import chylex.hee.game.container.ContainerPortalTokenStorage
import chylex.hee.game.container.ContainerShulkerBoxInInventory
import chylex.hee.game.container.ContainerTrinketPouch
import chylex.hee.init.factory.ContainerConstructors
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.named
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.ContainerType
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModContainers{
	val AMULET_OF_RECOVERY       = build<ContainerAmuletOfRecovery>() named "amulet_of_recovery"
	val BREWING_STAND            = build<ContainerBrewingStandCustom>() named "brewing_stand"
	val LOOT_CHEST               = build<ContainerLootChest>() named "loot_chest"
	val PORTAL_TOKEN_STORAGE     = build<ContainerPortalTokenStorage>() named "portal_token_storage"
	val SHULKER_BOX_IN_INVENTORY = build<ContainerShulkerBoxInInventory>() named "shulker_box_in_inventory"
	val TRINKET_POUCH            = build<ContainerTrinketPouch>() named "trinket_pouch"
	
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<ContainerType<*>>){
		with(e.registry){
			register(AMULET_OF_RECOVERY)
			register(BREWING_STAND)
			register(LOOT_CHEST)
			register(PORTAL_TOKEN_STORAGE)
			register(SHULKER_BOX_IN_INVENTORY)
			register(TRINKET_POUCH)
		}
	}
	
	// Utilities
	
	private inline fun <reified T : Container> build(): ContainerType<T>{
		return ContainerType(ContainerConstructors.get(T::class.java))
	}
}
