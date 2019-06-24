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
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTool
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ItemScorchingTool(private val toolClass: String) : ItemTool(SCORCHING_TOOL, emptySet()), IScorchingItem, ICustomRepairBehavior by ScorchingHelper.Repair(SCORCHING_TOOL){
	override val material: ToolMaterial
		get() = toolMaterial
	
	init{
		setHarvestLevel(toolClass, toolMaterial.harvestLevel)
	}
	
	// Mining behavior
	
	override fun canMine(state: IBlockState): Boolean{
		if (!ScorchingFortune.canSmelt(state)){
			return false
		}
		
		val block = state.block
		
		if (block is IBlockHarvestToolCheck){
			return block.canHarvestUsing(toolClass, toolMaterial.harvestLevel)
		}
		
		val harvestTool = block.getHarvestTool(state)
		return harvestTool == null || (toolClass == harvestTool && toolMaterial.harvestLevel >= block.getHarvestLevel(state))
	}
	
	override fun getDestroySpeed(stack: ItemStack, state: IBlockState): Float{
		return if (canMine(state))
			efficiency
		else
			0.25F
	}
	
	override fun onBlockDestroyed(stack: ItemStack, world: World, state: IBlockState, pos: BlockPos, entity: EntityLivingBase): Boolean{
		if (!world.isRemote && state.getBlockHardness(world, pos) != 0F){
			if (canMine(state)){
				stack.damageItem(1, entity)
				PacketClientFX(FX_BLOCK_BREAK, FxBlockData(pos)).sendToAllAround(world, pos, 32.0)
			}
			else{
				stack.damageItem(2, entity)
			}
		}
		
		return true
	}
	
	// Hitting behavior
	
	override fun hitEntity(stack: ItemStack, target: EntityLivingBase, attacker: EntityLivingBase): Boolean{
		target.setFire(1)
		PacketClientFX(FX_ENTITY_HIT, FxEntityData(target)).sendToAllAround(target, 32.0)
		
		stack.damageItem(1, attacker)
		return true
	}
	
	// Repair handling
	
	override fun getIsRepairable(toRepair: ItemStack, repairWith: ItemStack): Boolean{
		return ScorchingHelper.onGetIsRepairable(this, toRepair, repairWith)
	}
}
