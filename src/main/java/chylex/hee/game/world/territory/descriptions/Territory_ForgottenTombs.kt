package chylex.hee.game.world.territory.descriptions

import chylex.hee.client.MC
import chylex.hee.client.render.lightmaps.ILightmap
import chylex.hee.client.render.lightmaps.ILightmap.Companion.calcLightFactor
import chylex.hee.client.render.territory.components.SkyPlaneTopFoggy
import chylex.hee.game.block.BlockGraveDirt
import chylex.hee.game.block.BlockVoidPortalInner
import chylex.hee.game.block.BlockVoidPortalInner.Type.RETURN_ACTIVE
import chylex.hee.game.block.BlockVoidPortalInner.Type.RETURN_INACTIVE
import chylex.hee.game.block.entity.TileEntityPortalInner
import chylex.hee.game.block.with
import chylex.hee.game.entity.OPERATION_MUL_INCR_GROUPED
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.game.entity.living.behavior.UndreadDustEffects
import chylex.hee.game.entity.lookPosVec
import chylex.hee.game.entity.posVec
import chylex.hee.game.entity.selectAllEntities
import chylex.hee.game.entity.selectVulnerableEntities
import chylex.hee.game.mechanics.dust.DustType
import chylex.hee.game.particle.ParticleDust
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.potion.makeEffect
import chylex.hee.game.world.Pos
import chylex.hee.game.world.allInBoxMutable
import chylex.hee.game.world.allInCenteredBoxMutable
import chylex.hee.game.world.breakBlock
import chylex.hee.game.world.center
import chylex.hee.game.world.distanceSqTo
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Tomb.MobSpawnerTrigger.Companion.FX_SPAWN_UNDREAD
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.isAir
import chylex.hee.game.world.isFullBlock
import chylex.hee.game.world.math.BoundingBox
import chylex.hee.game.world.offsetUntil
import chylex.hee.game.world.playPlayer
import chylex.hee.game.world.setState
import chylex.hee.game.world.territory.ITerritoryDescription
import chylex.hee.game.world.territory.ITerritoryTicker
import chylex.hee.game.world.territory.TerritoryDifficulty
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.game.world.territory.generators.Generator_ForgottenTombs
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.properties.TerritoryEnvironment
import chylex.hee.game.world.territory.properties.TerritoryTokenHolders
import chylex.hee.game.world.territory.storage.TerritoryEntry
import chylex.hee.game.world.territory.storage.TerritoryStorageComponent
import chylex.hee.game.world.territory.storage.data.ForgottenTombsEndData
import chylex.hee.game.world.totalTime
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModEntities
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.fx.FxVecData
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.LerpedFloat
import chylex.hee.system.math.Vec
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.ceilToInt
import chylex.hee.system.math.directionTowards
import chylex.hee.system.math.floorToInt
import chylex.hee.system.math.offsetTowards
import chylex.hee.system.math.square
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Potions
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItem
import chylex.hee.system.random.nextItemOrNull
import chylex.hee.system.random.nextRounded
import com.google.common.collect.Sets
import net.minecraft.client.renderer.Vector3f
import net.minecraft.entity.EntitySpawnPlacementRegistry
import net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE
import net.minecraft.entity.SpawnReason.NATURAL
import net.minecraft.entity.SpawnReason.STRUCTURE
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.util.Direction.UP
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.RayTraceContext
import net.minecraft.util.math.RayTraceContext.BlockMode
import net.minecraft.util.math.RayTraceContext.FluidMode
import net.minecraft.util.math.RayTraceResult.Type
import net.minecraft.world.Difficulty.PEACEFUL
import net.minecraft.world.LightType.BLOCK
import net.minecraft.world.LightType.SKY
import net.minecraft.world.World
import java.util.Random
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

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
			
			for (offset in pos.allInCenteredBoxMutable(1, 1, 1)) {
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
	
	private val undreadFollowRangeModifier = AttributeModifier(UUID.fromString("8F3A50CE-DAE7-4F3E-91E8-41505CBFB7A9"), "Undread End Follow Range", 3.0, OPERATION_MUL_INCR_GROUPED)
	
	override fun initialize(instance: TerritoryInstance, entry: TerritoryEntry, tickers: MutableList<ITerritoryTicker>) {
		super.initialize(instance, entry, tickers)
		
		val endData = entry.registerComponent(TerritoryStorageComponent.FORGOTTEN_TOMBS_END_DATA)
		
		tickers.add(object : ITerritoryTicker {
			override fun tick(world: World) {
				if (world.difficulty == PEACEFUL || endData.isPortalActivated || world.totalTime % 20L != 0L) {
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
				
				for (spawn in 1..maxSpawns) {
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
					
					for (attempt in 1..spawnAttempts) {
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
		
		tickers.add(object : ITerritoryTicker {
			override fun tick(world: World) {
				val ticks = endData.endSequenceTicks?.takeIf { it != Int.MAX_VALUE } ?: return
				val aabb = endData.roomAABB ?: return
				
				if (world.difficulty == PEACEFUL) {
					if (activatePortal(world, aabb)) {
						endData.endSequenceTicks = Int.MAX_VALUE
						endData.isPortalActivated = true
						return
					}
				}
				
				endData.endSequenceTicks = ticks + 1
				
				if (ticks < 50) {
					return
				}
				
				if (endData.undreadsToActivate.let { it != null && it <= 0 } && activatePortal(world, aabb)) {
					endData.undreadsToActivate = null
					endData.isPortalActivated = true
					return
				}
				
				val rand = world.rand
				val mod = 3 * sqrt((ticks - 20) * 0.12F).floorToInt().coerceIn(2, 10)
				
				if (ticks % mod == 0 && rand.nextInt(5) != 0) {
					val targets = world.selectVulnerableEntities.inBox<EntityPlayer>(aabb)
					for (attempt in 0 until rand.nextRounded(1.19F)) {
						val (bb, amount) = endData.nextUndreadSpawn() ?: break
						spawnUndreads(world, targets, bb, amount)
						
						if (attempt == 0) {
							for (target in targets) {
								val playerVec = target.lookPosVec
								val spawnVec = bb.centerVec
								
								val dist = playerVec.distanceTo(spawnVec)
								val scale = if (dist < 9.0) dist else 9.0 + sqrt(dist - 8.0)
								val soundVec = playerVec.add(playerVec.directionTowards(spawnVec).scale(scale))
								
								ModBlocks.GRAVE_DIRT_PLAIN.soundType.let {
									it.breakSound.playPlayer(target, soundVec, SoundCategory.HOSTILE, it.volume * 0.9F, it.pitch + rand.nextFloat(-0.25F, 0.2F))
								}
							}
						}
					}
				}
			}
			
			private fun spawnUndreads(world: World, targets: List<EntityPlayer>, bb: BoundingBox, amount: Int) {
				for (pos in bb.min.allInBoxMutable(bb.max)) {
					if (pos.getBlock(world) is BlockGraveDirt) {
						pos.breakBlock(world, drops = false)
					}
				}
				
				if (amount == 0) {
					return
				}
				
				val rand = world.rand
				val dustList = mutableListOf<DustType>()
				
				repeat(rand.nextRounded(1.27F)) {
					val dust = when (rand.nextInt(0, 25 - (world.difficulty.id * 2))) {
						in 0..3   -> DustType.END_POWDER
						in 4..7   -> DustType.REDSTONE
						in 8..10  -> DustType.SUGAR
						in 11..13 -> DustType.GUNPOWDER
						in 14..15 -> DustType.ANCIENT_DUST
						else      -> null
					}
					
					if (dust != null) {
						dustList.add(dust)
					}
				}
				
				val dusts = UndreadDustEffects(Sets.newEnumSet(dustList, DustType::class.java))
				
				repeat(amount) {
					val undread = EntityMobUndread(world).apply {
						isForgottenTombsEnd = true
						attackTarget = rand.nextItemOrNull(targets)
						getAttribute(FOLLOW_RANGE).applyModifier(undreadFollowRangeModifier)
						addPotionEffect(Potions.SLOW_FALLING.makeEffect(Int.MAX_VALUE, 0, showParticles = false))
						enablePersistence()
					}
					
					for (attempt in 1..50) {
						val pos = Pos(
							rand.nextInt(bb.min.x, bb.max.x),
							rand.nextInt(bb.min.y, bb.max.y),
							rand.nextInt(bb.min.z, bb.max.z),
						).offsetUntil(UP, 0..10) {
							it.isAir(world) && it.up().isAir(world)
						} ?: continue
						
						undread.setLocationAndAngles(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, rand.nextFloat(0F, 360F), 0F)
						
						if (world.checkNoEntityCollision(undread)) {
							undread.onInitialSpawn(world, world.getDifficultyForLocation(pos), STRUCTURE, dusts, null)
							PacketClientFX(FX_SPAWN_UNDREAD, FxVecData(undread.posVec)).sendToAllAround(undread, 32.0)
							world.addEntity(undread)
							break
						}
					}
				}
			}
			
			private fun activatePortal(world: World, aabb: AxisAlignedBB): Boolean {
				val chunkX1 = aabb.minX.floorToInt() shr 4
				val chunkZ1 = aabb.minZ.floorToInt() shr 4
				val chunkX2 = aabb.maxX.ceilToInt() shr 4
				val chunkZ2 = aabb.maxZ.ceilToInt() shr 4
				var foundPortal = false
				
				for (chunkX in chunkX1..chunkX2) for (chunkZ in chunkZ1..chunkZ2) {
					val chunk = world.getChunk(chunkX, chunkZ)
					
					for (tile in chunk.tileEntityMap.values) {
						if (tile is TileEntityPortalInner.Void && tile.blockState[BlockVoidPortalInner.TYPE] == RETURN_INACTIVE) {
							tile.pos.setState(world, ModBlocks.VOID_PORTAL_INNER.with(BlockVoidPortalInner.TYPE, RETURN_ACTIVE))
							// TODO fx
							foundPortal = true
						}
					}
				}
				
				return foundPortal
			}
		})
	}
	
	override val tokenHolders = object : TerritoryTokenHolders() {
		override fun afterUse(holder: EntityTokenHolder, instance: TerritoryInstance) {
			instance.getStorageComponent<ForgottenTombsEndData>()?.startEndSequence(holder.world, instance, holder.posVec)
		}
	}
}
