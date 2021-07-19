package chylex.hee.game.block

import chylex.hee.game.block.entity.TileEntityBrewingStandCustom
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.game.world.util.breakBlock
import chylex.hee.game.world.util.getTile
import chylex.hee.game.world.util.setBlock
import chylex.hee.init.ModContainers
import net.minecraft.block.BlockState
import net.minecraft.block.BrewingStandBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.tileentity.BrewingStandTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

open class BlockBrewingStandCustom(builder: BlockBuilder) : BrewingStandBlock(builder.p), IHeeBlock {
	override val renderLayer
		get() = CUTOUT
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
		return TileEntityBrewingStandCustom()
	}
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult): ActionResultType {
		if (world.isRemote) {
			return SUCCESS
		}
		
		val tile = pos.getTile<BrewingStandTileEntity>(world)
		
		if (tile is TileEntityBrewingStandCustom) {
			ModContainers.open(player, tile, pos)
		}
		else {
			// POLISH maybe make the tile entity upgrade smoother but this is fine lol
			pos.breakBlock(world, false)
			pos.setBlock(world, this)
		}
		
		return SUCCESS
	}
}
