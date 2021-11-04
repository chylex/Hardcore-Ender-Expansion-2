package chylex.hee.game.territory.storage

import chylex.hee.game.fx.util.playPlayer
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.game.territory.system.storage.TerritoryStorageComponent
import chylex.hee.init.ModSounds
import chylex.hee.util.delegate.NotifyOnChange
import chylex.hee.util.math.BoundingBox
import chylex.hee.util.math.Pos
import chylex.hee.util.math.floorToInt
import chylex.hee.util.nbt.NBTObjectList
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getAABBOrNull
import chylex.hee.util.nbt.getIntegerOrNull
import chylex.hee.util.nbt.getListOfLongArrays
import chylex.hee.util.nbt.putAABB
import chylex.hee.util.nbt.putList
import chylex.hee.util.random.nextFloat
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.vector.Vector3d
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
	
	fun startEndSequence(world: World, instance: TerritoryInstance, soundPos: Vector3d) {
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
		remainingGraveDirtAreas.addAll(getListOfLongArrays(GRAVE_DIRT_AREAS_TAG).map { BoundingBox(Pos(it[0]), Pos(it[1])) })
		remainingGraveDirtUndreads = getIntArray(GRAVE_DIRT_UNDREADS_TAG)
		remainingGraveDirtIndex = getInt(GRAVE_DIRT_INDEX_TAG)
	}
}
