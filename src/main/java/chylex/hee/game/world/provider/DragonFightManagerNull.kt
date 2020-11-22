package chylex.hee.game.world.provider
import chylex.hee.system.migration.EntityEnderCrystal
import chylex.hee.system.migration.EntityEnderDragon
import chylex.hee.system.migration.EntityPlayerMP
import chylex.hee.system.serialization.TagCompound
import net.minecraft.util.DamageSource
import net.minecraft.world.end.DragonFightManager
import net.minecraft.world.end.DragonSpawnState
import net.minecraft.world.server.ServerWorld

class DragonFightManagerNull(world: ServerWorld) : DragonFightManager(world, 0L, TagCompound()){
	override fun tick(){}
	override fun write() = TagCompound()
	
	override fun dragonUpdate(dragon: EntityEnderDragon){}
	override fun processDragonDeath(dragon: EntityEnderDragon){}
	override fun hasPreviouslyKilledDragon() = false
	
	override fun setRespawnState(state: DragonSpawnState){}
	override fun tryRespawnDragon(){}
	
	override fun getNumAliveCrystals() = 0
	override fun onCrystalDestroyed(crystal: EntityEnderCrystal, source: DamageSource){}
	override fun resetSpikeCrystals(){}
	
	override fun addPlayer(player: EntityPlayerMP){}
	override fun removePlayer(player: EntityPlayerMP){}
}
