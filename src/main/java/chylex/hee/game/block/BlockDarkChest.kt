package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityDarkChest
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.entity.living.ai.AIOcelotSitOverride.IOcelotCanSitOn
import chylex.hee.system.migration.vanilla.BlockChest
import chylex.hee.system.migration.vanilla.TileEntityChest
import net.minecraft.block.BlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader

class BlockDarkChest(builder: BlockBuilder) : BlockChest(builder.p), IOcelotCanSitOn{
	override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity{
		return TileEntityDarkChest()
	}
	
	override fun canOcelotSitOn(world: IWorldReader, pos: BlockPos): Boolean{
		return TileEntityChest.getPlayersUsing(world, pos) < 1
	}
}
