package chylex.hee.proxy
import chylex.hee.system.migration.forge.Side
import net.minecraft.server.MinecraftServer
import net.minecraftforge.fml.common.FMLCommonHandler

object Environment{
	val side: Side = FMLCommonHandler.instance().side
	
	fun constructProxy(): ModCommonProxy{
		val constructor: () -> ModCommonProxy = when(side){
			Side.CLIENT -> {{ ModClientProxy() }}
			Side.SERVER -> {{ ModCommonProxy() }}
		}
		
		return constructor()
	}
	
	fun getServer(): MinecraftServer{
		return FMLCommonHandler.instance().minecraftServerInstance
	}
}
