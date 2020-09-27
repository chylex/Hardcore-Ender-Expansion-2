package chylex.hee.game.block.entity
import chylex.hee.init.ModTileEntities
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.TileEntityChest
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.math.AxisAlignedBB

class TileEntityDarkChest(type: TileEntityType<TileEntityDarkChest>) : TileEntityChest(type){
	constructor() : this(ModTileEntities.DARK_CHEST)
	
	@Sided(Side.CLIENT)
	override fun getRenderBoundingBox(): AxisAlignedBB{
		return AxisAlignedBB(pos.add(-1, 0, -1), pos.add(2, 2, 2))
	}
}
