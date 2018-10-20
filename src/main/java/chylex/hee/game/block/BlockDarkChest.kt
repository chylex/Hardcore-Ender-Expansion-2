package chylex.hee.game.block
import chylex.hee.game.block.BlockSimple.Builder.Companion.setupBlockProperties
import chylex.hee.game.block.entity.TileEntityDarkChest
import net.minecraft.block.BlockChest
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.util.EnumHelper

class BlockDarkChest(builder: BlockSimple.Builder) : BlockChest(TYPE){
	companion object{
		val TYPE = EnumHelper.addEnum(BlockChest.Type::class.java, "HEE_DARK", emptyArray())!!
	}
	
	init{
		setupBlockProperties(builder, replaceMaterialAndColor = true)
	}
	
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityDarkChest()
	}
}
