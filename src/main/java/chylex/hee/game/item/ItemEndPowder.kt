package chylex.hee.game.item

import chylex.hee.game.block.IBlockDeathFlowerDecaying
import chylex.hee.game.entity.item.EntityItemCauldronTrigger
import chylex.hee.game.item.components.UseOnBlockComponent
import chylex.hee.game.world.BlockEditor
import chylex.hee.game.world.getBlock
import chylex.hee.system.migration.ActionResult.FAIL
import chylex.hee.system.migration.ActionResult.PASS
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.EntityPlayer
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResultType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ItemEndPowder(properties: Properties) : ItemWithComponents(properties), UseOnBlockComponent {
	init {
		components.attach(this)
	}
	
	override fun useOnBlock(world: World, pos: BlockPos, player: EntityPlayer, item: ItemStack, ctx: ItemUseContext): ActionResultType? {
		if (!BlockEditor.canEdit(pos, player, item)) {
			return FAIL
		}
		
		val block = pos.getBlock(world)
		
		if (block is IBlockDeathFlowerDecaying) {
			if (!world.isRemote) {
				block.healDeathFlower(world, pos)
			}
			
			item.shrink(1)
			return SUCCESS
		}
		
		return PASS
	}
	
	override fun hasCustomEntity(stack: ItemStack): Boolean {
		return true
	}
	
	override fun createEntity(world: World, replacee: Entity, stack: ItemStack): Entity {
		return EntityItemCauldronTrigger(world, stack, replacee)
	}
}
