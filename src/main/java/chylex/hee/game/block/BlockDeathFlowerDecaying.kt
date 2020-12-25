package chylex.hee.game.block

import chylex.hee.client.render.block.IBlockLayerCutout
import chylex.hee.game.block.IBlockDeathFlowerDecaying.Companion.LEVEL
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.item.ItemDeathFlower
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.loot.LootContext
import java.util.Random

class BlockDeathFlowerDecaying(builder: BlockBuilder) : BlockEndPlant(builder), IBlockDeathFlowerDecaying, IBlockLayerCutout {
	override fun fillStateContainer(container: Builder<Block, BlockState>) {
		container.add(LEVEL)
	}
	
	override val thisAsBlock
		get() = this
	
	override val healedFlowerBlock
		get() = ModBlocks.DEATH_FLOWER_HEALED
	
	override val witheredFlowerBlock
		get() = ModBlocks.DEATH_FLOWER_WITHERED
	
	override fun tickRate(world: IWorldReader): Int {
		return implTickRate()
	}
	
	override fun getStateForPlacement(context: BlockItemUseContext): BlockState {
		return defaultState.with(LEVEL, ItemDeathFlower.getDeathLevel(context.item))
	}
	
	private fun getDrop(state: BlockState): ItemStack {
		return ItemStack(this).also { ItemDeathFlower.setDeathLevel(it, state[LEVEL]) }
	}
	
	override fun getDrops(state: BlockState, context: LootContext.Builder): MutableList<ItemStack> {
		return mutableListOf(getDrop(state))
	}
	
	override fun getPickBlock(state: BlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: EntityPlayer): ItemStack {
		return getDrop(state)
	}
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
		@Suppress("DEPRECATION")
		super.onBlockAdded(state, world, pos, oldState, isMoving)
		implOnBlockAdded(world, pos)
	}
	
	override fun tick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random) {
		@Suppress("DEPRECATION")
		super.tick(state, world, pos, rand)
		implUpdateTick(world, pos, state, rand)
	}
}
