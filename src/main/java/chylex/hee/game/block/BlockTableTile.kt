package chylex.hee.game.block
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.system.facades.Resource
import net.minecraftforge.registries.ForgeRegistries

class BlockTableTile<T : TileEntityBaseTable>(builder: BlockBuilder, name: String, tier: Int, firstTier: Int) : BlockAbstractTableTile<T>(builder, name, tier, firstTier){
	@Suppress("UNCHECKED_CAST")
	override fun createTileEntity() = ForgeRegistries.TILE_ENTITIES.getValue(Resource.Custom(name))!!.create()!! as T
}
