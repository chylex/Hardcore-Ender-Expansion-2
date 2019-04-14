package chylex.hee.game.block.entity
import chylex.hee.system.util.square
import net.minecraft.tileentity.TileEntityEndPortal
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

sealed class TileEntityPortalInner : TileEntityEndPortal(){
	@SideOnly(Side.CLIENT)
	override fun getMaxRenderDistanceSquared(): Double = square(180.0)
	
	@SideOnly(Side.CLIENT)
	override fun getRenderBoundingBox(): AxisAlignedBB = AxisAlignedBB(pos, pos.add(1, 1, 1))
	
	class End : TileEntityPortalInner()
	class Void : TileEntityPortalInner()
}
