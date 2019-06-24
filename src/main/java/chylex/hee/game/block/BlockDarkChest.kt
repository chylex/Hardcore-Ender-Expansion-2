package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityDarkChest
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.info.BlockBuilder.Companion.setupBlockProperties
import chylex.hee.game.entity.living.ai.AIOcelotSitOverride.IOcelotCanSitOn
import chylex.hee.system.util.getTile
import net.minecraft.block.BlockChest
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.util.EnumHelper

class BlockDarkChest(builder: BlockBuilder) : BlockChest(TYPE), IOcelotCanSitOn{
	companion object{
		val TYPE = EnumHelper.addEnum(BlockChest.Type::class.java, "HEE_DARK", emptyArray())!!
	}
	
	init{
		setupBlockProperties(builder, replaceMaterialAndColor = true)
	}
	
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityDarkChest()
	}
	
	override fun canOcelotSitOn(world: World, pos: BlockPos): Boolean{
		return pos.getTile<TileEntityDarkChest>(world)?.let { it.numPlayersUsing < 1 } == true
	}
}
