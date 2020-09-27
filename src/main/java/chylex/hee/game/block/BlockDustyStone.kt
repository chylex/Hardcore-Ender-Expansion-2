package chylex.hee.game.block
import chylex.hee.client.MC.player
import chylex.hee.game.block.logic.IBlockHarvestToolCheck
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.item.Tool.Type.PICKAXE
import chylex.hee.game.item.Tool.Type.SHOVEL
import chylex.hee.game.world.getState
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import net.minecraft.block.BlockState
import net.minecraft.client.particle.ParticleManager
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraftforge.common.ToolType

abstract class BlockDustyStone(builder: BlockBuilder) : BlockSimple(builder), IBlockHarvestToolCheck{
	abstract override fun canHarvestBlock(state: BlockState, world: IBlockReader, pos: BlockPos, player: PlayerEntity): Boolean
	
	override fun canHarvestUsing(toolClass: ToolType, toolLevel: Int): Boolean{
		return isToolEffective(defaultState, toolClass) && toolLevel >= 0
	}
	
	override fun isToolEffective(state: BlockState, tool: ToolType): Boolean{
		return tool == PICKAXE || tool == SHOVEL
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
