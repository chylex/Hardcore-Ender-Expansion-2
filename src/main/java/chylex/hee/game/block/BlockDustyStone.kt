package chylex.hee.game.block
import chylex.hee.client.util.MC.player
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.IBlockHarvestToolCheck
import chylex.hee.game.item.util.Tool.Type.PICKAXE
import chylex.hee.game.item.util.Tool.Type.SHOVEL
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.getState
import net.minecraft.block.BlockState
import net.minecraft.client.particle.ParticleManager
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.World
import net.minecraftforge.common.ToolType

abstract class BlockDustyStone(builder: BlockBuilder) : BlockSimple(builder), IBlockHarvestToolCheck{
	override fun isToolEffective(state: BlockState, tool: ToolType): Boolean{
		return tool == PICKAXE || tool == SHOVEL
	}
	
	override fun canHarvestUsing(toolClass: ToolType, toolLevel: Int): Boolean{
		return isToolEffective(defaultState, toolClass) && toolLevel >= 0
	}
	
	protected fun isPickaxeOrShovel(stack: ItemStack): Boolean{
		return stack.item.let {
			it.getHarvestLevel(stack, PICKAXE, player, defaultState) >= 0 ||
			it.getHarvestLevel(stack, SHOVEL,  player, defaultState) >= 0
		}
	}
	
	// Client side
	
	@Sided(Side.CLIENT)
	private var isSpawningExtraBreakParticles = false
	
	@Sided(Side.CLIENT)
	override fun addHitEffects(state: BlockState, world: World, target: RayTraceResult, manager: ParticleManager): Boolean{
		if (target is BlockRayTraceResult){
			manager.addBlockHitEffects(target.pos, target.face)
			manager.addBlockHitEffects(target.pos, target.face)
		}
		
		return false
	}
	
	@Sided(Side.CLIENT)
	override fun addDestroyEffects(state: BlockState, world: World, pos: BlockPos, manager: ParticleManager): Boolean{
		if (isSpawningExtraBreakParticles){
			return false
		}
		
		isSpawningExtraBreakParticles = true
		manager.addBlockDestroyEffects(pos, pos.getState(world))
		manager.addBlockDestroyEffects(pos, pos.getState(world))
		isSpawningExtraBreakParticles = false
		
		// TODO sound fx
		return false
	}
}
