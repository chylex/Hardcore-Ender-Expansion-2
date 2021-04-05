package chylex.hee.game.world.territory.storage.data

import chylex.hee.game.world.math.BoundingBox
import chylex.hee.game.world.playPlayer
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.storage.TerritoryStorageComponent
import chylex.hee.init.ModSounds
import chylex.hee.system.delegate.NotifyOnChange
import chylex.hee.system.math.floorToInt
import chylex.hee.system.random.nextFloat
import chylex.hee.system.serialization.NBTList.Companion.putList
import chylex.hee.system.serialization.NBTObjectList
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getAABBOrNull
import chylex.hee.system.serialization.getIntegerOrNull
import chylex.hee.system.serialization.getListOfLongArrays
import chylex.hee.system.serialization.putAABB
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Difficulty.EASY
import net.minecraft.world.Difficulty.HARD
import net.minecraft.world.Difficulty.NORMAL
import net.minecraft.world.World
import kotlin.math.max
import kotlin.random.asKotlinRandom

class ForgottenTombsEndData(markDirty: () -> Unit) : TerritoryStorageComponent() {
	private companion object {
		private const val ROOM_AABB_TAG = "AABB"
		private const val END_SEQUENCE_TICKS_TAG = "Ticks"
		private const val UNDREADS_TO_ACTIVATE_TAG = "UndreadsToActivate"
		private const val IS_PORTAL_ACTIVATED_TAG = "PortalActivated"
		private const val GRAVE_DIRT_AREAS_TAG = "GraveDirtAreas"
		private const val GRAVE_DIRT_UNDREADS_TAG = "GraveDirtUndreads"
		private const val GRAVE_DIRT_INDEX_TAG = "GraveDirtIndex"
	}
	
	var roomAABB: AxisAlignedBB? by NotifyOnChange(null, markDirty)
		private set
	
	var endSequenceTicks: Int? by NotifyOnChange(null, markDirty)
	var undreadsToActivate: Int? by NotifyOnChange(null, markDirty)
	var isPortalActivated: Boolean by NotifyOnChange(false, markDirty)
	
	private val remainingGraveDirtAreas = mutableListOf<BoundingBox>()
	private var remainingGraveDirtUndreads = intArrayOf()
	private var remainingGraveDirtIndex by NotifyOnChange(0, markDirty)
	
	fun setupRoom(aabb: AxisAlignedBB, graveDirtAreas: MutableList<BoundingBox>) {
		remainingGraveDirtAreas.clear()
		remainingGraveDirtAreas.addAll(graveDirtAreas)
		roomAABB = aabb
	}
	
	fun startEndSequence(world: World, instance: TerritoryInstance, soundPos: Vec3d) {
		if (endSequenceTicks == null) {
			endSequenceTicks = 0
			
			for (player in instance.players) {
				ModSounds.AMBIENT_FORGOTTEN_TOMBS_END_TRIGGER.playPlayer(player, soundPos, SoundCategory.NEUTRAL)
			}
			
			if (remainingGraveDirtAreas.isNotEmpty()) {
				val rand = world.rand
				
				val doubleUndreadRatio = when (world.difficulty) {
					EASY   -> rand.nextFloat(0.28F, 0.36F)
					NORMAL -> rand.nextFloat(0.57F, 0.65F)
					HARD   -> rand.nextFloat(0.75F, 0.84F)
					else   -> 0F
				}
				
				val total = remainingGraveDirtAreas.size
				val double = (total * doubleUndreadRatio).floorToInt()
				val single = max(0, total - double - 2)
				val empty = total - double - single
				
				remainingGraveDirtUndreads = IntArray(double) { 2 } + IntArray(single) { 1 } + IntArray(empty) { 0 }
				undreadsToActivate = (remainingGraveDirtUndreads.sum() * 0.75F).floorToInt()
				
				remainingGraveDirtAreas.shuffle(rand)
				remainingGraveDirtUndreads.shuffle(rand.asKotlinRandom())
				remainingGraveDirtIndex = 0
			}
		}
	}
	
	fun nextUndreadSpawn(): Pair<BoundingBox, Int>? {
		val boundingBox = remainingGraveDirtAreas.getOrNull(remainingGraveDirtIndex) ?: return null
		val amount = remainingGraveDirtUndreads.getOrNull(remainingGraveDirtIndex) ?: return null
		
		++remainingGraveDirtIndex
		return boundingBox to amount
	}
	
	fun onUndreadDied() {
		val remaining = undreadsToActivate
		if (remaining != null) {
			undreadsToActivate = remaining - 1
		}
	}
	
	override fun serializeNBT() = TagCompound().apply {
		roomAABB?.let { putAABB(ROOM_AABB_TAG, it) }
		endSequenceTicks?.let { putInt(END_SEQUENCE_TICKS_TAG, it) }
		undreadsToActivate?.let { putInt(UNDREADS_TO_ACTIVATE_TAG, it) }
		putBoolean(IS_PORTAL_ACTIVATED_TAG, isPortalActivated)
		
		putList(GRAVE_DIRT_AREAS_TAG, NBTObjectList.of(remainingGraveDirtAreas.map { longArrayOf(it.min.toLong(), it.max.toLong()) }))
		putIntArray(GRAVE_DIRT_UNDREADS_TAG, remainingGraveDirtUndreads)
		putInt(GRAVE_DIRT_INDEX_TAG, remainingGraveDirtIndex)
	}
	
	override fun deserializeNBT(nbt: TagCompound) = with(nbt) {
		roomAABB = getAABBOrNull(ROOM_AABB_TAG)
		endSequenceTicks = getIntegerOrNull(END_SEQUENCE_TICKS_TAG)
		undreadsToActivate = getIntegerOrNull(UNDREADS_TO_ACTIVATE_TAG)
		isPortalActivated = getBoolean(IS_PORTAL_ACTIVATED_TAG)
		
		remainingGraveDirtAreas.clear()
		remainingGraveDirtAreas.addAll(getListOfLongArrays(GRAVE_DIRT_AREAS_TAG).map { BoundingBox(BlockPos.fromLong(it[0]), BlockPos.fromLong(it[1])) })
		remainingGraveDirtUndreads = getIntArray(GRAVE_DIRT_UNDREADS_TAG)
		remainingGraveDirtIndex = getInt(GRAVE_DIRT_INDEX_TAG)
	}
}
