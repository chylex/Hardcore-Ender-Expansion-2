package chylex.hee.game.world.provider
import chylex.hee.game.world.provider.behavior.DragonFightManagerNull
import net.minecraft.world.DimensionType
import net.minecraft.world.WorldProviderEnd
import net.minecraft.world.WorldServer
import net.minecraft.world.gen.IChunkGenerator

class WorldProviderEndCustom : WorldProviderEnd(){
	companion object{
		const val DEFAULT_CELESTIAL_ANGLE = 0.5F
		const val DEFAULT_SUN_BRIGHTNESS = 1F
		const val DEFAULT_SKY_LIGHT = 0
		
		fun register(){
			DimensionType.THE_END.clazz = WorldProviderEndCustom::class.java
		}
	}
	
	override fun init(){
		super.init()
		(world as? WorldServer)?.let { dragonFightManager = DragonFightManagerNull(it) }
	}
	
	override fun createChunkGenerator(): IChunkGenerator{
		return ChunkGeneratorEndDebug(world)
	}
	
	override fun onWorldUpdateEntities(){}
}
