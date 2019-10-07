package chylex.hee.game.mechanics.instability.region
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.entity.living.EntityMobEndermiteInstability
import chylex.hee.game.mechanics.instability.region.components.ClusterLeakLogic
import chylex.hee.game.mechanics.instability.region.components.EndermiteTeleportLogic
import chylex.hee.game.mechanics.instability.region.entry.IRegionEntry
import chylex.hee.game.mechanics.instability.region.entry.IRegionEntryConstructor
import chylex.hee.game.mechanics.instability.region.entry.RegionEntryMap
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.TagLongArray
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.nextInt
import net.minecraft.util.ITickable
import net.minecraft.world.World
import net.minecraftforge.common.util.INBTSerializable
import java.util.Random
import kotlin.math.min

class RegionInstability<T : IRegionEntry>(private val world: World, private val entryConstructor: IRegionEntryConstructor<T>) : ITickable, INBTSerializable<TagCompound>{
	private companion object{
		private const val POINTS_TO_TRIGGER = 500
		private const val TELEPORTS_TO_CHAOS = 16
	}
	
	private val rand = Random()
	
	private val entries = RegionEntryMap(entryConstructor)
	private val triggered = RegionEntryMap(entryConstructor)
	
	private var ticksToRecount = 25 * 20
	private var ticksToTeleport = 35 * 20
	
	override fun update(){
		if (!triggered.isEmpty && --ticksToTeleport < 0){
			ticksToTeleport = rand.nextInt(35, 75)
			tickTriggered()
		}
		
		if (--ticksToRecount < 0){
			ticksToRecount = rand.nextInt(15, 25) * 20
			tickRecount()
		}
	}
	
	// Ticking
	
	private fun tickRecount(){
		for((region, endermites) in groupEndermitesByRegion()){
			val baseCount = (endermites.size + 2) / 3
			
			addInstabilityPoints(region, baseCount * 10)
			
			for(adjacent in region.adjacent){
				@Suppress("UNCHECKED_CAST")
				addInstabilityPoints(adjacent as T, baseCount * 3)
			}
		}
	}
	
	private fun tickTriggered(){
		val groups = groupEndermitesByRegion()
		
		for(region in triggered.regions){
			val endermites = groups[region]
			
			if (endermites != null && endermites.isNotEmpty()){
				val prevCount = triggered.getPoints(region)
				val newCount = prevCount + EndermiteTeleportLogic.call(endermites, min(3, TELEPORTS_TO_CHAOS - prevCount), rand)
				
				if (newCount < TELEPORTS_TO_CHAOS){
					triggered.setPoints(region, newCount)
				}
				else{
					triggered.removeRegion(region)
					
					val clusters = groupClustersByRegion()[region]
					
					if (clusters != null){
						ClusterLeakLogic.call(clusters, rand)
					}
					
					multiplyInstabilityPoints(region, 0F)
					
					for(adjacent in region.adjacent){
						@Suppress("UNCHECKED_CAST")
						multiplyInstabilityPoints(adjacent as T, 0.25F)
					}
				}
			}
		}
	}
	
	// Regions
	
	private fun groupEndermitesByRegion(): Map<T, List<EntityMobEndermiteInstability>>{
		return world.loadedEntityList.filterIsInstance<EntityMobEndermiteInstability>().groupBy { entryConstructor.fromPos(Pos(it)) }
	}
	
	private fun groupClustersByRegion(): Map<T, List<TileEntityEnergyCluster>>{
		return world.loadedTileEntityList.filterIsInstance<TileEntityEnergyCluster>().groupBy { entryConstructor.fromPos(it.pos) }
	}
	
	// Points
	
	private fun addInstabilityPoints(region: T, points: Int){
		modifyInstabilityPoints(region){ it + points }
	}
	
	private fun multiplyInstabilityPoints(region: T, multiplier: Float){
		modifyInstabilityPoints(region){ (it * multiplier).floorToInt() }
	}
	
	private inline fun modifyInstabilityPoints(region: T, modifier: (Int) -> Int): Int{
		if (triggered.containsRegion(region)){
			return -1
		}
		
		val prevPoints = entries.getPoints(region)
		val newPoints = modifier(prevPoints).coerceIn(0, POINTS_TO_TRIGGER)
		
		if (newPoints == 0){
			entries.removeRegion(region)
		}
		else{
			entries.setPoints(region, newPoints)
			
			if (newPoints == POINTS_TO_TRIGGER){
				triggered.setPoints(region, 0)
			}
		}
		
		return newPoints
	}
	
	// Serialization
	
	override fun serializeNBT() = TagCompound().apply {
		setTag("Entries", entries.serializeNBT())
		setTag("Triggered", triggered.serializeNBT())
		
		setInteger("TicksToRecount", ticksToRecount)
		setInteger("TicksToTeleport", ticksToTeleport)
	}
	
	override fun deserializeNBT(nbt: TagCompound) = with(nbt){
		entries.deserializeNBT(getTag("Entries") as? TagLongArray)
		triggered.deserializeNBT(getTag("Triggered") as? TagLongArray)
		
		ticksToRecount = getInteger("TicksToRecount")
		ticksToTeleport = getInteger("TicksToTeleport")
	}
}
