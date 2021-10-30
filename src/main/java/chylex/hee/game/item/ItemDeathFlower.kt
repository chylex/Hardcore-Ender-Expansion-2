package chylex.hee.game.item

import chylex.hee.game.Resource
import chylex.hee.game.block.IBlockDeathFlowerDecaying
import chylex.hee.game.block.IBlockDeathFlowerDecaying.Companion.LEVEL
import chylex.hee.game.block.util.with
import chylex.hee.game.item.builder.HeeBlockItemBuilder
import chylex.hee.game.item.components.IBeforeUseItemOnBlockComponent
import chylex.hee.game.item.components.ICreativeTabComponent
import chylex.hee.game.item.util.ItemProperty
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.setState
import chylex.hee.init.ModBlocks
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.nbt.getIntegerOrNull
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.stats.Stats
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ItemDeathFlower(block: Block) : HeeBlockItemBuilder(block) {
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
	
	init {
		properties.add(DEATH_LEVEL_PROPERTY)
		
		components.creativeTab = ICreativeTabComponent { tab, item ->
			tab.add(ItemStack(item).also { setDeathLevel(it, IBlockDeathFlowerDecaying.MIN_LEVEL) })
		}
		
		components.beforeUseOnBlock = object : IBeforeUseItemOnBlockComponent {
			override fun beforeUse(world: World, pos: BlockPos, context: ItemUseContext, stack: ItemStack): ActionResultType {
				val player = context.player
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
		}
	}
}
