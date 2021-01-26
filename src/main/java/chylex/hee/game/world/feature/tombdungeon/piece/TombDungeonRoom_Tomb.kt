package chylex.hee.game.world.feature.tombdungeon.piece

import chylex.hee.game.block.BlockGraveDirt
import chylex.hee.game.entity.living.EntityMobSpiderling
import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.game.entity.posVec
import chylex.hee.game.entity.selectExistingEntities
import chylex.hee.game.entity.selectVulnerableEntities
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.TOMB_DUNGEON_UNDREAD_SPAWNER
import chylex.hee.game.world.Pos
import chylex.hee.game.world.distanceSqTo
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel.MobAmount
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.TOMB_ENTRANCE_INSIDE
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.getState
import chylex.hee.game.world.isAir
import chylex.hee.game.world.offsetWhile
import chylex.hee.game.world.playClient
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.EntityStructureTrigger
import chylex.hee.init.ModEntities
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.fx.FxVecData
import chylex.hee.network.fx.FxVecHandler
import chylex.hee.system.math.Vec
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.addY
import chylex.hee.system.math.directionTowards
import chylex.hee.system.math.square
import chylex.hee.system.math.toYaw
import chylex.hee.system.migration.BlockWeb
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItem
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.use
import net.minecraft.entity.EntitySpawnPlacementRegistry
import net.minecraft.entity.SpawnReason.STRUCTURE
import net.minecraft.util.Direction.Axis
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.Random

abstract class TombDungeonRoom_Tomb(file: String, entranceY: Int, allowSecrets: Boolean, isFancy: Boolean) : TombDungeonRoom(file, isFancy) {
	final override val secretAttachWeight = if (allowSecrets) 2 else 0
	final override val secretAttachY = entranceY
	
	final override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(TOMB_ENTRANCE_INSIDE, Pos(centerX, entranceY, maxZ), SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		generateSpawnerTrigger(world, instance)
	}
	
	protected open fun generateSpawnerTrigger(world: IStructureWorld, instance: Instance) {
		val rand = world.rand
		val level = instance.context ?: return
		val mobAmount = getSpawnerTriggerMobAmount(rand, level) ?: return
		
		val (undreads, spiderlings) = level.pickUndreadAndSpiderlingSpawns(rand, mobAmount)
		MobSpawnerTrigger.place(world, entrance = connections.first().offset, width = maxX, depth = maxZ, undreads, spiderlings)
	}
	
	protected abstract fun getSpawnerTriggerMobAmount(rand: Random, level: TombDungeonLevel): MobAmount?
	
	class MobSpawnerTrigger() : ITriggerHandler {
		companion object {
			private const val WIDTH_TAG = "Width"
			private const val DEPTH_TAG = "Depth"
			private const val UNDREADS_TAG = "Undreads"
			private const val SPIDERLINGS_TAG = "Spiderlings"
			
			fun place(world: IStructureWorld, entrance: BlockPos, width: Int, depth: Int, undreads: Int, spiderlings: Int) {
				val nbt = MobSpawnerTrigger(width - 1.0, depth - 1.0, undreads, spiderlings).serializeNBT()
				
				world.addTrigger(entrance, EntityStructureTrigger({ wrld ->
					EntityTechnicalTrigger(wrld, TOMB_DUNGEON_UNDREAD_SPAWNER, nbt).apply { rotationYaw = NORTH.horizontalAngle }
				}, yOffset = 0.5))
			}
			
			val FX_SPAWN_UNDREAD = object : FxVecHandler() {
				override fun handle(world: World, rand: Random, vec: Vec3d) {
					EntityMobUndread(world).apply {
						setLocationAndAngles(vec.x, vec.y, vec.z, 0F, 0F)
						spawnExplosionParticle()
						deathSound.playClient(vec, soundCategory, volume = 1.2F, pitch = soundPitch * 0.7F)
					}
				}
			}
			
			val FX_SPAWN_SPIDERLING = object : FxVecHandler() {
				override fun handle(world: World, rand: Random, vec: Vec3d) {
					EntityMobSpiderling(world).apply {
						setLocationAndAngles(vec.x, vec.y, vec.z, 0F, 0F)
						spawnExplosionParticle()
						ambientSound.playClient(vec, soundCategory, volume = 1F, pitch = soundPitch)
					}
				}
			}
		}
		
		private var width = 0.0
		private var depth = 0.0
		private var undreads = 0
		private var spiderlings = 0
		
		constructor(width: Double, depth: Double, undreads: Int, spiderlings: Int) : this() {
			this.width = width
			this.depth = depth
			this.undreads = undreads
			this.spiderlings = spiderlings
		}
		
		override fun check(world: World): Boolean {
			return !world.isRemote
		}
		
		override fun update(entity: EntityTechnicalTrigger) {
			val world = entity.world
			val facing = entity.horizontalFacing
			val vecF = Vec3.fromYaw(facing.horizontalAngle)
			val vecL = Vec3.fromYaw(facing.rotateYCCW().horizontalAngle)
			val vecR = Vec3.fromYaw(facing.rotateY().horizontalAngle)
			
			val aabb = AxisAlignedBB(
				vecL.scale(width * 0.5).add(vecF.scale(0.3)),
				vecR.scale(width * 0.5).add(vecF.scale(depth)).addY(2.5)
			).offset(entity.posVec)
			
			val nearbyPlayers = world.selectVulnerableEntities.inBox<EntityPlayer>(aabb)
			if (nearbyPlayers.isEmpty()) {
				return
			}
			
			val rand = world.rand
			val minPlayerDist = ((depth * 0.4) + (width * 0.16)).coerceIn(1.5, 3.5)
			
			for((entityCount, entityType, spawnParticle) in listOf(
				Triple(undreads, ModEntities.UNDREAD, FX_SPAWN_UNDREAD),
				Triple(spiderlings, ModEntities.SPIDERLING, FX_SPAWN_SPIDERLING)
			)) {
				repeat(entityCount) {
					var bestPos: BlockPos? = null
					
					for(attempt in 1..75) {
						val pos = Pos(
							rand.nextFloat(aabb.minX, aabb.maxX),
							rand.nextFloat(aabb.minY, aabb.maxY) + 0.5,
							rand.nextFloat(aabb.minZ, aabb.maxZ),
						).offsetWhile(DOWN, 1..3) {
							it.isAir(world) || it.getBlock(world) is BlockWeb
						}
						
						val collisionCheckAABB = entityType.getBoundingBoxWithSizeApplied(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5).grow(0.2, 0.0, 0.2)
						
						if (EntitySpawnPlacementRegistry.func_223515_a(entityType, world, STRUCTURE, pos, rand) &&
							world.hasNoCollisions(collisionCheckAABB) &&
							world.selectExistingEntities.inBox<EntityLivingBase>(collisionCheckAABB.grow(0.4, 0.0, 0.4)).isEmpty() &&
							world.players.none { pos.distanceSqTo(it) < square(minPlayerDist) }
						) {
							bestPos = pos
							
							if (entityType === ModEntities.UNDREAD) {
								if (rand.nextInt(5) == 0 || pos.down().getBlock(world) is BlockGraveDirt) {
									break
								}
							}
							else {
								break
							}
						}
					}
					
					if (bestPos != null) {
						val x = bestPos.x + rand.nextFloat(0.35, 0.65)
						val y = bestPos.y + bestPos.down().getState(world).getCollisionShape(world, bestPos).getEnd(Axis.Y) - 1.0
						val z = bestPos.z + rand.nextFloat(0.35, 0.65)
						val vec = Vec(x, y, z)
						val target = rand.nextItem(nearbyPlayers)
						
						entityType.create(world)?.apply {
							setLocationAndAngles(x, y, z, vec.directionTowards(target.posVec).toYaw(), 0F)
							rotationYawHead = rotationYaw
							attackTarget = target
							onGround = true // allow instant pathfinding in MeleeAttackGoal
							
							if (this is EntityMobSpiderling) {
								wakeUp(preventSleep = true, aiDelayTicks = if (rand.nextInt(10) == 0) rand.nextInt(4, 6) else rand.nextInt(11, 18))
							}
							
							onInitialSpawn(world, world.getDifficultyForLocation(bestPos), STRUCTURE, null, null)
							world.addEntity(this)
							
							PacketClientFX(spawnParticle, FxVecData(vec)).sendToAllAround(this, 24.0)
						}
					}
				}
			}
			
			entity.remove()
		}
		
		override fun nextTimer(rand: Random): Int {
			return 3
		}
		
		override fun serializeNBT() = TagCompound().apply {
			putDouble(WIDTH_TAG, width)
			putDouble(DEPTH_TAG, depth)
			putInt(UNDREADS_TAG, undreads)
			putInt(SPIDERLINGS_TAG, spiderlings)
		}
		
		override fun deserializeNBT(nbt: TagCompound) = nbt.use {
			width = getDouble(WIDTH_TAG)
			depth = getDouble(DEPTH_TAG)
			undreads = getInt(UNDREADS_TAG)
			spiderlings = getInt(SPIDERLINGS_TAG)
		}
	}
}
