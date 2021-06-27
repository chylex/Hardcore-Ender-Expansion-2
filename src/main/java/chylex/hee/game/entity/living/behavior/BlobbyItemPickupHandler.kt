package chylex.hee.game.entity.living.behavior

import chylex.hee.game.entity.living.EntityMobBlobby
import chylex.hee.game.entity.living.ai.AIPickUpItemDetour.IItemPickupHandler
import chylex.hee.game.entity.posVec
import chylex.hee.game.inventory.nullIfEmpty
import chylex.hee.game.world.playServer
import chylex.hee.system.migration.Sounds
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.serialization.NBTList.Companion.putList
import chylex.hee.system.serialization.NBTObjectList
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getListOfStrings
import chylex.hee.system.serialization.use
import net.minecraft.item.ItemStack
import net.minecraft.util.SoundCategory
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
		
		Sounds.ENTITY_ITEM_PICKUP.playServer(blobby.world, blobby.posVec, SoundCategory.NEUTRAL, volume = 0.22F, pitch = rand.nextFloat(0.6F, 3.4F))
		
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
