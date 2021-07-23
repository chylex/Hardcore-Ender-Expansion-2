package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import net.minecraft.block.BlockState
import net.minecraft.block.LeavesBlock
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.stats.Stats
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

class BlockWhitebarkLeaves(builder: BlockBuilder) : LeavesBlock(builder.p), IHeeBlock {
	override val model
		get() = BlockModel.Leaves
	
	override val renderLayer
		get() = CUTOUT
	
	override val drop
		get() = BlockDrop.Manual
	
	override fun harvestBlock(world: World, player: PlayerEntity, pos: BlockPos, state: BlockState, tile: TileEntity?, stack: ItemStack) {
		if (!world.isRemote && stack.item === Items.SHEARS) {
			player.addStat(Stats.BLOCK_MINED[this])
		}
		else {
			super.harvestBlock(world, player, pos, state, tile, stack)
		}
	}
	
	override fun canCreatureSpawn(state: BlockState, world: IBlockReader, pos: BlockPos, type: PlacementType?, entityType: EntityType<*>?): Boolean {
		return false
	}
	
	override fun getFlammability(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 60
	}
	
	override fun getFireSpreadSpeed(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 30
	}
}
