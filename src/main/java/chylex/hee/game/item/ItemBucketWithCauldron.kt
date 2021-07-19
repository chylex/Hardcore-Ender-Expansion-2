package chylex.hee.game.item

import chylex.hee.game.block.BlockAbstractCauldron
import chylex.hee.game.block.util.CAULDRON_LEVEL
import chylex.hee.game.block.util.with
import chylex.hee.game.fx.util.playServer
import chylex.hee.game.fx.util.playUniversal
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.setState
import chylex.hee.util.forge.supply
import net.minecraft.block.Blocks
import net.minecraft.block.CauldronBlock
import net.minecraft.fluid.Fluid
import net.minecraft.item.BucketItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.item.Items
import net.minecraft.stats.Stats
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents

open class ItemBucketWithCauldron(fluid: Fluid, private val cauldronBlock: CauldronBlock, properties: Properties) : BucketItem(supply(fluid), properties), IHeeItem {
	override fun onItemUse(context: ItemUseContext): ActionResultType {
		val world = context.world
		val pos = context.pos
		
		if (pos.getState(world).let { it.block === Blocks.CAULDRON && it[CAULDRON_LEVEL] == 0 }) {
			val player = context.player
			
			if (!world.isRemote) {
				if (player != null) {
					if (!player.abilities.isCreativeMode) {
						player.setHeldItem(context.hand, ItemStack(Items.BUCKET))
					}
					
					player.addStat(Stats.FILL_CAULDRON)
				}
				
				pos.setState(world, cauldronBlock.with(CAULDRON_LEVEL, BlockAbstractCauldron.MAX_LEVEL))
			}
			
			if (player == null) {
				SoundEvents.ITEM_BUCKET_EMPTY.playServer(world, pos, SoundCategory.BLOCKS)
			}
			else {
				SoundEvents.ITEM_BUCKET_EMPTY.playUniversal(player, pos, SoundCategory.BLOCKS)
			}
			
			return SUCCESS
		}
		
		return super.onItemUse(context)
	}
}
