package chylex.hee.game.block
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.getTile
import net.minecraft.block.BlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

abstract class BlockAbstractTableTile<T : TileEntityBaseTable>(builder: BlockBuilder) : BlockAbstractTable(builder){
	abstract fun createTileEntity(): T
	
	override fun hasTileEntity(state: BlockState): Boolean{
		return true
	}
	
	final override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity{
		return createTileEntity()
	}
	
	override fun onBlockHarvested(world: World, pos: BlockPos, state: BlockState, player: EntityPlayer){
		if (!world.isRemote && player.isCreative){
			pos.getTile<TileEntityBaseTable>(world)?.onTableDestroyed(dropTableLink = false)
		}
	}
	
	override fun onReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, isMoving: Boolean){
		pos.getTile<TileEntityBaseTable>(world)?.onTableDestroyed(dropTableLink = true)
		super.onReplaced(state, world, pos, newState, isMoving)
	}
}
