package chylex.hee.game.block

import chylex.hee.game.block.logic.IBlockHarvestToolCheck
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.item.util.Tool.Type.PICKAXE
import chylex.hee.game.item.util.Tool.Type.SHOVEL
import chylex.hee.game.particle.ParticleDust
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.Constant
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.isFullBlock
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.random.nextInt
import net.minecraft.block.BlockState
import net.minecraft.client.particle.ParticleManager
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraftforge.common.ToolType
import java.util.Random

abstract class BlockDustyStone(builder: BlockBuilder) : HeeBlock(builder), IBlockHarvestToolCheck {
	private companion object {
		private val PARTICLE_DUST = ParticleSpawnerCustom(
			type = ParticleDust,
			data = ParticleDust.Data(lifespan = 300..470, scale = (0.145F)..(0.165F), reactsToSkyLight = false),
			pos = Constant(0.7F, DOWN) + InBox(0.75F, 0.2F, 0.75F)
		)
	}
	
	abstract override fun canHarvestBlock(state: BlockState, world: IBlockReader, pos: BlockPos, player: PlayerEntity): Boolean
	
	override fun canHarvestUsing(toolClass: ToolType, toolLevel: Int): Boolean {
		return isToolEffective(defaultState, toolClass) && toolLevel >= 0
	}
	
	override fun isToolEffective(state: BlockState, tool: ToolType): Boolean {
		return tool == PICKAXE || tool == SHOVEL
	}
	
	protected fun isPickaxeOrShovel(player: PlayerEntity, stack: ItemStack): Boolean {
		return stack.item.let {
			it.getHarvestLevel(stack, PICKAXE, player, defaultState) >= 0 ||
			it.getHarvestLevel(stack, SHOVEL,  player, defaultState) >= 0
		}
	}
	
	// Client side
	
	@Sided(Side.CLIENT)
	private var isSpawningExtraBreakParticles = false
	
	@Sided(Side.CLIENT)
	override fun animateTick(state: BlockState, world: World, pos: BlockPos, rand: Random) {
		if (rand.nextInt(18) == 0 && !pos.down().isFullBlock(world)) {
			PARTICLE_DUST.spawn(Point(pos, rand.nextInt(1, 2)), rand)
		}
	}
	
	@Sided(Side.CLIENT)
	override fun addHitEffects(state: BlockState, world: World, target: RayTraceResult, manager: ParticleManager): Boolean {
		if (target is BlockRayTraceResult) {
			manager.addBlockHitEffects(target.pos, target.face)
			manager.addBlockHitEffects(target.pos, target.face)
		}
		
		return false
	}
	
	@Sided(Side.CLIENT)
	override fun addDestroyEffects(state: BlockState, world: World, pos: BlockPos, manager: ParticleManager): Boolean {
		if (isSpawningExtraBreakParticles) {
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
