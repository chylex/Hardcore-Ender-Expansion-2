package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityMinersBurialAltar
import chylex.hee.game.block.properties.BlockBuilder
import net.minecraft.block.BlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.world.IBlockReader

class BlockMinersBurialAltar(builder: BlockBuilder) : BlockSimpleShaped(builder, AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.75, 1.0)){
	override fun hasTileEntity(state: BlockState): Boolean{
		return true
	}
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity{
		return TileEntityMinersBurialAltar()
	}
}
