package chylex.hee.game.block.entity

import chylex.hee.init.ModTileEntities
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.square
import chylex.hee.system.migration.TileEntityEndPortal
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.math.AxisAlignedBB

sealed class TileEntityPortalInner(type: TileEntityType<out TileEntityPortalInner>) : TileEntityEndPortal(type) {
	@Sided(Side.CLIENT)
	override fun getMaxRenderDistanceSquared() = square(180.0)
	
	@Sided(Side.CLIENT)
	override fun getRenderBoundingBox() = AxisAlignedBB(pos.add(-2, 0, -2), pos.add(3, 1, 3))
	
	class End(type: TileEntityType<End>) : TileEntityPortalInner(type) {
		constructor() : this(ModTileEntities.END_PORTAL_INNER)
	}
	
	class Void(type: TileEntityType<Void>) : TileEntityPortalInner(type) {
		constructor() : this(ModTileEntities.VOID_PORTAL_INNER)
	}
}
