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
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.Pos
import chylex.hee.system.util.xz
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.biome.Biomes
import net.minecraft.world.biome.provider.SingleBiomeProvider
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.dimension.EndDimension
import net.minecraft.world.gen.ChunkGenerator
import net.minecraft.world.gen.EndGenerationSettings
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.client.IRenderHandler
import java.util.function.BiFunction

class WorldProviderEndCustom(world: World, type: DimensionType) : EndDimension(world, type){
	companion object{
		const val DEFAULT_CELESTIAL_ANGLE = 0.5F
		const val DEFAULT_SUN_BRIGHTNESS = 1F
		const val DEFAULT_SKY_LIGHT = 0
		
		const val SAVE_FOLDER = "DIM-HEE"
		val CONSTRUCTOR = BiFunction(::WorldProviderEndCustom)
		
		fun register(){
			with(HEE.dim){
				directory = SAVE_FOLDER
				factory = CONSTRUCTOR
				hasSkyLight = true
			}
		}
		
		var debugMode = false
	}
	
	private var clientProxy: ModCommonProxy? = null
	
	private val clientEnvironment
		get() = clientProxy?.getClientSidePlayer()?.let(TerritoryInstance.Companion::fromPos)?.territory?.desc?.environment
	
	init{
		when(val w = this.world){
			is ServerWorld -> {
				dragonFightManager = DragonFightManagerNull(w, this)
			}
			
			else -> {
				clientProxy = HEE.proxy
			}
		}
	}
	
	override fun createChunkGenerator(): ChunkGenerator<EndGenerationSettings>{
		val settings = EndGenerationSettings().apply {
			defaultBlock = Blocks.END_STONE.defaultState
			defaultFluid = Blocks.AIR.defaultState
			spawnPos = Pos(THE_HUB_INSTANCE.centerPoint).xz.withY(255)
		}
		
		return ChunkGeneratorEndCustom(world, SingleBiomeProvider(SingleBiomeProviderSettings().setBiome(Biomes.THE_END)), settings)
	}
	
	// Spawn point
	
	fun getSpawnInfo(): SpawnInfo{
		return THE_HUB_INSTANCE.prepareSpawnPoint(null, clearanceRadius = 1)
	}
	
	override fun getSpawnPoint(): BlockPos{
		return THE_HUB_INSTANCE.getSpawnPoint()
	}
	
	override fun getSpawnCoordinate(): BlockPos?{
		return null
	}
	
	// Behavior properties
	
	override fun tick(){ // stops triggering a few seconds after all players leave the dimension (if still loaded)
		if (!debugMode){
			TerritoryVoid.onWorldTick(world as ServerWorld)
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
	
	@Sided(Side.CLIENT)
	override fun getSunBrightness(partialTicks: Float): Float{
		return clientEnvironment?.sunBrightness ?: DEFAULT_SUN_BRIGHTNESS
	}
	
	// Visual Properties (Sky)
	
	@Sided(Side.CLIENT)
	override fun isSkyColored(): Boolean{
		return true
	}
	
	@Sided(Side.CLIENT)
	override fun getSkyRenderer(): IRenderHandler?{
		return clientEnvironment?.renderer ?: EmptyRenderer
	}
	
	@Sided(Side.CLIENT)
	override fun getSkyColor(pos: BlockPos, partialTicks: Float): Vec3d{
		return clientEnvironment?.fogColor ?: Vec3d.ZERO // return fog color because vanilla blends fog into sky color based on chunk render distance
	}
	
	// Visual properties (Fog)
	
	@Sided(Side.CLIENT)
	override fun getFogColor(celestialAngle: Float, partialTicks: Float): Vec3d{
		return clientEnvironment?.fogColor ?: Vec3d.ZERO
	}
	
	@Sided(Side.CLIENT)
	override fun doesXZShowFog(x: Int, z: Int): Boolean{
		if (debugMode){
			return false
		}
		
		GL.setFogMode(FOG_EXP2)
		GL.setFogDensity(clientEnvironment?.fogDensity?.times(EnvironmentRenderer.currentFogDensityMp) ?: 0F) // TODO adjust fog density by render distance
		return true
	}
	
	@Sided(Side.CLIENT)
	override fun getVoidFogYFactor(): Double{
		return 1.0
	}
	
	// Neutralization
	
	override fun setSpawnPoint(pos: BlockPos){}
	
	@Sided(Side.CLIENT)
	override fun setSkyRenderer(renderer: IRenderHandler){}
	
	@Sided(Side.CLIENT)
	override fun setCloudRenderer(renderer: IRenderHandler){}
	
	@Sided(Side.CLIENT)
	override fun setWeatherRenderer(renderer: IRenderHandler){}
}
