package chylex.hee.game.item

import chylex.hee.game.block.BlockAbstractCauldron
import chylex.hee.game.block.with
import chylex.hee.game.world.getState
import chylex.hee.game.world.playServer
import chylex.hee.game.world.playUniversal
import chylex.hee.game.world.setState
import chylex.hee.system.facades.Stats
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.BlockCauldron
import chylex.hee.system.migration.ItemBucket
import chylex.hee.system.migration.Sounds
import chylex.hee.system.migration.supply
import net.minecraft.block.Blocks
import net.minecraft.fluid.Fluid
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.item.Items
import net.minecraft.util.ActionResultType
import net.minecraft.util.SoundCategory

open class ItemBucketWithCauldron(fluid: Fluid, private val cauldronBlock: BlockCauldron, properties: Properties) : ItemBucket(supply(fluid), properties) {
	override fun onItemUse(context: ItemUseContext): ActionResultType {
		val world = context.world
		val pos = context.pos
		
		if (pos.getState(world).let { it.block === Blocks.CAULDRON && it[BlockCauldron.LEVEL] == 0 }) {
			val player = context.player
			
			if (!world.isRemote) {
				if (player != null) {
					if (!player.abilities.isCreativeMode) {
						player.setHeldItem(context.hand, ItemStack(Items.BUCKET))
					}
					
					player.addStat(Stats.CAULDRON_FILLED)
				}
				
				pos.setState(world, cauldronBlock.with(BlockCauldron.LEVEL, BlockAbstractCauldron.MAX_LEVEL))
			}
			
			if (player == null) {
				Sounds.ITEM_BUCKET_EMPTY.playServer(world, pos, SoundCategory.BLOCKS)
			}
			else {
				Sounds.ITEM_BUCKET_EMPTY.playUniversal(player, pos, SoundCategory.BLOCKS)
			}
			
			return SUCCESS
		}
		
		return super.onItemUse(context)
	}
}
