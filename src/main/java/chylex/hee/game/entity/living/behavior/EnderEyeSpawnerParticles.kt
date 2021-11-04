package chylex.hee.game.entity.living.behavior

import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.entity.util.lookPosVec
import chylex.hee.game.fx.FxVecData
import chylex.hee.game.fx.FxVecHandler
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.network.client.PacketClientFX
import chylex.hee.util.color.IColorGenerator
import chylex.hee.util.color.RGB
import chylex.hee.util.math.Vec
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.addY
import chylex.hee.util.math.bottomCenter
import chylex.hee.util.math.square
import chylex.hee.util.math.subtractY
import chylex.hee.util.nbt.NBTObjectList
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getListOfCompounds
import chylex.hee.util.nbt.putList
import chylex.hee.util.nbt.use
import chylex.hee.util.random.nextInt
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World
import net.minecraftforge.common.util.INBTSerializable
import java.util.Random
import kotlin.math.pow
import kotlin.math.sqrt

class EnderEyeSpawnerParticles(private val entity: EntityBossEnderEye) : INBTSerializable<TagCompound> {
	companion object {
		private const val PARTICLE_LIST_TAG = "Particles"
		private const val X_TAG = "X"
		private const val Y_TAG = "Y"
		private const val Z_TAG = "Z"
		private const val DELAY_TAG = "Delay"
		private const val PREV_DIST_SQ_TAG = "PrevDistSq"
		private const val ORIG_DISTANCE_XZ_TAG = "OrigDistXZ"
		
		private val PARTICLE_TICK = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(color = IColorGenerator { RGB(nextInt(160, 220), nextInt(0, 30), nextInt(20, 50)) }, lifespan = 7..19, scale = 0.666F),
			maxRange = 256.0,
			hideOnMinimalSetting = false
		)
		
		val FX_PARTICLE = object : FxVecHandler() {
			override fun handle(world: World, rand: Random, vec: Vector3d) {
				PARTICLE_TICK.spawn(Point(vec, 2), rand)
			}
		}
	}
	
	private class ParticleInstance(pos: Vector3d, delay: Int, private var originalDistanceXZ: Float) : INBTSerializable<TagCompound> {
		constructor() : this(Vec3.ZERO, 0, 0F)
		
		var pos = pos
			private set
		
		var delay = delay
			private set
		
		private var prevDistSq = Float.MAX_VALUE
		
		fun tick(entity: EntityBossEnderEye): Boolean {
			if (delay > 0) {
				--delay
				return false
			}
			
			val lookPos = entity.lookPosVec.subtractY(entity.height * 0.25)
			
			val dir = lookPos.subtract(pos).normalize()
			val distSq = lookPos.squareDistanceTo(pos)
			
			pos = pos.add(dir.scale(0.04 + (0.08 * (distSq - 0.5).coerceAtLeast(0.0).pow(0.33))))
			
			if (entity.rng.nextInt(3) == 0) {
				val progress = sqrt(square(pos.x - entity.posX) + square(pos.z - entity.posZ)) / originalDistanceXZ
				val progressCurvePoint = when {
					progress < 0.3 -> progress / 0.3
					progress > 0.7 -> (1.0 - progress) / 0.3
					else           -> 1.0
				}
				
				PacketClientFX(FX_PARTICLE, FxVecData(pos.addY(sqrt(progressCurvePoint) * 6.0))).sendToAllAround(entity.world, pos, 256.0)
			}
			
			if (distSq > prevDistSq || distSq < square(0.15)) {
				return true
			}
			
			prevDistSq = distSq.toFloat()
			return false
		}
		
		override fun serializeNBT() = TagCompound().apply {
			putDouble(X_TAG, pos.x)
			putDouble(Y_TAG, pos.y)
			putDouble(Z_TAG, pos.z)
			putInt(DELAY_TAG, delay)
			putFloat(PREV_DIST_SQ_TAG, prevDistSq)
			putFloat(ORIG_DISTANCE_XZ_TAG, originalDistanceXZ)
		}
		
		override fun deserializeNBT(nbt: TagCompound) = nbt.use {
			pos = Vec(
				getDouble(X_TAG),
				getDouble(Y_TAG),
				getDouble(Z_TAG)
			)
			
			delay = getInt(DELAY_TAG)
			prevDistSq = getFloat(PREV_DIST_SQ_TAG)
			originalDistanceXZ = getFloat(ORIG_DISTANCE_XZ_TAG)
		}
	}
	
	// Manager
	
	private val particles = mutableListOf<ParticleInstance>()
	
	fun add(start: BlockPos) {
		val center = start.bottomCenter
		var delay = 0
		
		for (particle in particles) {
			if (center.squareDistanceTo(particle.pos) < square(0.8)) {
				delay = particle.delay + 15
			}
		}
		
		particles.add(ParticleInstance(center, delay, sqrt(square(entity.posX - center.x) + square(entity.posZ - center.z)).toFloat()))
	}
	
	fun tick() {
		particles.removeAll { it.tick(entity) }
	}
	
	override fun serializeNBT() = TagCompound().apply {
		putList(PARTICLE_LIST_TAG, NBTObjectList.of(particles.map(ParticleInstance::serializeNBT)))
	}
	
	override fun deserializeNBT(nbt: TagCompound) = nbt.use {
		particles.clear()
		particles.addAll(getListOfCompounds(PARTICLE_LIST_TAG).map { ParticleInstance().apply { deserializeNBT(it) } })
	}
}
