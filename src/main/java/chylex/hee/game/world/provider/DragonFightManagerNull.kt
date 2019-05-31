package chylex.hee.game.world.provider
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.DamageSource
import net.minecraft.world.WorldServer
import net.minecraft.world.end.DragonFightManager
import net.minecraft.world.end.DragonSpawnManager

class DragonFightManagerNull(world: WorldServer) : DragonFightManager(world, NBTTagCompound()){
	override fun tick(){}
	
	override fun hasPreviouslyKilledDragon() = false
	override fun dragonUpdate(dragon: EntityDragon){}
	override fun processDragonDeath(dragon: EntityDragon){}
	override fun setRespawnState(state: DragonSpawnManager){}
	override fun respawnDragon(){}
	
	override fun getNumAliveCrystals() = 0
	override fun onCrystalDestroyed(crystal: EntityEnderCrystal, source: DamageSource){}
	override fun resetSpikeCrystals(){}
	
	override fun addPlayer(player: EntityPlayerMP){}
	override fun removePlayer(player: EntityPlayerMP){}
}
