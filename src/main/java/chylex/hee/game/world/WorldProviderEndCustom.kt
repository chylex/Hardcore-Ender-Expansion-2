package chylex.hee.game.world
import chylex.hee.HEE
import chylex.hee.client.render.territory.AbstractEnvironmentRenderer
import chylex.hee.client.render.territory.EmptyRenderer
import chylex.hee.client.render.util.GL
import chylex.hee.client.util.MC
import chylex.hee.game.mechanics.portal.SpawnInfo
import chylex.hee.game.world.provider.DragonFightManagerNull
import chylex.hee.game.world.provider.WorldBorderNull
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryInstance.Companion.THE_HUB_INSTANCE
import chylex.hee.game.world.territory.TerritoryVoid
import chylex.hee.proxy.ModCommonProxy
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.Pos
import chylex.hee.system.util.xz
import net.minecraft.block.Blocks
import net.minecraft.client.renderer.Vector3f
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.biome.Biomes
import net.minecraft.world.biome.provider.SingleBiomeProvider
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.dimension.EndDimension
import net.minecraft.world.gen.ChunkGenerator
import net.minecraft.world.gen.EndGenerationSettings
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.IRenderHandler
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent
import java.util.function.BiFunction

class WorldProviderEndCustom(world: World, type: DimensionType) : EndDimension(world, type){
	@SubscribeAllEvents(Dist.CLIENT, modid = HEE.ID)
	companion object{
		const val DEFAULT_CELESTIAL_ANGLE = 1F
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
		
		private val CLIENT_SIDE_SPAWN_POINT = Pos(THE_HUB_INSTANCE.centerPoint).xz.withY(255)
		private val SERVER_SIDE_SPAWN_POINT = Pos(THE_HUB_INSTANCE.centerPoint).xz.withY(4095) // blocks vanilla attempt to spawn portal platform
		
		@Sided(Side.CLIENT)
		@SubscribeEvent(priority = EventPriority.LOWEST)
		fun onFog(e: RenderFogEvent){
			val player = MC.player?.takeIf { it.world.dimension.type == DimensionType.THE_END } ?: return
			
			val density = TerritoryInstance.fromPos(player)?.let { it.territory.desc.environment }?.let {
				(it.fogDensity * AbstractEnvironmentRenderer.currentFogDensityMp) + (it.fogRenderDistanceModifier * AbstractEnvironmentRenderer.currentRenderDistanceMp)
			}
			
			GL.setFogMode(GL.FOG_EXP2)
			GL.setFogDensity(density ?: 0F)
		}
	}
	
	private var clientProxy: ModCommonProxy? = null
	
	private val clientEnvironment
		get() = clientProxy?.getClientSidePlayer()?.let(TerritoryInstance.Companion::fromPos)?.let { it.territory.desc.environment }
	
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
			spawnPos = CLIENT_SIDE_SPAWN_POINT
		}
		
		return ChunkGeneratorEndCustom(world, SingleBiomeProvider(SingleBiomeProviderSettings(world.worldInfo).setBiome(Biomes.THE_END)), settings)
	}
	
	/* UPDATE
	override fun getBiome(pos: BlockPos): Biome{
		return Biomes.THE_END // prevents client from falling back to Plains and rendering rain while loading
	}*/
	
	// Spawn point
	
	fun getSpawnInfo(): SpawnInfo{
		return THE_HUB_INSTANCE.prepareSpawnPoint(null, clearanceRadius = 1)
	}
	
	override fun getSpawnPoint(): BlockPos{
		return if (world.isRemote)
			CLIENT_SIDE_SPAWN_POINT
		else
			THE_HUB_INSTANCE.getSpawnPoint()
	}
	
	override fun getSpawnCoordinate(): BlockPos{
		return SERVER_SIDE_SPAWN_POINT
	}
	
	// Behavior properties
	
	override fun tick(){ // stops triggering a few seconds after all players leave the dimension (if still loaded)
		if (!debugMode){
			TerritoryVoid.onWorldTick(world as ServerWorld)
		}
	}
	
	override fun updateWeather(defaultLogic: Runnable){
		world.prevRainingStrength = 0F
		world.rainingStrength = 0F
		
		world.prevThunderingStrength = 0F
		world.thunderingStrength = 0F
	}
	
	override fun canDoRainSnowIce(chunk: Chunk): Boolean{
		return false
	}
	
	override fun canDoLightning(chunk: Chunk): Boolean{
		return false
	}
	
	override fun createWorldBorder() = WorldBorderNull()
	
	// Visual properties (Light)
	
	override fun getLightmapColors(partialTicks: Float, sunBrightness: Float, skyLight: Float, blockLight: Float, colors: Vector3f){
		clientEnvironment?.let { it.lightmap.update(colors, sunBrightness, skyLight.coerceAtMost(it.skyLight / 16F), blockLight, partialTicks) }
	}
	
	override fun getLightBrightness(lightLevel: Int): Float{
		return clientEnvironment?.lightBrightnessTable?.let { it[lightLevel] } ?: super.getLightBrightness(lightLevel)
	}
	
	override fun calculateCelestialAngle(worldTime: Long, partialTicks: Float): Float{
		return clientEnvironment?.celestialAngle ?: DEFAULT_CELESTIAL_ANGLE
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
	
	/* UPDATE ASM
	@Sided(Side.CLIENT)
	override fun getSkyColor(pos: BlockPos, partialTicks: Float): Vec3d{
		return clientEnvironment?.fogColor ?: Vec3d.ZERO // return fog color because vanilla blends fog into sky color based on chunk render distance
	}*/
	
	// Visual properties (Fog)
	
	@Sided(Side.CLIENT)
	override fun getFogColor(celestialAngle: Float, partialTicks: Float): Vec3d{
		return clientEnvironment?.fogColor ?: Vec3d.ZERO
	}
	
	@Sided(Side.CLIENT)
	override fun doesXZShowFog(x: Int, z: Int): Boolean{
		return !debugMode
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
