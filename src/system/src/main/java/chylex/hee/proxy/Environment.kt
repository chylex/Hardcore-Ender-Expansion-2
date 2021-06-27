package chylex.hee.proxy

import chylex.hee.system.forge.Side
import net.minecraft.loot.LootTable
import net.minecraft.server.MinecraftServer
import net.minecraft.util.RegistryKey
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.server.ServerLifecycleHooks

object Environment {
	val side: Side = FMLEnvironment.dist
	
	fun getServer(): MinecraftServer {
		return ServerLifecycleHooks.getCurrentServer()
	}
	
	fun getDimension(dimension: RegistryKey<World>): ServerWorld {
		return getServer().getWorld(dimension)!!
	}
	
	fun getLootTable(location: ResourceLocation): LootTable {
		return getServer().lootTableManager.getLootTableFromLocation(location)
	}
}
