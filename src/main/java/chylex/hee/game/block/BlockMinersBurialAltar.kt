package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityMinersBurialAltar
import chylex.hee.game.block.info.BlockBuilder
import net.minecraft.block.ITileEntityProvider
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.world.World

class BlockMinersBurialAltar(builder: BlockBuilder) : BlockSimpleShaped(builder, AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.75, 1.0)), ITileEntityProvider{
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityMinersBurialAltar()
	}
}
