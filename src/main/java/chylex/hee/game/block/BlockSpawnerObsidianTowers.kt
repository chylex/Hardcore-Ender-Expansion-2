package chylex.hee.game.block

import chylex.hee.game.block.entity.TileEntitySpawnerObsidianTower
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.SpawnerBlock
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader

class BlockSpawnerObsidianTowers(builder: BlockBuilder) : SpawnerBlock(builder.p), IHeeBlock {
	override val model
		get() = BlockStateModel(BlockStatePreset.SimpleFrom(Blocks.SPAWNER), BlockModel.Manual)
	
	override val renderLayer
		get() = CUTOUT
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
		return TileEntitySpawnerObsidianTower()
	}
	
	override fun getExpDrop(state: BlockState, world: IWorldReader, pos: BlockPos, fortune: Int, silktouch: Int): Int {
		return 0
	}
}
