package chylex.hee.game.block.entity
import chylex.hee.init.ModTileEntities
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.TileEntityEndPortal
import chylex.hee.system.util.square
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.math.AxisAlignedBB

sealed class TileEntityPortalInner(type: TileEntityType<out TileEntityPortalInner>) : TileEntityEndPortal(type){
	@Sided(Side.CLIENT)
	override fun getMaxRenderDistanceSquared() = square(180.0)
	
	@Sided(Side.CLIENT)
	override fun getRenderBoundingBox() = AxisAlignedBB(pos, pos.add(1, 1, 1))
	
	class End(type: TileEntityType<End>) : TileEntityPortalInner(type){
		constructor() : this(ModTileEntities.END_PORTAL_INNER)
	}
	
	class Void(type: TileEntityType<Void>) : TileEntityPortalInner(type){
		constructor() : this(ModTileEntities.VOID_PORTAL_INNER)
	}
}
