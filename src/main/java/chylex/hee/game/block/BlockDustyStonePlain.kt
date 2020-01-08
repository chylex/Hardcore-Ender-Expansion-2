package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.facades.Resource
import net.minecraft.util.ResourceLocation

class BlockDustyStonePlain(builder: BlockBuilder) : BlockDustyStone(builder){
	override fun getLootTable(): ResourceLocation = when(this){
		ModBlocks.DUSTY_STONE         -> Resource.Custom("blocks/dusty_stone")
		ModBlocks.DUSTY_STONE_CRACKED -> Resource.Custom("blocks/dusty_stone_cracked")
		ModBlocks.DUSTY_STONE_DAMAGED -> Resource.Custom("blocks/dusty_stone_damaged")
		else                          -> super.getLootTable()
	}
	
	/* UPDATE
	override fun canSilkHarvest(world: World, pos: BlockPos, state: BlockState, player: EntityPlayer): Boolean{
		return isPickaxeOrShovel(player.getHeldItem(MAIN_HAND))
	}*/
}
