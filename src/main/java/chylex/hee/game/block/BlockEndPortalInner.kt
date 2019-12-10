package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.mechanics.portal.DimensionTeleporter
import chylex.hee.game.mechanics.portal.EntityPortalContact
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.closestTickingTile
import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockEndPortalInner(builder: BlockBuilder) : BlockAbstractPortal(builder){
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityPortalInner.End()
	}
	
	override fun onEntityInside(world: World, pos: BlockPos, entity: Entity){
		if (!EntityPortalContact.shouldTeleport(entity)){
			return
		}
		
		if (world.provider.dimension == 1){
			entity.changeDimension(0, DimensionTeleporter.LastEndPortal)
		}
		else{
			val acceptor = pos.closestTickingTile<TileEntityEndPortalAcceptor>(world, MAX_DISTANCE_FROM_FRAME)
			
			if (acceptor != null && acceptor.isCharged){
				findInnerArea(world, acceptor.pos, ModBlocks.END_PORTAL_FRAME)?.let {
					(min, max) -> DimensionTeleporter.LastEndPortal.updateForEntity(entity, Pos((min.x + max.x) / 2, pos.y, (min.z + max.z) / 2))
				}
				
				entity.changeDimension(1, DimensionTeleporter.EndSpawnPortal)
			}
		}
	}
}
