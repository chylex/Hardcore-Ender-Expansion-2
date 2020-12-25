package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.potion.brewing.PotionItems
import chylex.hee.system.migration.PotionTypes
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockCauldronWithGoo(builder: BlockBuilder, private val goo: BlockAbstractGoo) : BlockAbstractCauldron(builder) {
	override fun createFilledBucket(): ItemStack? {
		return ItemStack(goo.fluid.filledBucket)
	}
	
	override fun createFilledBottle(): ItemStack? {
		return PotionItems.getBottle(Items.POTION, PotionTypes.THICK)
	}
	
	override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
		goo.onInsideGoo(entity)
	}
	
	override fun fillWithRain(world: World, pos: BlockPos) {}
}
