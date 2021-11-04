package chylex.hee.game.entity.living.behavior

import chylex.hee.game.entity.living.EntityMobBlobby
import chylex.hee.game.entity.living.ai.AIPickUpItemDetour.IItemPickupHandler
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.fx.util.playServer
import chylex.hee.game.item.util.nullIfEmpty
import chylex.hee.util.nbt.NBTObjectList
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getListOfStrings
import chylex.hee.util.nbt.putList
import chylex.hee.util.nbt.use
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextInt
import net.minecraft.item.ItemStack
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraftforge.common.Tags
import net.minecraftforge.common.util.INBTSerializable

class BlobbyItemPickupHandler(private val blobby: EntityMobBlobby) : IItemPickupHandler, INBTSerializable<TagCompound> {
	private companion object {
		private const val DELAY_TAG = "Delay"
		private const val BANS_TAG = "Bans"
	}
	
	private val rand
		get() = blobby.rng
	
	var catchUpBonusTicks = 0
		private set
	
	private var delay = 0
	private val bans = mutableSetOf<String>()
	
	fun update() {
		if (delay > 0) {
			--delay
		}
		
		if (catchUpBonusTicks > 0) {
			--catchUpBonusTicks
		}
	}
	
	fun banItem(registryKey: String) {
		bans.add(registryKey)
	}
	
	override fun canPickUp(stack: ItemStack): Boolean {
		val heldStack = blobby.heldItem
		val heldItem = heldStack.item
		
		if (heldItem.isIn(Tags.Items.SLIMEBALLS)) {
			return false
		}
		
		val candidateItem = stack.item
		
		if (heldItem.item === candidateItem || bans.contains(candidateItem.registryName!!.toString())) {
			return false
		}
		
		return candidateItem.isIn(Tags.Items.SLIMEBALLS) || (delay == 0 && (heldStack.isEmpty || rand.nextInt(7) == 0))
	}
	
	override fun onPickUp(stack: ItemStack) {
		blobby.heldItem.nullIfEmpty?.let(blobby::entityDropItem)
		blobby.heldItem = stack
		
		SoundEvents.ENTITY_ITEM_PICKUP.playServer(blobby.world, blobby.posVec, SoundCategory.NEUTRAL, volume = 0.22F, pitch = rand.nextFloat(0.6F, 3.4F))
		
		delay = rand.nextInt(10, 3800)
		catchUpBonusTicks = rand.nextInt(80, 90)
	}
	
	override fun serializeNBT() = TagCompound().apply {
		putInt(DELAY_TAG, delay)
		putList(BANS_TAG, NBTObjectList.of(bans))
	}
	
	override fun deserializeNBT(nbt: TagCompound) = nbt.use {
		delay = getInt(DELAY_TAG)
		
		bans.clear()
		bans.addAll(getListOfStrings(BANS_TAG))
	}
}
