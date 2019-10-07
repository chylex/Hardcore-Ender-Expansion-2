package chylex.hee.game.block.entity
import chylex.hee.game.block.BlockDarkChest
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.math.AxisAlignedBB

class TileEntityDarkChest : TileEntityChest(BlockDarkChest.TYPE){
	@Sided(Side.CLIENT)
	override fun getRenderBoundingBox(): AxisAlignedBB{
		return AxisAlignedBB(pos.add(-1, 0, -1), pos.add(2, 2, 2))
	}
}
