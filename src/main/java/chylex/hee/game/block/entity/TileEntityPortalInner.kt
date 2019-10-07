package chylex.hee.game.block.entity
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.square
import net.minecraft.tileentity.TileEntityEndPortal
import net.minecraft.util.math.AxisAlignedBB

sealed class TileEntityPortalInner : TileEntityEndPortal(){
	@Sided(Side.CLIENT)
	override fun getMaxRenderDistanceSquared() = square(180.0)
	
	@Sided(Side.CLIENT)
	override fun getRenderBoundingBox() = AxisAlignedBB(pos, pos.add(1, 1, 1))
	
	class End : TileEntityPortalInner()
	class Void : TileEntityPortalInner()
}
