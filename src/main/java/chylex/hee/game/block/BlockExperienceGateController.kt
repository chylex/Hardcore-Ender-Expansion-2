package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityExperienceGate
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.util.getTile
import net.minecraft.block.ITileEntityProvider
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

class BlockExperienceGateController(builder: BlockBuilder) : BlockExperienceGate(builder), ITileEntityProvider{
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityExperienceGate()
	}
	
	override fun findController(world: IBlockAccess, pos: BlockPos): TileEntityExperienceGate?{
		return pos.getTile(world)
	}
}
