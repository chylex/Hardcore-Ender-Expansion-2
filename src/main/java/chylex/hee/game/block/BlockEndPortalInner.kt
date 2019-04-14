package chylex.hee.game.block
import chylex.hee.HEE
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.system.util.closestTickingTile
import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockEndPortalInner(builder: BlockSimple.Builder) : BlockAbstractPortal(builder){
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityPortalInner.End()
	}
	
	override fun onEntityInside(world: World, pos: BlockPos, entity: Entity){
		val acceptor = pos.closestTickingTile<TileEntityEndPortalAcceptor>(world, MAX_DISTANCE_FROM_FRAME)
		
		if (acceptor != null && acceptor.isCharged){
			HEE.log.info("teleporting") // TODO
			entity.changeDimension(1)
		}
	}
}
