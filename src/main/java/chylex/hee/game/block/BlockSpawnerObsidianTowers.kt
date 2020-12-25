package chylex.hee.game.block

import chylex.hee.client.render.block.IBlockLayerCutout
import chylex.hee.game.block.entity.TileEntitySpawnerObsidianTower
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.system.migration.BlockMobSpawner
import net.minecraft.block.BlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader

class BlockSpawnerObsidianTowers(builder: BlockBuilder) : BlockMobSpawner(builder.p), IBlockLayerCutout {
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
		return TileEntitySpawnerObsidianTower()
	}
	
	override fun getExpDrop(state: BlockState, world: IWorldReader, pos: BlockPos, fortune: Int, silktouch: Int): Int {
		return 0
	}
}
