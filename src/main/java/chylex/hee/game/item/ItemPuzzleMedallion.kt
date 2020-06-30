package chylex.hee.game.item
import chylex.hee.game.block.entity.TileEntityMinersBurialAltar
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.system.migration.ActionResult.FAIL
import chylex.hee.system.migration.ActionResult.PASS
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.getTile
import chylex.hee.system.util.playUniversal
import net.minecraft.item.Item
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.SoundCategory

class ItemPuzzleMedallion(properties: Properties) : Item(properties){
	override fun onItemUse(context: ItemUseContext): ActionResultType{
		val player = context.player ?: return FAIL
		val world = context.world
		val pos = context.pos
		
		val heldItem = player.getHeldItem(context.hand)
		
		if (!BlockEditor.canEdit(pos, player, heldItem)){
			return FAIL
		}
		
		val tile = pos.getTile<TileEntityMinersBurialAltar>(world)
		
		if (tile != null && !tile.hasMedallion){
			tile.hasMedallion = true
			heldItem.shrink(1)
			
			Sounds.BLOCK_STONE_HIT.playUniversal(player, pos, SoundCategory.BLOCKS, volume = 2F, pitch = 0.8F)
			return SUCCESS
		}
		
		return PASS
	}
}
