package chylex.hee.game.item
import chylex.hee.game.block.util.IBlockHarvestToolCheck
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxEntityData
import chylex.hee.game.item.repair.ICustomRepairBehavior
import chylex.hee.game.item.util.CustomToolMaterial.SCORCHING_TOOL
import chylex.hee.game.mechanics.scorching.IScorchingItem
import chylex.hee.game.mechanics.scorching.ScorchingFortune
import chylex.hee.game.mechanics.scorching.ScorchingHelper
import chylex.hee.game.mechanics.scorching.ScorchingHelper.FX_BLOCK_BREAK
import chylex.hee.game.mechanics.scorching.ScorchingHelper.FX_ENTITY_HIT
import chylex.hee.network.client.PacketClientFX
import chylex.hee.proxy.Environment
import chylex.hee.system.migration.Hand.MAIN_HAND
import chylex.hee.system.migration.forge.EventResult
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.ItemTool
import chylex.hee.system.util.doDamage
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import net.minecraftforge.common.ToolType
import net.minecraftforge.event.entity.player.CriticalHitEvent

class ItemScorchingTool(
	properties: Properties,
	private val toolType: ToolType,
	attackSpeed: Float
) : ItemTool(0F, attackSpeed, SCORCHING_TOOL, emptySet(), properties.addToolType(toolType, SCORCHING_TOOL.harvestLevel)), IScorchingItem, ICustomRepairBehavior by ScorchingHelper.Repair(SCORCHING_TOOL){
	override val material
		get() = SCORCHING_TOOL
	
	// Mining behavior
	
	override fun canMine(state: BlockState): Boolean{
		val block = state.block
		
		if (!ScorchingFortune.canSmelt(Environment.getServer().getWorld(DimensionType.OVERWORLD) /* UPDATE test */, block)){
			return false
		}
		
		if (block is IBlockHarvestToolCheck){
			return block.canHarvestUsing(toolType, material.harvestLevel)
		}
		
		val harvestTool = block.getHarvestTool(state)
		return harvestTool == null || (toolType == harvestTool && material.harvestLevel >= block.getHarvestLevel(state))
	}
	
	override fun getDestroySpeed(stack: ItemStack, state: BlockState): Float{
		return if (canMine(state))
			efficiency
		else
			0.25F
	}
	
	override fun onBlockDestroyed(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, entity: EntityLivingBase): Boolean{
		if (!world.isRemote && state.getBlockHardness(world, pos) != 0F){
			if (canMine(state)){
				stack.doDamage(1, entity, MAIN_HAND)
				PacketClientFX(FX_BLOCK_BREAK, FxBlockData(pos)).sendToAllAround(world, pos, 32.0)
			}
			else{
				stack.doDamage(2, entity, MAIN_HAND)
			}
		}
		
		return true
	}
	
	// Hitting behavior
	
	override fun onHit(e: CriticalHitEvent){
		e.result = EventResult.DENY
	}
	
	override fun hitEntity(stack: ItemStack, target: EntityLivingBase, attacker: EntityLivingBase): Boolean{
		target.setFire(1)
		PacketClientFX(FX_ENTITY_HIT, FxEntityData(target)).sendToAllAround(target, 32.0)
		
		stack.doDamage(1, attacker, MAIN_HAND)
		return true
	}
	
	// Repair handling
	
	override fun getIsRepairable(toRepair: ItemStack, repairWith: ItemStack): Boolean{
		return ScorchingHelper.onGetIsRepairable(this, toRepair, repairWith)
	}
}
