package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.info.BlockBuilder.Companion.setupBlockProperties
import chylex.hee.init.ModGuiHandler.GuiType
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.getTile
import chylex.hee.system.util.translationKeyOriginal
import net.minecraft.block.BlockBrewingStand
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class BlockBrewingStandOverride(builder: BlockBuilder) : BlockBrewingStand(){
	init{
		setupBlockProperties(builder)
		translationKey = Blocks.BREWING_STAND.translationKeyOriginal
	}
	
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityBrewingStandCustom()
	}
	
	override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean{
		if (world.isRemote){
			return true
		}
		
		pos.getTile<TileEntityBrewingStandCustom>(world)?.let {
			GuiType.BREWING_STAND.open(player, pos.x, pos.y, pos.z)
		}
		
		return true
	}
}
