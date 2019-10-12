package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntitySpawnerObsidianTower
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.info.BlockBuilder.Companion.setupBlockProperties
import net.minecraft.block.BlockMobSpawner
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

class BlockSpawnerObsidianTowers(builder: BlockBuilder) : BlockMobSpawner(){
	init{
		setupBlockProperties(builder)
	}
	
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntitySpawnerObsidianTower()
	}
	
	override fun getExpDrop(state: IBlockState, world: IBlockAccess, pos: BlockPos, fortune: Int): Int{
		return 0
	}
}
