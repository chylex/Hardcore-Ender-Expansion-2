package chylex.hee.game.particle.spawner.properties

import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextVector
import net.minecraft.entity.Entity
import net.minecraft.util.Direction
import net.minecraft.util.math.vector.Vector3d
import java.util.Random

interface IOffset {
	fun next(out: MutableOffsetPoint, rand: Random)
	
	operator fun plus(other: IOffset) = Sum(this, other)
	
	data class MutableOffsetPoint(var x: Float, var y: Float, var z: Float) {
		constructor() : this(0F, 0F, 0F)
	}
	
	// General
	
	object None : IOffset {
		override fun next(out: MutableOffsetPoint, rand: Random) {
			out.x = 0F
			out.y = 0F
			out.z = 0F
		}
	}
	
	class Constant(
		private val offsetX: Float,
		private val offsetY: Float,
		private val offsetZ: Float,
	) : IOffset {
		constructor(offset: Float, towards: Direction) : this(
			offsetX = offset * towards.xOffset,
			offsetY = offset * towards.yOffset,
			offsetZ = offset * towards.zOffset
		)
		
		constructor(offset: Vector3d) : this(
			offsetX = offset.x.toFloat(),
			offsetY = offset.y.toFloat(),
			offsetZ = offset.z.toFloat()
		)
		
		override fun next(out: MutableOffsetPoint, rand: Random) {
			out.x = offsetX
			out.y = offsetY
			out.z = offsetZ
		}
	}
	
	class InBox(
		private val minX: Float,
		private val maxX: Float,
		private val minY: Float,
		private val maxY: Float,
		private val minZ: Float,
		private val maxZ: Float,
	) : IOffset {
		constructor(maxOffsetX: Float, maxOffsetY: Float, maxOffsetZ: Float) : this(
			minX = -maxOffsetX,
			maxX = +maxOffsetX,
			minY = -maxOffsetY,
			maxY = +maxOffsetY,
			minZ = -maxOffsetZ,
			maxZ = +maxOffsetZ
		)
		
		constructor(maxOffset: Float) : this(maxOffset, maxOffset, maxOffset)
		
		constructor(entity: Entity, extraOffsetX: Float, extraOffsetY: Float, extraOffsetZ: Float) : this(
			maxOffsetX = (entity.width * 0.5F) + extraOffsetX,
			maxOffsetY = (entity.height * 0.5F) + extraOffsetY,
			maxOffsetZ = (entity.width * 0.5F) + extraOffsetZ
		)
		
		constructor(entity: Entity, extraOffset: Float = 0F) : this(entity, extraOffset, extraOffset, extraOffset)
		
		override fun next(out: MutableOffsetPoint, rand: Random) {
			out.x = rand.nextFloat(minX, maxX)
			out.y = rand.nextFloat(minY, maxY)
			out.z = rand.nextFloat(minZ, maxZ)
		}
	}
	
	class OutlineBox(
		private val minX: Float,
		private val maxX: Float,
		private val minY: Float,
		private val maxY: Float,
		private val minZ: Float,
		private val maxZ: Float,
	) : IOffset {
		constructor(maxOffsetX: Float, maxOffsetY: Float, maxOffsetZ: Float) : this(
			minX = -maxOffsetX,
			maxX = +maxOffsetX,
			minY = -maxOffsetY,
			maxY = +maxOffsetY,
			minZ = -maxOffsetZ,
			maxZ = +maxOffsetZ
		)
		
		override fun next(out: MutableOffsetPoint, rand: Random) {
			out.x = rand.nextFloat(minX, maxX)
			out.y = rand.nextFloat(minY, maxY)
			out.z = rand.nextFloat(minZ, maxZ)
			
			when (rand.nextInt(6)) {
				0 -> out.x = minX
				1 -> out.x = maxX
				2 -> out.y = minY
				3 -> out.y = maxY
				4 -> out.z = minZ
				5 -> out.z = maxZ
			}
		}
	}
	
	class InSphere(
		private val minRadius: Float,
		private val maxRadius: Float,
	) : IOffset {
		constructor(radius: Float) : this(0F, radius)
		
		override fun next(out: MutableOffsetPoint, rand: Random) {
			val vec = rand.nextVector(rand.nextFloat(minRadius, maxRadius).toDouble())
			out.x = vec.x.toFloat()
			out.y = vec.y.toFloat()
			out.z = vec.z.toFloat()
		}
	}
	
	class Gaussian(
		private val mpX: Float,
		private val mpY: Float,
		private val mpZ: Float,
	) : IOffset {
		constructor(mp: Float) : this(mp, mp, mp)
		
		override fun next(out: MutableOffsetPoint, rand: Random) {
			out.x = (rand.nextGaussian() * mpX).toFloat()
			out.y = (rand.nextGaussian() * mpY).toFloat()
			out.z = (rand.nextGaussian() * mpZ).toFloat()
		}
	}
	
	// Math
	
	class Sum(
		private val offset1: IOffset,
		private val offset2: IOffset,
	) : IOffset {
		override fun next(out: MutableOffsetPoint, rand: Random) {
			offset1.next(out, rand)
			val (firstX, firstY, firstZ) = out
			
			offset2.next(out, rand)
			out.x += firstX
			out.y += firstY
			out.z += firstZ
		}
	}
}
