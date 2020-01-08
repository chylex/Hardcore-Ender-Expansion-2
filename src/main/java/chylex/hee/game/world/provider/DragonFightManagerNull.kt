package chylex.hee.game.world.provider
import chylex.hee.game.world.WorldProviderEndCustom
import chylex.hee.system.migration.vanilla.EntityEnderCrystal
import chylex.hee.system.migration.vanilla.EntityPlayerMP
import chylex.hee.system.util.TagCompound
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.util.DamageSource
import net.minecraft.world.end.DragonFightManager
import net.minecraft.world.end.DragonSpawnState
import net.minecraft.world.server.ServerWorld

class DragonFightManagerNull(world: ServerWorld, provider: WorldProviderEndCustom) : DragonFightManager(world, TagCompound(), provider){
	override fun tick(){}
	override fun write() = TagCompound()
	
	override fun dragonUpdate(dragon: EnderDragonEntity){}
	override fun processDragonDeath(dragon: EnderDragonEntity){}
	override fun hasPreviouslyKilledDragon() = false
	
	override fun setRespawnState(state: DragonSpawnState){}
	override fun tryRespawnDragon(){}
	
	override fun getNumAliveCrystals() = 0
	override fun onCrystalDestroyed(crystal: EntityEnderCrystal, source: DamageSource){}
	override fun resetSpikeCrystals(){}
	
	override fun addPlayer(player: EntityPlayerMP){}
	override fun removePlayer(player: EntityPlayerMP){}
}
