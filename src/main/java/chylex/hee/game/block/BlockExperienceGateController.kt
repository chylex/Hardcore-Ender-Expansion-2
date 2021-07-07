package chylex.hee.game.block

import chylex.hee.game.block.entity.TileEntityExperienceGate
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.world.util.getTile
import net.minecraft.block.BlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

class BlockExperienceGateController(builder: BlockBuilder) : BlockExperienceGate(builder) {
	override fun hasTileEntity(state: BlockState): Boolean {
		return true
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
		return TileEntityExperienceGate()
	}
	
	override fun findController(world: IBlockReader, pos: BlockPos): TileEntityExperienceGate? {
		return pos.getTile(world)
	}
}
