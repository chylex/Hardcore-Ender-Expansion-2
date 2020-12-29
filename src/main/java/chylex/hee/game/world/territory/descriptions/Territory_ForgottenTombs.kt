package chylex.hee.game.world.territory.descriptions

import chylex.hee.client.MC
import chylex.hee.client.render.lightmaps.ILightmap
import chylex.hee.client.render.lightmaps.ILightmap.Companion.calcLightFactor
import chylex.hee.client.render.territory.components.SkyPlaneTopFoggy
import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.game.entity.lookPosVec
import chylex.hee.game.entity.selectAllEntities
import chylex.hee.game.particle.ParticleDust
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.Pos
import chylex.hee.game.world.allInCenteredBoxMutable
import chylex.hee.game.world.center
import chylex.hee.game.world.distanceSqTo
import chylex.hee.game.world.isFullBlock
import chylex.hee.game.world.offsetUntil
import chylex.hee.game.world.territory.ITerritoryDescription
import chylex.hee.game.world.territory.ITerritoryTicker
import chylex.hee.game.world.territory.TerritoryDifficulty
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.game.world.territory.generators.Generator_ForgottenTombs
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.properties.TerritoryEnvironment
import chylex.hee.game.world.territory.storage.TerritoryEntry
import chylex.hee.game.world.totalTime
import chylex.hee.init.ModEntities
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.LerpedFloat
import chylex.hee.system.math.Vec
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.floorToInt
import chylex.hee.system.math.offsetTowards
import chylex.hee.system.math.square
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Potions
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItem
import net.minecraft.client.renderer.Vector3f
import net.minecraft.entity.EntitySpawnPlacementRegistry
import net.minecraft.entity.SpawnReason.NATURAL
import net.minecraft.util.Direction.UP
import net.minecraft.util.math.RayTraceContext
import net.minecraft.util.math.RayTraceContext.BlockMode
import net.minecraft.util.math.RayTraceContext.FluidMode
import net.minecraft.util.math.RayTraceResult.Type
import net.minecraft.world.Difficulty
import net.minecraft.world.LightType.BLOCK
import net.minecraft.world.LightType.SKY
import net.minecraft.world.World
import java.util.Random
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object Territory_ForgottenTombs : ITerritoryDescription {
	override val difficulty
		get() = TerritoryDifficulty.HOSTILE
	
	override val colors = object : TerritoryColors() {
		override val tokenTop    = RGB(211, 212, 152)
		override val tokenBottom = RGB(160, 151, 116)
		
		override val portalSeed = 410L
		
		override fun nextPortalColor(rand: Random, color: FloatArray) {
			if (rand.nextBoolean()) {
				color[0] = rand.nextFloat(0.65F, 0.9F)
				color[1] = rand.nextFloat(0.45F, 0.7F)
				color[2] = rand.nextFloat(0.15F, 0.4F)
			}
			else {
				color.fill(rand.nextFloat(0.95F, 1F))
			}
		}
	}
	
	private const val MAX_FOG_DENSITY = 0.069F
	
	private val PARTICLE_DUST = ParticleSpawnerCustom(
		type = ParticleDust,
		data = ParticleDust.Data(lifespan = 100..175, scale = (0.145F)..(0.165F), reactsToSkyLight = true),
		pos = InBox(0.5F, 0.5F, 0.5F),
		maxRange = 64.0
	)
	
	override val environment = object : TerritoryEnvironment() {
		override val fogColor
			get() = (fogDensity / 0.275F).let { Vec(0.15 + it, 0.08 + it, 0.03) }
		
		override val fogDensity
			get() = currentFogDensity.get(MC.partialTicks)
		
		override val fogRenderDistanceModifier = 0.003F
		
		override val skyLight = 15
		
		override val voidRadiusMpXZ = 1.35F
		override val voidRadiusMpY = 0.975F
		override val voidCenterOffset = Vec3.y(-8.0)
		
		override val renderer = SkyPlaneTopFoggy(
			texture = Resource.Custom("textures/environment/stars.png"),
			color = Vec(0.58, 0.58, 0.54),
			rescale = 29F,
			distance = 65F,
			width = 300F
		)
		
		override val lightmap = object : ILightmap {
			override fun update(colors: Vector3f, sunBrightness: Float, skyLight: Float, blockLight: Float, partialTicks: Float) {
				val blockFactor = calcLightFactor(blockLight)
				
				colors.x = (blockLight * 0.9F) + skyLight + 0.12F
				colors.y = (blockFactor * 0.7F) + (skyLight * 0.8F) + 0.08F
				colors.z = (blockFactor * 0.5F) + (skyLight * 1.2F) + (0.09F * nightVisionFactor)
			}
		}
		
		private val currentFogDensity = LerpedFloat(MAX_FOG_DENSITY)
		private var nightVisionFactor = 0F
		
		@Sided(Side.CLIENT)
		override fun setupClient(player: EntityPlayer) {
			tickClient(player)
			currentFogDensity.updateImmediately(MAX_FOG_DENSITY * 0.8F)
		}
		
		@Sided(Side.CLIENT)
		override fun tickClient(player: EntityPlayer) {
			val world = player.world
			val pos = Pos(player.lookPosVec)
			
			var levelBlock = 0
			var levelSky = 0
			
			for(offset in pos.allInCenteredBoxMutable(1, 1, 1)) {
				levelBlock = max(levelBlock, world.getLightFor(BLOCK, offset))
				levelSky = max(levelSky, world.getLightFor(SKY, offset))
			}
			
			val light = max(levelBlock / 15F, levelSky / 12F)
			
			val prev = currentFogDensity.currentValue
			val next = MAX_FOG_DENSITY - (light.pow(0.2F) * 0.85F * MAX_FOG_DENSITY)
			val speed = if (next > prev) 0.025F else 0.055F
			
			currentFogDensity.update(offsetTowards(prev, next, speed))
			nightVisionFactor = if (player.isPotionActive(Potions.NIGHT_VISION)) 1F else 0F
			
			if (MC.instance.isGamePaused) {
				return
			}
			
			val rand = world.rand
			val count = 3 + ((levelSky * 2) / 5)
			
			repeat(count) {
				val distXZ = rand.nextInt(3 + (levelSky / 4), 25 + (levelSky * 2))
				val distY = if (levelSky > 0) distXZ else distXZ / 2
				
				for(attempt in 1..3) {
					val testPos = pos.add(
						rand.nextInt(-distXZ, distXZ),
						rand.nextInt(-distY / 2, distY),
						rand.nextInt(-distXZ, distXZ)
					)
					
					if (!testPos.isFullBlock(world)) {
						PARTICLE_DUST.spawn(Point(testPos, 1), rand)
						break
					}
				}
			}
		}
	}
	
	override fun initialize(instance: TerritoryInstance, entry: TerritoryEntry, tickers: MutableList<ITerritoryTicker>) {
		super.initialize(instance, entry, tickers)
		
		tickers.add(object : ITerritoryTicker {
			override fun tick(world: World) {
				if (world.difficulty == Difficulty.PEACEFUL || world.totalTime % 20L != 0L) {
					return
				}
				
				val players = instance.players.filter { it.isAlive && !it.isSpectator }
				
				if (players.isEmpty()) {
					return
				}
				
				val maxUndreadsInTerritory = min(40, 10 + (10 * players.size))
				val allUndreads = world.selectAllEntities.filter { it is EntityMobUndread && TerritoryInstance.fromPos(it) == instance }
				
				if (allUndreads.size >= maxUndreadsInTerritory) {
					return
				}
				
				val rand = world.rand
				val maxY = TerritoryType.FORGOTTEN_TOMBS.height.last - Generator_ForgottenTombs.EntranceCave.ELLIPSOID_Y_OFFSET - 30 // roughly where the first level starts
				val maxSpawns = min(3, maxUndreadsInTerritory - allUndreads.size)
				
				for(spawn in 1..maxSpawns) {
					val pickedPlayer = rand.nextItem(players)
					val playerY = pickedPlayer.posY
					
					if (playerY > maxY) {
						continue
					}
					
					val playerDepth = maxY - pickedPlayer.posY
					val maxUndreadsInDepth = 2 + (playerDepth * 0.28).floorToInt()
					
					if (allUndreads.count { abs(it.posY - playerY) < 8 } >= maxUndreadsInDepth) {
						continue
					}
					
					val spawnAttempts = 2 + (playerDepth * 0.1).floorToInt().coerceAtMost(5)
					
					for(attempt in 1..spawnAttempts) {
						val testPos = Pos(pickedPlayer).add(
							rand.nextInt(-19, 19),
							rand.nextInt(-8, 8),
							rand.nextInt(-19, 19)
						).offsetUntil(UP, -5..3) {
							EntitySpawnPlacementRegistry.func_223515_a(ModEntities.UNDREAD, world, NATURAL, it, rand) &&
							world.hasNoCollisions(ModEntities.UNDREAD.getBoundingBoxWithSizeApplied(it.x + 0.5, it.y.toDouble(), it.z + 0.5))
						}
						
						if (testPos == null || players.any { testPos.distanceSqTo(it) < square(15) }) {
							continue
						}
						
						if (players.any { world.rayTraceBlocks(RayTraceContext(it.lookPosVec, testPos.center, BlockMode.OUTLINE, FluidMode.NONE, it)).type == Type.MISS }) {
							continue
						}
						
						EntityMobUndread(world).apply {
							setLocationAndAngles(testPos.x + 0.5, testPos.y.toDouble(), testPos.z + 0.5, rand.nextFloat(0F, 360F), 0F)
							world.addEntity(this)
						}
						
						break
					}
				}
			}
		})
	}
}
