package chylex.hee.game.territory.description

import chylex.hee.client.render.lightmaps.ILightmap
import chylex.hee.client.render.lightmaps.ILightmap.Companion.calcLightFactor
import chylex.hee.client.render.world.SkyPlaneTopFoggy
import chylex.hee.client.util.MC
import chylex.hee.game.Resource
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.entity.util.lookPosVec
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.particle.ParticleDust
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.territory.behavior.ForgottenTombsEndSequenceBehavior
import chylex.hee.game.territory.behavior.ForgottenTombsSpawnerBehavior
import chylex.hee.game.territory.storage.ForgottenTombsEndData
import chylex.hee.game.territory.system.ITerritoryBehavior
import chylex.hee.game.territory.system.ITerritoryDescription
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.game.territory.system.properties.TerritoryColors
import chylex.hee.game.territory.system.properties.TerritoryDifficulty
import chylex.hee.game.territory.system.properties.TerritoryEnvironment
import chylex.hee.game.territory.system.properties.TerritoryTokenHolders
import chylex.hee.game.territory.system.storage.TerritoryEntry
import chylex.hee.game.territory.system.storage.TerritoryStorageComponent
import chylex.hee.game.world.util.allInCenteredBoxMutable
import chylex.hee.game.world.util.isFullBlock
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.LerpedFloat
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Vec
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.lerp
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextInt
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.potion.Effects
import net.minecraft.util.math.vector.Vector3f
import net.minecraft.world.LightType.BLOCK
import net.minecraft.world.LightType.SKY
import java.util.Random
import kotlin.math.max
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
		override fun setupClient(player: PlayerEntity) {
			tickClient(player)
			currentFogDensity.updateImmediately(MAX_FOG_DENSITY * 0.8F)
		}
		
		@Sided(Side.CLIENT)
		override fun tickClient(player: PlayerEntity) {
			val world = player.world
			val pos = Pos(player.lookPosVec)
			
			var levelBlock = 0
			var levelSky = 0
			
			for (offset in pos.allInCenteredBoxMutable(1, 1, 1)) {
				levelBlock = max(levelBlock, world.getLightFor(BLOCK, offset))
				levelSky = max(levelSky, world.getLightFor(SKY, offset))
			}
			
			val light = max(levelBlock / 15F, levelSky / 12F)
			
			val prev = currentFogDensity.currentValue
			val next = MAX_FOG_DENSITY - (light.pow(0.2F) * 0.85F * MAX_FOG_DENSITY)
			val speed = if (next > prev) 0.025F else 0.055F
			
			currentFogDensity.update(lerp(prev, next, speed))
			nightVisionFactor = if (player.isPotionActive(Effects.NIGHT_VISION)) 1F else 0F
			
			if (MC.instance.isGamePaused) {
				return
			}
			
			val rand = world.rand
			val count = 3 + ((levelSky * 2) / 5)
			
			repeat(count) {
				val distXZ = rand.nextInt(3 + (levelSky / 4), 25 + (levelSky * 2))
				val distY = if (levelSky > 0) distXZ else distXZ / 2
				
				for (attempt in 1..3) {
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
	
	override fun initialize(instance: TerritoryInstance, entry: TerritoryEntry, behaviors: MutableList<ITerritoryBehavior>) {
		super.initialize(instance, entry, behaviors)
		
		val endData = entry.registerComponent(TerritoryStorageComponent.FORGOTTEN_TOMBS_END_DATA)
		
		behaviors.add(ForgottenTombsSpawnerBehavior(instance, endData))
		behaviors.add(ForgottenTombsEndSequenceBehavior(instance, endData))
	}
	
	override val tokenHolders = object : TerritoryTokenHolders() {
		override fun afterUse(holder: EntityTokenHolder, instance: TerritoryInstance) {
			instance.getStorageComponent<ForgottenTombsEndData>()?.startEndSequence(holder.world, instance, holder.posVec)
		}
	}
}
