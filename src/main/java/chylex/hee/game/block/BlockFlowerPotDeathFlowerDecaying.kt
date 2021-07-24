package chylex.hee.game.block

import chylex.hee.game.Resource.location
import chylex.hee.game.block.IBlockDeathFlowerDecaying.Companion.LEVEL
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.item.ItemDeathFlower
import chylex.hee.game.world.util.setBlock
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootContext
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random

class BlockFlowerPotDeathFlowerDecaying(builder: BlockBuilder, flower: Block) : BlockFlowerPotCustom(builder, flower), IBlockDeathFlowerDecaying {
	override val model
		get() = BlockStateModel(
			BlockStatePreset.None,
			BlockModel.Multi((1..4).map {
				BlockModel.Suffixed("_$it", BlockModel.PottedPlant(ModBlocks.DEATH_FLOWER_DECAYING.location("_$it")))
			})
		)
	
	override val drop
		get() = BlockDrop.Manual
	
	override fun fillStateContainer(container: Builder<Block, BlockState>) {
		container.add(LEVEL)
	}
	
	override val thisAsBlock
		get() = this
	
	override val healedFlowerBlock
		get() = ModBlocks.POTTED_DEATH_FLOWER_HEALED
	
	override val witheredFlowerBlock
		get() = ModBlocks.POTTED_DEATH_FLOWER_WITHERED
	
	private fun getDrop(state: BlockState): ItemStack {
		return ItemStack(flower).also { ItemDeathFlower.setDeathLevel(it, state[LEVEL]) }
	}
	
	override fun getDrops(state: BlockState, context: LootContext.Builder): MutableList<ItemStack> {
		return mutableListOf(ItemStack(Blocks.FLOWER_POT), getDrop(state))
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
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult): ActionResultType {
		val heldItem = player.getHeldItem(hand)
		
		if (heldItem.item === ModItems.END_POWDER) {
			return PASS
		}
		
		val drop = getDrop(state)
		
		if (heldItem.isEmpty) {
			player.setHeldItem(hand, drop)
		}
		else if (!player.addItemStackToInventory(drop)) {
			player.dropItem(drop, false)
		}
		
		pos.setBlock(world, emptyPot)
		return SUCCESS
	}
}
