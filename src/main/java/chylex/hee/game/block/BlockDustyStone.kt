package chylex.hee.game.block
import chylex.hee.client.util.MC.player
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.IBlockHarvestToolCheck
import chylex.hee.game.item.util.Tool.Type.PICKAXE
import chylex.hee.game.item.util.Tool.Type.SHOVEL
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.getState
import net.minecraft.block.state.IBlockState
import net.minecraft.client.particle.ParticleManager
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.World

abstract class BlockDustyStone(builder: BlockBuilder) : BlockSimple(builder), IBlockHarvestToolCheck{
	override fun isToolEffective(type: String, state: IBlockState): Boolean{
		return type == PICKAXE || type == SHOVEL
	}
	
	override fun canHarvestUsing(toolClass: String, toolLevel: Int): Boolean{
		return isToolEffective(toolClass, defaultState) && toolLevel >= 0
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
	override fun addHitEffects(state: IBlockState, world: World, target: RayTraceResult, manager: ParticleManager): Boolean{
		manager.addBlockHitEffects(target.blockPos, target.sideHit)
		manager.addBlockHitEffects(target.blockPos, target.sideHit)
		return false
	}
	
	@Sided(Side.CLIENT)
	override fun addDestroyEffects(world: World, pos: BlockPos, manager: ParticleManager): Boolean{
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
