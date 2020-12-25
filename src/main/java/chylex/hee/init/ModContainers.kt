package chylex.hee.init

import chylex.hee.HEE
import chylex.hee.game.container.ContainerAmuletOfRecovery
import chylex.hee.game.container.ContainerBrewingStandCustom
import chylex.hee.game.container.ContainerLootChest
import chylex.hee.game.container.ContainerPortalTokenStorage
import chylex.hee.game.container.ContainerShulkerBox
import chylex.hee.game.container.ContainerShulkerBoxInInventory
import chylex.hee.game.container.ContainerTrinketPouch
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.forge.named
import chylex.hee.system.forge.registerAllFields
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.EntityPlayerMP
import chylex.hee.system.reflection.ObjectConstructors
import chylex.hee.system.serialization.writePos
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.ContainerType
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.extensions.IForgeContainerType
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD
import net.minecraftforge.fml.network.IContainerFactory
import net.minecraftforge.fml.network.NetworkHooks

@SubscribeAllEvents(modid = HEE.ID, bus = MOD)
object ModContainers {
	@JvmField val AMULET_OF_RECOVERY       = build<ContainerAmuletOfRecovery>() named "amulet_of_recovery"
	@JvmField val BREWING_STAND            = build<ContainerBrewingStandCustom>() named "brewing_stand"
	@JvmField val LOOT_CHEST               = build<ContainerLootChest>() named "loot_chest"
	@JvmField val PORTAL_TOKEN_STORAGE     = build<ContainerPortalTokenStorage>() named "portal_token_storage"
	@JvmField val SHULKER_BOX              = build<ContainerShulkerBox>() named "shulker_box"
	@JvmField val SHULKER_BOX_IN_INVENTORY = build<ContainerShulkerBoxInInventory>() named "shulker_box_in_inventory"
	@JvmField val TRINKET_POUCH            = build<ContainerTrinketPouch>() named "trinket_pouch"
	
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<ContainerType<*>>) {
		e.registerAllFields(this)
	}
	
	// Open
	
	fun open(player: EntityPlayer, container: INamedContainerProvider) {
		(player as? EntityPlayerMP)?.let { NetworkHooks.openGui(it, container) }
	}
	
	fun open(player: EntityPlayer, container: INamedContainerProvider, parameter: Int) {
		(player as? EntityPlayerMP)?.let { NetworkHooks.openGui(it, container) { buffer -> buffer.writeVarInt(parameter) } }
	}
	
	fun open(player: EntityPlayer, container: INamedContainerProvider, parameter: BlockPos) {
		(player as? EntityPlayerMP)?.let { NetworkHooks.openGui(it, container) { buffer -> buffer.writePos(parameter) } }
	}
	
	// Utilities
	
	@Suppress("UNCHECKED_CAST")
	private inline fun <reified T : Container> build(): ContainerType<T> {
		val handle = ObjectConstructors.generic<T, Container, IContainerFactory<T>>("create", Int::class.java, PlayerInventory::class.java, PacketBuffer::class.java)
		return IForgeContainerType.create(handle.invokeExact() as IContainerFactory<T>)
	}
}
