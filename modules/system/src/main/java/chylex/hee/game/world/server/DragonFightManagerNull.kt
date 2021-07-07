package chylex.hee.game.world.server

import chylex.hee.util.nbt.TagCompound
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.item.EnderCrystalEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.DamageSource
import net.minecraft.world.end.DragonFightManager
import net.minecraft.world.end.DragonSpawnState
import net.minecraft.world.server.ServerWorld

open class DragonFightManagerNull(world: ServerWorld) : DragonFightManager(world, 0L, TagCompound()) {
	override fun tick() {}
	override fun write() = TagCompound()
	
	override fun dragonUpdate(dragon: EnderDragonEntity) {}
	override fun processDragonDeath(dragon: EnderDragonEntity) {}
	override fun hasPreviouslyKilledDragon() = false
	
	override fun setRespawnState(state: DragonSpawnState) {}
	override fun tryRespawnDragon() {}
	
	override fun getNumAliveCrystals() = 0
	override fun onCrystalDestroyed(crystal: EnderCrystalEntity, source: DamageSource) {}
	override fun resetSpikeCrystals() {}
	
	override fun addPlayer(player: ServerPlayerEntity) {}
	override fun removePlayer(player: ServerPlayerEntity) {}
}
