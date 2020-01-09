package chylex.hee.game.item
import chylex.hee.game.block.IBlockDeathFlowerDecaying
import chylex.hee.system.migration.vanilla.ItemBlock
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.getIntegerOrNull
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import net.minecraft.block.Block
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList

class ItemDeathFlower(block: Block, properties: Properties) : ItemBlock(block, properties){
	companion object{
		private const val LEVEL_TAG = "DeathLevel"
		
		fun getDeathLevel(stack: ItemStack): Int{
			return stack.heeTagOrNull?.getIntegerOrNull(LEVEL_TAG) ?: IBlockDeathFlowerDecaying.MIN_LEVEL
		}
		
		fun setDeathLevel(stack: ItemStack, level: Int){
			stack.heeTag.putInt(LEVEL_TAG, level)
		}
	}
	
	init{
		addPropertyOverride(Resource.Custom("death_level")){
			stack, _, _ -> getDeathLevel(stack).toFloat()
		}
	}
	
	// UPDATE fix flower pot metadata
	
	override fun fillItemGroup(tab: ItemGroup, items: NonNullList<ItemStack>){
		items.add(ItemStack(this).also { setDeathLevel(it, IBlockDeathFlowerDecaying.MIN_LEVEL) })
	}
}
