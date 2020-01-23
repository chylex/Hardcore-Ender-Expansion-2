package chylex.hee.proxy
import chylex.hee.system.migration.forge.Side
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ResourceLocation
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.loot.LootTable
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.server.ServerLifecycleHooks

object Environment{
	val side: Side = FMLEnvironment.dist
	
	fun constructProxy(): ModCommonProxy{
		val constructor: () -> ModCommonProxy = when(side){
			Side.CLIENT           -> {{ ModClientProxy() }}
			Side.DEDICATED_SERVER -> {{ ModCommonProxy() }}
		}
		
		return constructor()
	}
	
	fun getServer(): MinecraftServer{
		return ServerLifecycleHooks.getCurrentServer()
	}
	
	fun getDimension(dimension: DimensionType): ServerWorld{
		return getServer().func_71218_a(dimension)
	}
	
	fun getLootTable(location: ResourceLocation): LootTable{
		return getServer().lootTableManager.getLootTableFromLocation(location)
	}
}
