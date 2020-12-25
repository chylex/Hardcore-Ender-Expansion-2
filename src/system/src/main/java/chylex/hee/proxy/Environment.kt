package chylex.hee.proxy

import chylex.hee.system.forge.Side
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ResourceLocation
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.loot.LootTable
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.server.ServerLifecycleHooks

object Environment {
	val side: Side = FMLEnvironment.dist
	
	fun getServer(): MinecraftServer {
		return ServerLifecycleHooks.getCurrentServer()
	}
	
	fun getDimension(dimension: DimensionType): ServerWorld {
		return getServer().getWorld(dimension)
	}
	
	fun getLootTable(location: ResourceLocation): LootTable {
		return getServer().lootTableManager.getLootTableFromLocation(location)
	}
}
