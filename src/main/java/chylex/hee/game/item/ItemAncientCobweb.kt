package chylex.hee.game.item

import chylex.hee.game.Environment
import chylex.hee.game.entity.util.motionY
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.item.builder.HeeBlockItemBuilder
import chylex.hee.game.item.components.ITickInInventoryComponent
import chylex.hee.game.item.util.size
import chylex.hee.util.math.center
import chylex.hee.util.random.nextInt
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.item.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootParameterSets
import net.minecraft.loot.LootParameters
import net.minecraft.util.Hand.OFF_HAND
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import kotlin.math.min

class ItemAncientCobweb(block: Block) : HeeBlockItemBuilder(block) {
	init {
		components.tickInInventory.add(object : ITickInInventoryComponent {
			override fun tick(world: World, entity: Entity, stack: ItemStack, slot: Int, isSelected: Boolean) {
				if (world.isRemote || world.gameTime % 4L != 0L) {
					return
				}
				
				if (!(isSelected || (entity is LivingEntity && entity.getHeldItem(OFF_HAND) === stack))) {
					return
				}
				
				if (entity is PlayerEntity && (entity.isCreative || entity.isSpectator)) {
					return
				}
				
				val lootTable = Environment.getLootTable(block.lootTable)
				val lootContext = LootContext.Builder(world as ServerWorld)
					.withRandom(world.rand)
					.withParameter(LootParameters.BLOCK_STATE, block.defaultState)
					.withParameter(LootParameters.ORIGIN, BlockPos.ZERO.center)
					.withParameter(LootParameters.TOOL, ItemStack.EMPTY)
					.build(LootParameterSets.BLOCK)
				
				val itemsDisintegrated = min(stack.size, world.rand.nextInt(0, 2))
				stack.shrink(itemsDisintegrated)
				
				repeat(itemsDisintegrated) {
					val drops = lootTable.generate(lootContext)
					val front = entity.posVec.add(entity.lookVec.scale(0.6))
					
					for (drop in drops) {
						ItemEntity(world, front.x, entity.posY + entity.height * 0.45, front.z, drop).apply {
							motionY = 0.0
							setDefaultPickupDelay()
							world.addEntity(this)
						}
					}
				}
			}
		})
	}
}
