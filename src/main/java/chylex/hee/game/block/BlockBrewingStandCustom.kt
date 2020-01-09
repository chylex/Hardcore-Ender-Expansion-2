package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.init.ModGuiHandler.GuiType
import chylex.hee.system.migration.vanilla.BlockBrewingStand
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.getTile
import net.minecraft.block.BlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

open class BlockBrewingStandCustom(builder: BlockBuilder) : BlockBrewingStand(builder.p){
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity{
		return TileEntityBrewingStandCustom()
	}
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer, hand: Hand, hit: BlockRayTraceResult): Boolean{
		if (world.isRemote){
			return true
		}
		
		pos.getTile<TileEntityBrewingStandCustom>(world)?.let {
			GuiType.BREWING_STAND.open(player, pos.x, pos.y, pos.z)
		}
		
		return true
	}
}
