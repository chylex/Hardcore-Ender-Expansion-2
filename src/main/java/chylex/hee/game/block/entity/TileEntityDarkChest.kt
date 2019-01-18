package chylex.hee.game.block.entity
import chylex.hee.game.block.BlockDarkChest
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class TileEntityDarkChest : TileEntityChest(BlockDarkChest.TYPE){
	@SideOnly(Side.CLIENT)
	override fun getRenderBoundingBox(): AxisAlignedBB{
		return AxisAlignedBB(pos.add(-1, 0, -1), pos.add(2, 2, 2))
	}
}
