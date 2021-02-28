package chylex.hee.game.item

import chylex.hee.game.block.entity.TileEntityMinersBurialAltar
import chylex.hee.game.item.components.UseOnBlockComponent
import chylex.hee.game.world.BlockEditor
import chylex.hee.game.world.getTile
import chylex.hee.game.world.playUniversal
import chylex.hee.init.ModSounds
import chylex.hee.system.migration.ActionResult.FAIL
import chylex.hee.system.migration.ActionResult.PASS
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ItemPuzzleMedallion(properties: Properties) : ItemWithComponents(properties) {
	init {
		components.attach(object : UseOnBlockComponent {
			override fun useOnBlock(world: World, pos: BlockPos, player: EntityPlayer, item: ItemStack, ctx: ItemUseContext): ActionResultType {
				if (!BlockEditor.canEdit(pos, player, item)) {
					return FAIL
				}
				
				val tile = pos.getTile<TileEntityMinersBurialAltar>(world)
				
				if (tile != null && !tile.hasMedallion) {
					tile.hasMedallion = true
					item.shrink(1)
					
					ModSounds.ITEM_PUZZLE_MEDALLION_INSERT.playUniversal(player, pos, SoundCategory.BLOCKS, volume = 2F, pitch = 0.8F)
					return SUCCESS
				}
				
				return PASS
			}
		})
	}
}
