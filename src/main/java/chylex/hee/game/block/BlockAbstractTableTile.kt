package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityBaseTable
import chylex.hee.system.util.getTile
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

abstract class BlockAbstractTableTile<T : TileEntityBaseTable<*>>(builder: BlockSimple.Builder) : BlockAbstractTable(builder), ITileEntityProvider{
	abstract fun createNewTileEntity(): T
	
	final override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return createNewTileEntity()
	}
	
	override fun onBlockHarvested(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer){
		if (!world.isRemote && player.isCreative){
			pos.getTile<TileEntityBaseTable<*>>(world)?.onTableDestroyed(dropTableLink = false)
		}
	}
	
	override fun breakBlock(world: World, pos: BlockPos, state: IBlockState){
		if (!world.isRemote){
			pos.getTile<TileEntityBaseTable<*>>(world)?.onTableDestroyed(dropTableLink = true)
		}
		
		super.breakBlock(world, pos, state)
	}
}
