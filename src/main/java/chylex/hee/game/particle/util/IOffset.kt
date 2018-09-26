package chylex.hee.game.particle.util
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextVector
import net.minecraft.entity.Entity
import net.minecraft.util.EnumFacing
import java.util.Random

interface IOffset{
	fun next(out: MutableOffsetPoint, rand: Random)
	
	data class MutableOffsetPoint(var x: Float, var y: Float, var z: Float){
		constructor() : this(0F, 0F, 0F)
	}
	
	// General
	
	object None : IOffset{
		override fun next(out: MutableOffsetPoint, rand: Random){
			out.x = 0F
			out.y = 0F
			out.z = 0F
		}
	}
	
	class Constant(
		private val offsetX: Float,
		private val offsetY: Float,
		private val offsetZ: Float
	) : IOffset{
		constructor(offset: Float, towards: EnumFacing) : this(
			offsetX = offset * towards.frontOffsetX,
			offsetY = offset * towards.frontOffsetY,
			offsetZ = offset * towards.frontOffsetZ
		)
		
		override fun next(out: MutableOffsetPoint, rand: Random){
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
		private val maxZ: Float
	) : IOffset{
		constructor(maxOffsetX: Float, maxOffsetY: Float, maxOffsetZ: Float) : this(
			minX = -maxOffsetX,
			maxX =  maxOffsetX,
			minY = -maxOffsetY,
			maxY =  maxOffsetY,
			minZ = -maxOffsetZ,
			maxZ =  maxOffsetZ
		)
		
		constructor(maxOffset: Float) : this(maxOffset, maxOffset, maxOffset)
		
		constructor(entity: Entity, horizontalMp: Float = 1F, verticalMp: Float = 1F) : this(
			maxOffsetX = entity.width * 0.5F * horizontalMp,
			maxOffsetY = entity.height * 0.5F * verticalMp,
			maxOffsetZ = entity.width * 0.5F * horizontalMp
		)
		
		override fun next(out: MutableOffsetPoint, rand: Random){
			out.x = rand.nextFloat(minX, maxX)
			out.y = rand.nextFloat(minY, maxY)
			out.z = rand.nextFloat(minZ, maxZ)
		}
	}
	
	class InSphere(
		private val minRadius: Float,
		private val maxRadius: Float
	) : IOffset{
		constructor(radius: Float) : this(0F, radius)
		
		override fun next(out: MutableOffsetPoint, rand: Random){
			val vec = rand.nextVector(rand.nextFloat(minRadius, maxRadius).toDouble())
			out.x = vec.x.toFloat()
			out.y = vec.y.toFloat()
			out.z = vec.z.toFloat()
		}
	}
}
