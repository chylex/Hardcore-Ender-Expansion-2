package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModLoot
import chylex.hee.system.migration.Hand.MAIN_HAND
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

class BlockDustyStonePlain(builder: BlockBuilder) : BlockDustyStone(builder){
	override fun getDrops(drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int){
		val table = when(state.block){
			ModBlocks.DUSTY_STONE         -> ModLoot.DUSTY_STONE
			ModBlocks.DUSTY_STONE_CRACKED -> ModLoot.DUSTY_STONE_CRACKED
			ModBlocks.DUSTY_STONE_DAMAGED -> ModLoot.DUSTY_STONE_DAMAGED
			else -> return
		}
		
		table.generateDrops(drops, world, fortune)
	}
	
	override fun canSilkHarvest(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer): Boolean{
		return isPickaxeOrShovel(player.getHeldItem(MAIN_HAND))
	}
}
