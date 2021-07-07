package chylex.hee.game.item

import chylex.hee.game.block.logic.IBlockHarvestDropsOverride
import chylex.hee.game.item.properties.CustomToolMaterial.VOID_MINER
import chylex.hee.game.item.util.Tool.Type.AXE
import chylex.hee.game.item.util.Tool.Type.PICKAXE
import chylex.hee.game.item.util.Tool.Type.SHOVEL
import chylex.hee.system.MinecraftForgeEventBus
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.SubscribeEvent
import net.minecraft.block.BlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Hand.MAIN_HAND
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed
import net.minecraftforge.event.world.BlockEvent.BreakEvent

class ItemVoidMiner(properties: Properties) : ItemAbstractVoidTool(setupToolClasses(properties), VOID_MINER), IBlockHarvestDropsOverride {
	private companion object {
		private val toolClasses = arrayOf(PICKAXE, AXE, SHOVEL)
		
		private fun setupToolClasses(properties: Properties): Properties {
			return toolClasses.fold(properties) { props, type -> props.addToolType(type, VOID_MINER.harvestLevel) }
		}
	}
	
	init {
		MinecraftForgeEventBus.register(this)
	}
	
	override fun getDestroySpeed(stack: ItemStack, state: BlockState): Float {
		return 1F // need to wait until the event to skip vanilla Efficiency handling
	}
	
	override fun onBlockDestroyed(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, entity: LivingEntity): Boolean {
		guardItemBreaking(stack) { super.onBlockDestroyed(stack, world, state, pos, entity) }
		return true
	}
	
	override fun onHarvestDrops(state: BlockState, world: World, pos: BlockPos) {
		// drop nothing
	}
	
	override fun canApplyAtEnchantingTable(stack: ItemStack, enchantment: Enchantment): Boolean {
		return enchantment === Enchantments.EFFICIENCY || enchantment === Enchantments.UNBREAKING
	}
	
	// Mining events
	
	private fun getHeldVoidMiner(player: PlayerEntity): ItemStack? {
		return player.getHeldItem(MAIN_HAND).takeIf { it.item === this }
	}
	
	private fun isAppropriateToolFor(state: BlockState): Boolean {
		return ( // fuck checking just isToolEffective because vanilla hardcodes everything and Forge doesn't un-hardcode everything...
			Items.IRON_PICKAXE.canHarvestBlock(state) ||
			Items.IRON_SHOVEL.canHarvestBlock(state) ||
			Items.IRON_AXE.canHarvestBlock(state) ||
			toolClasses.any { state.block.isToolEffective(state, it) }
		)
	}
	
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onBreakSpeed(e: BreakSpeed) {
		val heldMiner = getHeldVoidMiner(e.player)?.takeIf { it.damage < it.maxDamage } ?: return
		val state = e.state
		
		if (isAppropriateToolFor(state)) {
			val efficiencyLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, heldMiner).coerceAtMost(5)
			e.newSpeed *= tier.efficiency + (6 * efficiencyLevel)
		}
	}
	
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onBlockBreak(e: BreakEvent) {
		if (e.player?.let(::getHeldVoidMiner) != null) {
			e.expToDrop = 0
		}
	}
}
