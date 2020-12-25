package chylex.hee.game.item

import chylex.hee.game.block.logic.IBlockHarvestDropsOverride
import chylex.hee.game.block.logic.IBlockHarvestToolCheck
import chylex.hee.game.inventory.doDamage
import chylex.hee.game.inventory.isNotEmpty
import chylex.hee.game.item.properties.CustomToolMaterial.SCORCHING_TOOL
import chylex.hee.game.item.repair.ICustomRepairBehavior
import chylex.hee.game.mechanics.scorching.IScorchingItem
import chylex.hee.game.mechanics.scorching.ScorchingFortune
import chylex.hee.game.mechanics.scorching.ScorchingHelper
import chylex.hee.game.mechanics.scorching.ScorchingHelper.FX_BLOCK_BREAK
import chylex.hee.game.mechanics.scorching.ScorchingHelper.FX_ENTITY_HIT
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.fx.FxBlockData
import chylex.hee.network.fx.FxEntityData
import chylex.hee.proxy.Environment
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.Hand.MAIN_HAND
import chylex.hee.system.migration.ItemTool
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import net.minecraftforge.common.ToolType

class ItemScorchingTool(
	properties: Properties,
	private val toolType: ToolType,
	attackSpeed: Float,
) : ItemTool(0F, attackSpeed, SCORCHING_TOOL, emptySet(), properties.addToolType(toolType, SCORCHING_TOOL.harvestLevel)), IScorchingItem, IBlockHarvestDropsOverride, ICustomRepairBehavior by ScorchingHelper.Repair(SCORCHING_TOOL) {
	override val material
		get() = SCORCHING_TOOL
	
	// Mining behavior
	
	override fun canMine(state: BlockState): Boolean {
		val block = state.block
		
		if (!ScorchingFortune.canSmelt(Environment.getDimension(DimensionType.OVERWORLD), block)) {
			return false
		}
		
		if (block is IBlockHarvestToolCheck) {
			return block.canHarvestUsing(toolType, material.harvestLevel)
		}
		
		val harvestTool = block.getHarvestTool(state)
		return harvestTool == null || (toolType == harvestTool && material.harvestLevel >= block.getHarvestLevel(state))
	}
	
	override fun getDestroySpeed(stack: ItemStack, state: BlockState): Float {
		return if (canMine(state))
			efficiency
		else
			0.25F
	}
	
	override fun onBlockDestroyed(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, entity: EntityLivingBase): Boolean {
		if (!world.isRemote && state.getBlockHardness(world, pos) != 0F) {
			if (canMine(state)) {
				stack.doDamage(1, entity, MAIN_HAND)
				PacketClientFX(FX_BLOCK_BREAK, FxBlockData(pos)).sendToAllAround(world, pos, 32.0)
			}
			else {
				stack.doDamage(2, entity, MAIN_HAND)
			}
		}
		
		return true
	}
	
	override fun onHarvestDrops(state: BlockState, world: World, pos: BlockPos) {
		if (canMine(state)) {
			val fortuneStack = ScorchingFortune.createSmeltedStack(world, state.block, world.rand)
			
			if (fortuneStack.isNotEmpty) {
				Block.spawnAsEntity(world, pos, fortuneStack)
			}
		}
	}
	
	// Hitting behavior
	
	override fun hitEntity(stack: ItemStack, target: EntityLivingBase, attacker: EntityLivingBase): Boolean {
		target.setFire(1)
		PacketClientFX(FX_ENTITY_HIT, FxEntityData(target)).sendToAllAround(target, 32.0)
		
		stack.doDamage(1, attacker, MAIN_HAND)
		return true
	}
	
	// Repair handling
	
	override fun getIsRepairable(toRepair: ItemStack, repairWith: ItemStack): Boolean {
		return ScorchingHelper.onGetIsRepairable(this, repairWith)
	}
}
