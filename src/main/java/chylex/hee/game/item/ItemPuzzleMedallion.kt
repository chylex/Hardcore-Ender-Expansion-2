package chylex.hee.game.item

import chylex.hee.game.block.entity.TileEntityMinersBurialAltar
import chylex.hee.game.fx.util.playUniversal
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.PlayerUseItemOnBlockComponent
import chylex.hee.game.world.util.getTile
import chylex.hee.init.ModSounds
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object ItemPuzzleMedallion : HeeItemBuilder() {
	init {
		components.useOnBlock = object : PlayerUseItemOnBlockComponent() {
			override fun use(world: World, pos: BlockPos, player: PlayerEntity, heldItem: ItemStack, context: ItemUseContext): ActionResultType {
				val tile = pos.getTile<TileEntityMinersBurialAltar>(world)
				if (tile != null && !tile.hasMedallion) {
					tile.hasMedallion = true
					heldItem.shrink(1)
					
					ModSounds.ITEM_PUZZLE_MEDALLION_INSERT.playUniversal(player, pos, SoundCategory.BLOCKS, volume = 2F, pitch = 0.8F)
					return SUCCESS
				}
				
				return PASS
			}
		}
	}
}
