package chylex.hee.game.block

import chylex.hee.game.Resource.location
import chylex.hee.game.block.entity.TileEntityDarkChest
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockStateModels
import chylex.hee.game.entity.living.ai.AIOcelotSitOverride.IOcelotCanSitOn
import chylex.hee.init.ModBlocks
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

class BlockDarkChest(builder: BlockBuilder) : ChestBlock(builder.p, Supplier<TileEntityType<out ChestTileEntity>> { ModTileEntities.DARK_CHEST }), IHeeBlock, IOcelotCanSitOn {
	override val model
		get() = BlockStateModels.Chest(ModBlocks.GLOOMROCK_SMOOTH.location)
	
	override val drop
		get() = BlockDrop.NamedTile
	
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity {
		return TileEntityDarkChest()
	}
	
	override fun canOcelotSitOn(world: IWorldReader, pos: BlockPos): Boolean {
		return ChestTileEntity.getPlayersUsing(world, pos) < 1
	}
}
