package chylex.hee.game.item
import chylex.hee.game.block.IBlockDeathFlowerDecaying
import chylex.hee.game.block.IBlockDeathFlowerDecaying.Companion.LEVEL
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.vanilla.ItemBlock
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.facades.Stats
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getIntegerOrNull
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.setState
import chylex.hee.system.util.with
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.SUCCESS
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
	
	override fun onItemUseFirst(stack: ItemStack, context: ItemUseContext): ActionResultType{
		val player = context.player
		val world = context.world
		val pos = context.pos
		
		if (player != null && pos.getBlock(world) === Blocks.FLOWER_POT){
			pos.setState(world, ModBlocks.POTTED_DEATH_FLOWER_DECAYING.with(LEVEL, getDeathLevel(stack)))
			
			if (!player.abilities.isCreativeMode){
				stack.shrink(1)
			}
			
			player.addStat(Stats.FLOWER_POTTED)
			return SUCCESS
		}
		
		return FAIL
	}
	
	override fun fillItemGroup(tab: ItemGroup, items: NonNullList<ItemStack>){
		if (isInGroup(tab)){
			items.add(ItemStack(this).also { setDeathLevel(it, IBlockDeathFlowerDecaying.MIN_LEVEL) })
		}
	}
}
