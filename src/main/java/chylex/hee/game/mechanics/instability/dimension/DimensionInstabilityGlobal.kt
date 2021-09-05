package chylex.hee.game.mechanics.instability.dimension

import chylex.hee.game.mechanics.instability.dimension.components.EndermiteSpawnLogic
import chylex.hee.util.math.ceilToInt
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.range
import chylex.hee.util.math.remapRange
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import kotlin.math.max
import kotlin.math.pow

open class DimensionInstabilityGlobal(private val world: World, private val endermiteSpawnLogic: EndermiteSpawnLogic) : IDimensionInstability {
	private companion object {
		private const val INITIAL_ENDERMITE_SPAWN_TIME = -300L
		
		private const val LEVEL_TAG = "Level"
		private const val LAST_ACTION_TAG = "LastAction"
		private const val LAST_ENDERMITE_SPAWN_TAG = "LastEndermiteSpawn"
		
		private fun calculatePassiveRelief(ticksSinceLastAction: Long): UShort {
			return (5 * (ticksSinceLastAction / (20L * 20L))).toUShort()
		}
		
		private fun calculateActionMultiplier(ticksSinceEndermiteSpawn: Long): Float {
			return if (ticksSinceEndermiteSpawn < 300L)
				remapRange(ticksSinceEndermiteSpawn.toFloat(), range(0F, 300F), range(0.2F, 1F))
			else
				1F
		}
		
		private fun calculateExtraLevelRequired(livingEndermites: Int): Int {
			return (25F * livingEndermites.toFloat().pow(0.565F)).floorToInt()
		}
	}
	
	init {
		require(world is ServerWorld) { "[DimensionInstabilityGlobal] world must be a server world" }
	}
	
	private var level = 0
	
	private var lastActionTime = 0L
	private var lastEndermiteSpawnTime = INITIAL_ENDERMITE_SPAWN_TIME
	
	override fun getLevel(pos: BlockPos): Int {
		return level
	}
	
	override fun resetActionMultiplier(pos: BlockPos) {
		lastEndermiteSpawnTime = INITIAL_ENDERMITE_SPAWN_TIME
	}
	
	override fun triggerAction(amount: UShort, pos: BlockPos) {
		val currentTime = world.gameTime
		val ticksSinceLastAction = currentTime - lastActionTime
		val ticksSinceEndermiteSpawn = currentTime - lastEndermiteSpawnTime
		
		triggerRelief(calculatePassiveRelief(ticksSinceLastAction), pos)
		lastActionTime = currentTime
		
		level += (amount.toInt() * calculateActionMultiplier(ticksSinceEndermiteSpawn)).ceilToInt()
		
		if (level >= 200 && level >= 200 + calculateExtraLevelRequired(endermiteSpawnLogic.countExisting(world as ServerWorld, pos))) {
			if (endermiteSpawnLogic.trySpawnNear(world, pos)) {
				triggerReliefMultiplier(0.9F)
				lastEndermiteSpawnTime = currentTime
			}
			else {
				triggerReliefMultiplier(0.45F)
			}
		}
	}
	
	override fun triggerRelief(amount: UShort, pos: BlockPos) {
		level = max(0, level - amount.toInt())
	}
	
	private fun triggerReliefMultiplier(multiplier: Float) {
		level -= (level * multiplier).floorToInt()
	}
	
	// Serialization
	
	override fun serializeNBT() = TagCompound().apply {
		putInt(LEVEL_TAG, level)
		putLong(LAST_ACTION_TAG, lastActionTime)
		putLong(LAST_ENDERMITE_SPAWN_TAG, lastEndermiteSpawnTime)
	}
	
	override fun deserializeNBT(nbt: TagCompound) = nbt.use {
		level = getInt(LEVEL_TAG)
		lastActionTime = getLong(LAST_ACTION_TAG)
		lastEndermiteSpawnTime = getLong(LAST_ENDERMITE_SPAWN_TAG)
	}
}
