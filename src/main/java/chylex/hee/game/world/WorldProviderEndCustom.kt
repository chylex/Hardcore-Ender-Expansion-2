package chylex.hee.game.world
import chylex.hee.HEE
import chylex.hee.client.render.territory.EmptyRenderer
import chylex.hee.client.render.territory.EnvironmentRenderer
import chylex.hee.client.render.util.GL
import chylex.hee.client.render.util.GL.FOG_EXP2
import chylex.hee.game.mechanics.portal.SpawnInfo
import chylex.hee.game.world.provider.DragonFightManagerNull
import chylex.hee.game.world.provider.WorldBorderNull
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryInstance.Companion.THE_HUB_INSTANCE
import chylex.hee.game.world.territory.TerritoryVoid
import chylex.hee.proxy.ModCommonProxy
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.DimensionType
import net.minecraft.world.WorldProviderEnd
import net.minecraft.world.WorldServer
import net.minecraft.world.gen.IChunkGenerator
import net.minecraftforge.client.IRenderHandler
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class WorldProviderEndCustom : WorldProviderEnd(){
	companion object{
		const val DEFAULT_CELESTIAL_ANGLE = 0.5F
		const val DEFAULT_SUN_BRIGHTNESS = 1F
		const val DEFAULT_SKY_LIGHT = 0
		
		fun register(){
			DimensionType.THE_END.clazz = WorldProviderEndCustom::class.java
		}
		
		var debugMode = false
	}
	
	private var clientProxy: ModCommonProxy? = null
	
	private val clientEnvironment
		get() = clientProxy?.getClientSidePlayer()?.let(TerritoryInstance.Companion::fromPos)?.territory?.desc?.environment
	
	override fun init(){
		super.init()
		
		when(val world = this.world){
			is WorldServer -> {
				dragonFightManager = DragonFightManagerNull(world)
			}
			
			else -> {
				clientProxy = HEE.proxy
			}
		}
	}
	
	override fun createChunkGenerator(): IChunkGenerator{
		return ChunkGeneratorEndCustom(world)
	}
	
	override fun getSaveFolder(): String{
		return "DIM-HEE"
	}
	
	// Spawn point
	
	fun getSpawnInfo(): SpawnInfo{
		return THE_HUB_INSTANCE.prepareSpawnPoint(world, null, clearanceRadius = 1)
	}
	
	override fun getSpawnPoint(): BlockPos{
		return THE_HUB_INSTANCE.getSpawnPoint(world)
	}
	
	override fun getRandomizedSpawnPoint(): BlockPos{
		return spawnPoint
	}
	
	override fun getSpawnCoordinate(): BlockPos?{
		return null
	}
	
	// Behavior properties
	
	override fun onWorldUpdateEntities(){ // stops triggering a few seconds after all players leave the dimension (if still loaded)
		if (!debugMode){
			TerritoryVoid.onWorldTick(world)
		}
	}
	
	override fun createWorldBorder() = WorldBorderNull()
	
	// TODO shitton of things to play around with, also test if default values work on server
	
	// Visual properties (Light)
	
	override fun getLightmapColors(partialTicks: Float, sunBrightness: Float, skyLight: Float, blockLight: Float, colors: FloatArray){
		clientEnvironment?.lightmap?.update(colors, sunBrightness, skyLight, blockLight, partialTicks)
	}
	
	override fun getLightBrightnessTable(): FloatArray{
		return clientEnvironment?.lightBrightnessTable ?: super.getLightBrightnessTable()
	}
	
	override fun calculateCelestialAngle(worldTime: Long, partialTicks: Float): Float{
		return clientEnvironment?.celestialAngle ?: DEFAULT_CELESTIAL_ANGLE
	}
	
	override fun getSunBrightnessFactor(partialTicks: Float): Float{
		return 1F - (clientEnvironment?.sunBrightness ?: DEFAULT_SUN_BRIGHTNESS)
	}
	
	@SideOnly(Side.CLIENT)
	override fun getSunBrightness(partialTicks: Float): Float{
		return clientEnvironment?.sunBrightness ?: DEFAULT_SUN_BRIGHTNESS
	}
	
	override fun hasSkyLight(): Boolean{
		return true
	}
	
	// Visual Properties (Sky)
	
	@SideOnly(Side.CLIENT)
	override fun isSkyColored(): Boolean{
		return true
	}
	
	@SideOnly(Side.CLIENT)
	override fun getSkyRenderer(): IRenderHandler?{
		return clientEnvironment?.renderer ?: EmptyRenderer
	}
	
	@SideOnly(Side.CLIENT)
	override fun getSkyColor(camera: Entity, partialTicks: Float): Vec3d{
		return clientEnvironment?.fogColor ?: Vec3d.ZERO // return fog color because vanilla blends fog into sky color based on chunk render distance
	}
	
	// Visual properties (Fog)
	
	@SideOnly(Side.CLIENT)
	override fun getFogColor(celestialAngle: Float, partialTicks: Float): Vec3d{
		return clientEnvironment?.fogColor ?: Vec3d.ZERO
	}
	
	@SideOnly(Side.CLIENT)
	override fun doesXZShowFog(x: Int, z: Int): Boolean{
		if (debugMode){
			return false
		}
		
		GL.setFogMode(FOG_EXP2)
		GL.setFogDensity(clientEnvironment?.fogDensity?.times(EnvironmentRenderer.currentFogDensityMp) ?: 0F) // TODO adjust fog density by render distance
		return true
	}
	
	@SideOnly(Side.CLIENT)
	override fun getVoidFogYFactor(): Double{
		return 1.0
	}
	
	// Neutralization
	
	override fun setSpawnPoint(pos: BlockPos){}
	
	@SideOnly(Side.CLIENT)
	override fun setSkyRenderer(renderer: IRenderHandler){}
	
	@SideOnly(Side.CLIENT)
	override fun setCloudRenderer(renderer: IRenderHandler){}
	
	@SideOnly(Side.CLIENT)
	override fun setWeatherRenderer(renderer: IRenderHandler){}
}
