package chylex.hee.game.world.provider
import chylex.hee.game.world.provider.behavior.DragonFightManagerNull
import net.minecraft.world.DimensionType
import net.minecraft.world.WorldProviderEnd
import net.minecraft.world.WorldServer
import net.minecraft.world.gen.IChunkGenerator

class WorldProviderEndCustom : WorldProviderEnd(){
	companion object{
		fun register(){
			DimensionType.THE_END.clazz = WorldProviderEndCustom::class.java
		}
	}
	
	override fun init(){
		super.init()
		(world as? WorldServer)?.let { dragonFightManager = DragonFightManagerNull(it) }
	}
	
	override fun onWorldUpdateEntities(){}
}
