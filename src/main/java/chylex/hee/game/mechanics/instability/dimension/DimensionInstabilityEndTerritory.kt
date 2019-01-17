package chylex.hee.game.mechanics.instability.dimension
import chylex.hee.game.mechanics.instability.dimension.components.EndermiteSpawnLogicEndTerritory
import chylex.hee.game.world.territory.TerritoryInstance
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class DimensionInstabilityEndTerritory(private val world: World) : IDimensionInstability{
	private val territories = Int2ObjectOpenHashMap<DimensionInstabilityGlobal>()
	
	private fun putEntry(key: Int): DimensionInstabilityGlobal{
		return DimensionInstabilityGlobal(world, EndermiteSpawnLogicEndTerritory).also { territories[key] = it }
	}
	
	private fun getEntry(pos: BlockPos): IDimensionInstability{
		val instance = TerritoryInstance.fromPos(pos) ?: return DimensionInstabilityNull
		val key = instance.hash
		
		return territories[key] ?: putEntry(key)
	}
	
	override fun resetActionMultiplier(pos: BlockPos){
		getEntry(pos).resetActionMultiplier(pos)
	}
	
	override fun triggerAction(amount: UShort, pos: BlockPos){
		getEntry(pos).triggerAction(amount, pos)
	}
	
	override fun triggerRelief(amount: UShort, pos: BlockPos){
		getEntry(pos).triggerRelief(amount, pos)
	}
	
	override fun serializeNBT() = NBTTagCompound().apply {
		for(entry in territories.int2ObjectEntrySet()){
			setTag(entry.intKey.toString(), entry.value.serializeNBT())
		}
	}
	
	override fun deserializeNBT(nbt: NBTTagCompound){
		for(key in nbt.keySet){
			val keyInt = key.toIntOrNull() ?: continue
			putEntry(keyInt).deserializeNBT(nbt.getCompoundTag(key))
		}
	}
}
