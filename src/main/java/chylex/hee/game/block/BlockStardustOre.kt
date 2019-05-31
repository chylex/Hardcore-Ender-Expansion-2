package chylex.hee.game.block
import chylex.hee.init.ModLoot
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.nextBiasedFloat
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

class BlockStardustOre(builder: BlockSimple.Builder) : BlockSimple(builder){
	override fun getDrops(drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int){
		ModLoot.STARDUST_ORE.generateDrops(drops, world, fortune)
	}
	
	override fun getExpDrop(state: IBlockState, world: IBlockAccess, pos: BlockPos, fortune: Int): Int{
		return (((world as? World)?.rand ?: RANDOM).nextBiasedFloat(4F) * 6F).ceilToInt()
	}
	
	override fun canSilkHarvest() = true
	
	override fun getRenderLayer() = CUTOUT
}
