package chylex.hee.game.item

import chylex.hee.game.block.entity.TileEntityMinersBurialAltar
import chylex.hee.game.fx.util.playUniversal
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.game.world.util.getTile
import chylex.hee.init.ModSounds
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.SoundCategory

class ItemPuzzleMedallion(properties: Properties) : HeeItem(properties) {
	override fun onItemUse(context: ItemUseContext): ActionResultType {
		val player = context.player ?: return FAIL
		val world = context.world
		val pos = context.pos
		
		val heldItem = player.getHeldItem(context.hand)
		
		if (!BlockEditor.canEdit(pos, player, heldItem)) {
			return FAIL
		}
		
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
