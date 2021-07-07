package chylex.hee.game.block

import chylex.hee.game.block.entity.TileEntityDarkChest
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.entity.living.ai.AIOcelotSitOverride.IOcelotCanSitOn
import chylex.hee.init.ModTileEntities
import net.minecraft.block.BlockState
import net.minecraft.block.ChestBlock
import net.minecraft.tileentity.ChestTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader
import java.util.function.Supplier

class BlockDarkChest(builder: BlockBuilder) : ChestBlock(builder.p, Supplier<TileEntityType<out ChestTileEntity>> { ModTileEntities.DARK_CHEST }), IOcelotCanSitOn {
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
		return TileEntityDarkChest()
	}
	
	override fun canOcelotSitOn(world: IWorldReader, pos: BlockPos): Boolean {
		return ChestTileEntity.getPlayersUsing(world, pos) < 1
	}
}
