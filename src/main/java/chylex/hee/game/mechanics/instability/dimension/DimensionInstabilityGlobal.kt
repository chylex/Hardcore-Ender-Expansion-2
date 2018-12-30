package chylex.hee.game.mechanics.instability.dimension
import chylex.hee.game.mechanics.instability.dimension.components.EndermiteSpawnLogic
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.remapRange
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.max
import kotlin.math.pow

open class DimensionInstabilityGlobal(private val world: World, private val endermiteSpawnLogic: EndermiteSpawnLogic) : IDimensionInstability{
	private companion object{
		private fun calculatePassiveRelief(ticksSinceLastAction: Long): UShort{
			return (5 * (ticksSinceLastAction / (20L * 20L))).toUShort()
		}
		
		private fun calculateActionMultiplier(ticksSinceFullRelief: Long): Float{
			return if (ticksSinceFullRelief < 300L)
				remapRange(ticksSinceFullRelief.toFloat(), (0F)..(300F), (0.2F)..(1F))
			else
				1F
		}
		
		private fun calculateExtraLevelRequired(livingEndermites: Int): Int{
			return (25F * livingEndermites.toFloat().pow(0.565F)).floorToInt()
		}
	}
	
	private var level = 0
	
	private var lastActionTime = 0L
	private var lastFullReliefTime = -300L
	
	override fun triggerAction(amount: UShort, pos: BlockPos){
		val currentTime = world.totalWorldTime
		val ticksSinceLastAction = currentTime - lastActionTime
		val ticksSinceFullRelief = currentTime - lastFullReliefTime
		
		triggerRelief(calculatePassiveRelief(ticksSinceLastAction), pos)
		lastActionTime = currentTime
		
		level += (amount.toInt() * calculateActionMultiplier(ticksSinceFullRelief)).ceilToInt()
		
		if (level >= 200 && level >= 200 + calculateExtraLevelRequired(endermiteSpawnLogic.countExisting(world, pos))){
			if (endermiteSpawnLogic.trySpawnNear(world, pos)){
				triggerReliefMultiplier(0.9F)
				lastFullReliefTime = currentTime
			}
			else{
				triggerReliefMultiplier(0.45F)
			}
		}
	}
	
	override fun triggerRelief(amount: UShort, pos: BlockPos){
		level = max(0, level - amount.toInt())
	}
	
	private fun triggerReliefMultiplier(multiplier: Float){
		level -= (level * multiplier).floorToInt()
	}
	
	// Serialization
	
	override fun serializeNBT() = NBTTagCompound().apply {
		setInteger("Level", level)
		setLong("LastAction", lastActionTime)
		setLong("LastFullRelief", lastFullReliefTime)
	}
	
	override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
		level = getInteger("Level")
		lastActionTime = getLong("LastAction")
		lastFullReliefTime = getLong("LastFullRelief")
	}
}