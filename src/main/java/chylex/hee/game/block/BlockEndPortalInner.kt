package chylex.hee.game.block

import chylex.hee.HEE
import chylex.hee.game.block.entity.TileEntityEndPortalAcceptor
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.entity.util.EntityPortalContact
import chylex.hee.game.mechanics.causatum.CausatumStage
import chylex.hee.game.mechanics.causatum.EnderCausatum
import chylex.hee.game.world.isEndDimension
import chylex.hee.game.world.server.DimensionTeleporter
import chylex.hee.game.world.util.closestTickingTile
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.Pos
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

class BlockEndPortalInner(builder: BlockBuilder) : BlockAbstractPortal(builder) {
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
		return TileEntityPortalInner.End()
	}
	
	override fun onEntityInside(world: World, pos: BlockPos, entity: Entity) {
		if (!EntityPortalContact.shouldTeleport(entity)) {
			return
		}
		
		if (world.isEndDimension) {
			DimensionTeleporter.changeDimension(entity, World.OVERWORLD, DimensionTeleporter.LastEndPortal)
		}
		else {
			val acceptor = pos.closestTickingTile<TileEntityEndPortalAcceptor>(world, MAX_DISTANCE_FROM_FRAME)
			
			if (acceptor != null && acceptor.isCharged) {
				findInnerArea(world, acceptor.pos, ModBlocks.END_PORTAL_FRAME)?.let { (min, max) ->
					DimensionTeleporter.LastEndPortal.updateForEntity(entity, Pos((min.x + max.x) / 2, pos.y, (min.z + max.z) / 2))
				}
				
				if (entity is PlayerEntity) {
					EnderCausatum.triggerStage(entity, CausatumStage.S2_ENTERED_END)
				}
				
				DimensionTeleporter.changeDimension(entity, HEE.dim, DimensionTeleporter.EndSpawnPortal)
			}
		}
	}
}
