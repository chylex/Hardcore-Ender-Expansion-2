package chylex.hee.proxy
import chylex.hee.system.migration.forge.Side
import net.minecraft.server.MinecraftServer
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
}
