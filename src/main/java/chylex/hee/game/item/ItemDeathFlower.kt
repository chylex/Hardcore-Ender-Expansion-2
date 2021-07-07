package chylex.hee.game.item

import chylex.hee.game.Resource
import chylex.hee.game.block.IBlockDeathFlowerDecaying
import chylex.hee.game.block.IBlockDeathFlowerDecaying.Companion.LEVEL
import chylex.hee.game.block.util.with
import chylex.hee.game.item.util.ItemProperty
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.setState
import chylex.hee.init.ModBlocks
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.nbt.getIntegerOrNull
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.stats.Stats
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.NonNullList

class ItemDeathFlower(block: Block, properties: Properties) : BlockItem(block, properties) {
	companion object {
		private const val LEVEL_TAG = "DeathLevel"
		
		val DEATH_LEVEL_PROPERTY = ItemProperty(Resource.Custom("death_level")) { stack ->
			getDeathLevel(stack).toFloat()
		}
		
		fun getDeathLevel(stack: ItemStack): Int {
			return stack.heeTagOrNull?.getIntegerOrNull(LEVEL_TAG) ?: IBlockDeathFlowerDecaying.MIN_LEVEL
		}
		
		fun setDeathLevel(stack: ItemStack, level: Int) {
			stack.heeTag.putInt(LEVEL_TAG, level)
		}
	}
	
	override fun onItemUseFirst(stack: ItemStack, context: ItemUseContext): ActionResultType {
		val player = context.player
		val world = context.world
		val pos = context.pos
		
		if (player != null && pos.getBlock(world) === Blocks.FLOWER_POT) {
			pos.setState(world, ModBlocks.POTTED_DEATH_FLOWER_DECAYING.with(LEVEL, getDeathLevel(stack)))
			
			if (!player.abilities.isCreativeMode) {
				stack.shrink(1)
			}
			
			player.addStat(Stats.POT_FLOWER)
			return SUCCESS
		}
		
		return PASS
	}
	
	override fun fillItemGroup(tab: ItemGroup, items: NonNullList<ItemStack>) {
		if (isInGroup(tab)) {
			items.add(ItemStack(this).also { setDeathLevel(it, IBlockDeathFlowerDecaying.MIN_LEVEL) })
		}
	}
}
