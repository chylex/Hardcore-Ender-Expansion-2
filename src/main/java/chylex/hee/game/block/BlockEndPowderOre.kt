package chylex.hee.game.block
import chylex.hee.init.ModLoot
import chylex.hee.system.util.nextInt
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

class BlockEndPowderOre(builder: BlockSimple.Builder) : BlockSimple(builder){
	override fun getDrops(drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int){
		ModLoot.END_POWDER_ORE.generateDrops(drops, world, fortune)
	}
	
	override fun getExpDrop(state: IBlockState, world: IBlockAccess, pos: BlockPos, fortune: Int): Int{
		return ((world as? World)?.rand ?: RANDOM).nextInt(1, 2)
	}
	
	override fun canSilkHarvest(): Boolean = true
}
