package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.entity.TileEntityVoidPortalStorage
import chylex.hee.system.util.closestTickingTile
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockVoidPortalInner(builder: BlockSimple.Builder) : BlockAbstractPortal(builder){
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityPortalInner.Void()
	}
	
	override fun onEntityInside(world: World, pos: BlockPos, entity: Entity){
		val acceptor = pos.closestTickingTile<TileEntityVoidPortalStorage>(world, MAX_DISTANCE_FROM_FRAME)
		
		if (acceptor != null && entity.isNonBoss){
		}
	}
}
