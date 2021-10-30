package chylex.hee.game.item

import chylex.hee.game.block.IBlockDeathFlowerDecaying
import chylex.hee.game.entity.item.EntityItemCauldronTrigger
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.IItemEntityComponent
import chylex.hee.game.item.components.PlayerUseItemOnBlockComponent
import chylex.hee.game.world.util.getBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object ItemEndPowder : HeeItemBuilder() {
	init {
		includeFrom(ItemDust)
		
		components.useOnBlock = object : PlayerUseItemOnBlockComponent() {
			override fun use(world: World, pos: BlockPos, player: PlayerEntity, heldItem: ItemStack, context: ItemUseContext): ActionResultType {
				val block = pos.getBlock(world)
				if (block is IBlockDeathFlowerDecaying) {
					if (!world.isRemote) {
						block.healDeathFlower(world, pos)
					}
					
					heldItem.shrink(1)
					return SUCCESS
				}
				
				return PASS
			}
		}
		
		components.itemEntity = IItemEntityComponent.fromConstructor(::EntityItemCauldronTrigger)
	}
}
