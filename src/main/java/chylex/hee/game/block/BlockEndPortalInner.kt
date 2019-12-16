package chylex.hee.game.block
import chylex.hee.HEE
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.mechanics.causatum.CausatumStage
import chylex.hee.game.mechanics.causatum.EnderCausatum
import chylex.hee.game.mechanics.portal.DimensionTeleporter
import chylex.hee.game.mechanics.portal.EntityPortalContact
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.closestTickingTile
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
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
		
		if (world.provider.dimension == HEE.DIM){
			entity.changeDimension(0, DimensionTeleporter.LastEndPortal)
		}
		else{
			val acceptor = pos.closestTickingTile<TileEntityEndPortalAcceptor>(world, MAX_DISTANCE_FROM_FRAME)
			
			if (acceptor != null && acceptor.isCharged){
				findInnerArea(world, acceptor.pos, ModBlocks.END_PORTAL_FRAME)?.let {
					(min, max) -> DimensionTeleporter.LastEndPortal.updateForEntity(entity, Pos((min.x + max.x) / 2, pos.y, (min.z + max.z) / 2))
				}
				
				if (entity is EntityPlayer){
					EnderCausatum.triggerStage(entity, CausatumStage.S2_ENTERED_END)
				}
				
				entity.changeDimension(1, DimensionTeleporter.EndSpawnPortal)
			}
		}
	}
}
