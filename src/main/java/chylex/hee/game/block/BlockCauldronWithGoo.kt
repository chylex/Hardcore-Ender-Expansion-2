package chylex.hee.game.block

import chylex.hee.game.Resource.location
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockStateModels
import chylex.hee.game.potion.brewing.PotionItems
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.potion.Potions
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockCauldronWithGoo(builder: BlockBuilder, private val goo: BlockAbstractGoo) : BlockAbstractCauldron(builder) {
	override val model
		get() = BlockStateModels.Cauldron(goo.location("_still"))
	
	override fun createFilledBucket(): ItemStack {
		return ItemStack(goo.fluid.filledBucket)
	}
	
	override fun createFilledBottle(): ItemStack {
		return PotionItems.getBottle(Items.POTION, Potions.THICK)
	}
	
	override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
		goo.onInsideGoo(entity)
	}
	
	override fun fillWithRain(world: World, pos: BlockPos) {}
}
