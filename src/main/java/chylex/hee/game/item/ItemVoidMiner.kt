package chylex.hee.game.item
import chylex.hee.game.item.base.ItemBaseVoidTool
import chylex.hee.game.item.util.Tool.Type.AXE
import chylex.hee.game.item.util.Tool.Type.PICKAXE
import chylex.hee.game.item.util.Tool.Type.SHOVEL
import net.minecraft.block.state.IBlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand.MAIN_HAND
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed
import net.minecraftforge.event.world.BlockEvent.BreakEvent
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST
import net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class ItemVoidMiner : ItemBaseVoidTool(){
	init{
		toolMaterial.harvestLevel.let {
			setHarvestLevel(PICKAXE, it)
			setHarvestLevel(AXE, it)
			setHarvestLevel(SHOVEL, it)
		}
	}
	
	override fun getDestroySpeed(stack: ItemStack, state: IBlockState): Float{
		return 1F // need to wait until the event to skip vanilla Efficiency handling
	}
	
	override fun onBlockDestroyed(stack: ItemStack, world: World, state: IBlockState, pos: BlockPos, entity: EntityLivingBase): Boolean{
		guardItemBreaking(stack, entity){ super.onBlockDestroyed(stack, world, state, pos, entity) }
		return true
	}
	
	override fun canApplyAtEnchantingTable(stack: ItemStack, enchantment: Enchantment): Boolean{
		return enchantment === Enchantments.EFFICIENCY || enchantment === Enchantments.UNBREAKING
	}
	
	// Mining events
	
	private fun getHeldVoidMiner(player: EntityPlayer): ItemStack?{
		return player.getHeldItem(MAIN_HAND).takeIf { it.item === this }
	}
	
	@SubscribeEvent(priority = HIGHEST)
	fun onBreakSpeed(e: BreakSpeed){
		val heldMiner = getHeldVoidMiner(e.entityPlayer)?.takeIf { it.itemDamage < it.maxDamage } ?: return
		val state = e.state
		
		if (getToolClasses(heldMiner).any { state.block.isToolEffective(it, state) }){
			val efficiencyLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, heldMiner).coerceAtMost(5)
			e.newSpeed *= toolMaterial.efficiency + (6 * efficiencyLevel)
		}
	}
	
	@SubscribeEvent(priority = HIGHEST)
	fun onBlockBreak(e: BreakEvent){
		if (e.player?.let(::getHeldVoidMiner) != null){
			e.expToDrop = 0
		}
	}
	
	@SubscribeEvent(priority = LOWEST)
	fun onHarvestDrops(e: HarvestDropsEvent){
		if (e.harvester?.let(::getHeldVoidMiner) != null){
			e.drops.clear()
			e.dropChance = 0F
		}
	}
}
