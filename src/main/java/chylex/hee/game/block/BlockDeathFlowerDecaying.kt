package chylex.hee.game.block

import chylex.hee.game.Resource.location
import chylex.hee.game.block.IBlockDeathFlowerDecaying.Companion.LEVEL
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.item.ItemDeathFlower
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.init.ModBlocks
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootContext
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random

class BlockDeathFlowerDecaying(builder: BlockBuilder) : BlockEndPlant(builder), IBlockDeathFlowerDecaying {
	override val model
		get() = BlockStateModel(
			BlockStatePreset.None,
			BlockModel.Multi((1..4).map {
				BlockModel.Suffixed("_$it", BlockModel.Cross(this.location("_$it")))
			}),
			ItemModel.Multi(
				ItemModel.Suffixed("_1"),
				ItemModel.WithOverrides(
					ItemModel.Layers("death_flower_1"),
					ItemDeathFlower.DEATH_LEVEL_PROPERTY to mapOf(
						4F to ItemModel.Suffixed("_2"),
						8F to ItemModel.Suffixed("_3"),
						12F to ItemModel.Suffixed("_4"),
					)
				))
		)
	
	override val drop
		get() = BlockDrop.Manual
	
	override fun fillStateContainer(container: Builder<Block, BlockState>) {
		container.add(LEVEL)
	}
	
	override val thisAsBlock
		get() = this
	
	override val healedFlowerBlock
		get() = ModBlocks.DEATH_FLOWER_HEALED
	
	override val witheredFlowerBlock
		get() = ModBlocks.DEATH_FLOWER_WITHERED
	
	override fun getStateForPlacement(context: BlockItemUseContext): BlockState {
		return defaultState.with(LEVEL, ItemDeathFlower.getDeathLevel(context.item))
	}
	
	private fun getDrop(state: BlockState): ItemStack {
		return ItemStack(this).also { ItemDeathFlower.setDeathLevel(it, state[LEVEL]) }
	}
	
	override fun getDrops(state: BlockState, context: LootContext.Builder): MutableList<ItemStack> {
		return mutableListOf(getDrop(state))
	}
	
	override fun getPickBlock(state: BlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: PlayerEntity): ItemStack {
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
