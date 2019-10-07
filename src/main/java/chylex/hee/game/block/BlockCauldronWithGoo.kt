package chylex.hee.game.block
import chylex.hee.game.mechanics.potion.brewing.PotionItems
import chylex.hee.system.migration.vanilla.Items
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.init.PotionTypes
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockCauldronWithGoo(private val goo: BlockAbstractGoo) : BlockAbstractCauldron(){
	init{
		setHardness(2F)
	}
	
	override fun createFilledBucket(): ItemStack?{
		return ItemStack(goo.filledBucket)
	}
	
	override fun createFilledBottle(): ItemStack?{
		return PotionItems.getBottle(Items.POTIONITEM, PotionTypes.THICK)
	}
	
	override fun onEntityCollision(world: World, pos: BlockPos, state: IBlockState, entity: Entity){
		goo.onInsideGoo(entity)
	}
	
	override fun fillWithRain(world: World, pos: BlockPos){}
}
