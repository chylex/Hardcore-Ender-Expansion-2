package chylex.hee.system

import chylex.hee.HEE
import chylex.hee.game.item.util.nbt
import chylex.hee.game.item.util.nbtOrNull
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getCompoundOrNull
import chylex.hee.util.nbt.getOrCreateCompound
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack

// NBT

val TagCompound.heeTag
	get() = this.getOrCreateCompound(HEE.ID)

val TagCompound.heeTagOrNull
	get() = this.getCompoundOrNull(HEE.ID)

// ItemStack

val ItemStack.heeTag: TagCompound
	get() = this.nbt.heeTag

val ItemStack.heeTagOrNull: TagCompound?
	get() = this.nbtOrNull?.heeTagOrNull

// Entity

val Entity.heeTag
	get() = this.persistentData.heeTag

val Entity.heeTagOrNull
	get() = this.persistentData.heeTagOrNull

val Entity.heeTagPersistent
	get() = this.persistentData.getOrCreateCompound(PlayerEntity.PERSISTED_NBT_TAG).heeTag

val Entity.heeTagPersistentOrNull
	get() = this.persistentData.getCompoundOrNull(PlayerEntity.PERSISTED_NBT_TAG)?.heeTagOrNull
