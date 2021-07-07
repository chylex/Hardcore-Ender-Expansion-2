package chylex.hee.game.territory.storage

import chylex.hee.game.territory.TerritoryVoid
import chylex.hee.game.territory.system.storage.TerritoryStorageComponent
import chylex.hee.util.delegate.NotifyOnChange
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getFloatOrNull
import chylex.hee.util.nbt.getLongOrNull
import chylex.hee.util.nbt.use
import kotlin.math.min

class VoidData(markDirty: () -> Unit) : TerritoryStorageComponent() {
	private companion object {
		private const val FACTOR_TAG = "Factor"
		private const val IS_CORRUPTING_TAG = "IsCorrupting"
		private const val LAST_CORRUPTION_TIME = "LastCorruptionTime"
		
		private const val CORRUPTION_DIFFERENCE = TerritoryVoid.RARE_TERRITORY_MAX_CORRUPTION_FACTOR - TerritoryVoid.RARE_TERRITORY_START_CORRUPTION_FACTOR
		private const val CORRUPTION_PER_TICK_EMPTY = CORRUPTION_DIFFERENCE / (20F * 1500F)
		private const val CORRUPTION_PER_TICK_WITH_PLAYERS = CORRUPTION_DIFFERENCE / (20F * 600F)
	}
	
	var voidFactor: Float by NotifyOnChange(TerritoryVoid.OUTSIDE_VOID_FACTOR, markDirty)
		private set
	
	var isCorrupting: Boolean by NotifyOnChange(false, markDirty)
		private set
	
	private var lastCorruptionTime = Long.MIN_VALUE
	
	fun startCorrupting(): Boolean {
		if (isCorrupting) {
			return false
		}
		
		isCorrupting = true
		voidFactor = TerritoryVoid.RARE_TERRITORY_START_CORRUPTION_FACTOR
		return true
	}
	
	fun onCorruptionTick(currentTime: Long) {
		if (lastCorruptionTime != Long.MIN_VALUE) {
			val timeDifference = currentTime - lastCorruptionTime
			
			// if the data has not been updated for longer than one tick,
			// it means no players have been in the territory so we catch up
			
			if (timeDifference > 1L) {
				voidFactor = min(voidFactor + CORRUPTION_PER_TICK_EMPTY * (timeDifference - 1L), TerritoryVoid.RARE_TERRITORY_MAX_CORRUPTION_FACTOR)
			}
		}
		
		lastCorruptionTime = currentTime
		voidFactor = min(voidFactor + CORRUPTION_PER_TICK_WITH_PLAYERS, TerritoryVoid.RARE_TERRITORY_MAX_CORRUPTION_FACTOR)
	}
	
	override fun serializeNBT() = TagCompound().apply {
		if (voidFactor != TerritoryVoid.OUTSIDE_VOID_FACTOR) {
			putFloat(FACTOR_TAG, voidFactor)
		}
		
		if (isCorrupting) {
			putBoolean(IS_CORRUPTING_TAG, true)
			putLong(LAST_CORRUPTION_TIME, lastCorruptionTime)
		}
	}
	
	override fun deserializeNBT(nbt: TagCompound) = nbt.use {
		voidFactor = getFloatOrNull(FACTOR_TAG) ?: TerritoryVoid.OUTSIDE_VOID_FACTOR
		isCorrupting = getBoolean(IS_CORRUPTING_TAG)
		lastCorruptionTime = getLongOrNull(LAST_CORRUPTION_TIME) ?: Long.MIN_VALUE
	}
}
