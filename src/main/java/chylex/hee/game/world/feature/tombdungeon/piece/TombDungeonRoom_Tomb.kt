package chylex.hee.game.world.feature.tombdungeon.piece

import chylex.hee.game.entity.posVec
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.TOMB_DUNGEON_UNDREAD_SPAWNER
import chylex.hee.game.world.Pos
import chylex.hee.game.world.distanceSqTo
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel.MobAmount
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.TOMB_ENTRANCE_INSIDE
import chylex.hee.game.world.isAir
import chylex.hee.game.world.offsetWhile
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.EntityStructureTrigger
import chylex.hee.init.ModEntities
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.addY
import chylex.hee.system.math.square
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.random.nextFloat
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.use
import net.minecraft.entity.EntitySpawnPlacementRegistry
import net.minecraft.entity.SpawnReason.SPAWNER
import net.minecraft.particles.RedstoneParticleData
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.WEST
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random

abstract class TombDungeonRoom_Tomb(file: String, entranceY: Int, allowSecrets: Boolean, isFancy: Boolean) : TombDungeonRoom(file, isFancy) {
	final override val secretAttachWeight = if (allowSecrets) 2 else 0
	final override val secretAttachY = entranceY
	
	final override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(TOMB_ENTRANCE_INSIDE, Pos(centerX, entranceY, maxZ), SOUTH)
	)
	
	protected abstract val mobAmount: MobAmount?
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		generateSpawnerTrigger(world, instance)
	}
	
	protected open fun generateSpawnerTrigger(world: IStructureWorld, instance: Instance) {
		val rand = world.rand
		val level = instance.context
		
		if (rand.nextInt(3) == 0 && level != null) {
			val mobAmount = mobAmount
			
			if (mobAmount != null) {
				val (undreads, spiderlings) = level.pickUndreadAndSpiderlingSpawns(rand, mobAmount)
				UndreadSpawnerTrigger.place(world, entrance = connections.first().offset, width = maxX, depth = maxZ, undreads, spiderlings)
			}
		}
	}
	
	class UndreadSpawnerTrigger() : ITriggerHandler {
		companion object {
			private const val WIDTH_TAG = "Width"
			private const val DEPTH_TAG = "Depth"
			private const val UNDREADS_TAG = "Undreads"
			private const val SPIDERLINGS_TAG = "Spiderlings"
			
			fun place(world: IStructureWorld, entrance: BlockPos, width: Int, depth: Int, undreads: Int, spiderlings: Int) {
				val nbt = UndreadSpawnerTrigger(width - 1.0, depth - 1.0, undreads, spiderlings).serializeNBT()
				
				world.addTrigger(entrance, EntityStructureTrigger({ wrld ->
					EntityTechnicalTrigger(wrld, TOMB_DUNGEON_UNDREAD_SPAWNER, nbt).apply { rotationYaw = NORTH.horizontalAngle }
				}, yOffset = 0.5))
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
			
 			repeat(10) {
				(world as ServerWorld).spawnParticle(RedstoneParticleData(if (facing == NORTH || facing == EAST) 1F else 0F, if (facing == SOUTH || facing == EAST) 1F else 0F, if (facing == WEST) 1F else 0F, 1F),
					world.rand.nextFloat(aabb.minX, aabb.maxX),
					world.rand.nextFloat(aabb.minY, aabb.maxY),
					world.rand.nextFloat(aabb.minZ, aabb.maxZ),
					1, 0.0, 0.0, 0.0, 0.0
				)
			}//TODO
			
			if (world.players.none { aabb.contains(it.posVec) }) {
				return
			}
			
			val rand = world.rand
			
			for((entityCount, entityType) in listOf(
				undreads to ModEntities.UNDREAD,
				spiderlings to ModEntities.SPIDERLING
			)) {
				repeat(entityCount) {
					for(attempt in 1..20) {
						val pos = Pos(
							rand.nextFloat(aabb.minX, aabb.maxX),
							rand.nextFloat(aabb.minY, aabb.maxY) + 0.5,
							rand.nextFloat(aabb.minZ, aabb.maxZ),
						).offsetWhile(DOWN, 1..3) {
							it.isAir(world)
						}
						
						if (EntitySpawnPlacementRegistry.func_223515_a(entityType, world, SPAWNER, pos, rand) &&
							world.hasNoCollisions(entityType.getBoundingBoxWithSizeApplied(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)) &&
							world.players.none { pos.distanceSqTo(it) < square(2.75) }
						) {
							entityType.create(world)?.apply {
								setLocationAndAngles(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, rand.nextFloat(0F, 360F), 0F)
								spawnExplosionParticle()
								world.addEntity(this)
							}
							
							break
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
