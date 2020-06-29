package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.init.ModContainers
import chylex.hee.system.migration.vanilla.BlockBrewingStand
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.TileEntityBrewingStand
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.getTile
import chylex.hee.system.util.setBlock
import net.minecraft.block.BlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

open class BlockBrewingStandCustom(builder: BlockBuilder) : BlockBrewingStand(builder.p){
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity{
		return TileEntityBrewingStandCustom()
	}
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer, hand: Hand, hit: BlockRayTraceResult): ActionResultType{
		if (world.isRemote){
			return SUCCESS
		}
		
		val tile = pos.getTile<TileEntityBrewingStand>(world)
		
		if (tile is TileEntityBrewingStandCustom){
			ModContainers.open(player, tile, pos)
		}
		else{
			// TODO maybe make the tile entity upgrade smoother but this is fine lol
			pos.breakBlock(world, false)
			pos.setBlock(world, this)
		}
		
		return SUCCESS
	}
}
