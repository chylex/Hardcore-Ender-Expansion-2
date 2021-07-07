package chylex.hee.game.block

import chylex.hee.HEE
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.fx.util.playUniversal
import chylex.hee.game.item.util.nullIfEmpty
import chylex.hee.game.world.util.getBlock
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.math.Pos
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.CauldronBlock
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.stats.Stats
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.World
import net.minecraftforge.event.entity.player.EntityItemPickupEvent

abstract class BlockAbstractCauldron(builder: BlockBuilder) : CauldronBlock(builder.p) {
	@SubscribeAllEvents(modid = HEE.ID)
	companion object {
		const val MAX_LEVEL = 3
		
		@SubscribeEvent
		fun onEntityItemPickup(e: EntityItemPickupEvent) {
			val item = e.item
			val pos = Pos(item)
			
			if (pos.getBlock(item.world) is CauldronBlock && Pos(e.player) != pos) {
				e.isCanceled = true
			}
		}
	}
	
	protected abstract fun createFilledBucket(): ItemStack?
	protected abstract fun createFilledBottle(): ItemStack?
	
	override fun setWaterLevel(world: World, pos: BlockPos, state: BlockState, level: Int) {
		super.setWaterLevel(world, pos, if (level == 0) Blocks.CAULDRON.defaultState else state, level)
	}
	
	private fun useAndUpdateHeldItem(player: PlayerEntity, hand: Hand, newHeldItem: ItemStack) {
		val oldHeldItem = player.getHeldItem(hand)
		
		oldHeldItem.shrink(1)
		
		if (oldHeldItem.isEmpty) {
			player.setHeldItem(hand, newHeldItem)
		}
		else if (!player.inventory.addItemStackToInventory(newHeldItem)) {
			player.dropItem(newHeldItem, false)
		}
	}
	
	final override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult): ActionResultType {
		val item = player.getHeldItem(hand).nullIfEmpty?.item
		
		if (item == null) {
			return PASS
		}
		
		if (item === Items.BUCKET) {
			val filledBucket = createFilledBucket()
			
			if (filledBucket != null && state[LEVEL] == MAX_LEVEL) {
				if (!world.isRemote) {
					player.addStat(Stats.USE_CAULDRON)
					useAndUpdateHeldItem(player, hand, filledBucket)
					setWaterLevel(world, pos, state, 0)
				}
				
				SoundEvents.ITEM_BUCKET_FILL.playUniversal(player, pos, SoundCategory.BLOCKS)
			}
			
			return SUCCESS
		}
		else if (item === Items.GLASS_BOTTLE) {
			val filledBottle = createFilledBottle()
			
			if (filledBottle != null && state[LEVEL] > 0) {
				if (!world.isRemote) {
					player.addStat(Stats.USE_CAULDRON)
					useAndUpdateHeldItem(player, hand, filledBottle)
					setWaterLevel(world, pos, state, state[LEVEL] - 1)
				}
				
				SoundEvents.ITEM_BOTTLE_FILL.playUniversal(player, pos, SoundCategory.BLOCKS)
			}
			
			return SUCCESS
		}
		
		return PASS
	}
	
	override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {}
	override fun fillWithRain(world: World, pos: BlockPos) {}
}
