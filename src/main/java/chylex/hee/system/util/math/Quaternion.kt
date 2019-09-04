package chylex.hee.system.util.math
import chylex.hee.system.util.square
import chylex.hee.system.util.toDegrees
import chylex.hee.system.util.toRadians
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.withSign

data class Quaternion(val x: Float, val y: Float, val z: Float, val w: Float){
	companion object{
		fun fromYawPitch(yaw: Float, pitch: Float): Quaternion{
			val yawRad = yaw.toRadians()
			val pitchRad = pitch.toRadians()
			
			val cy = cos(yawRad * 0.5)
			val sy = sin(yawRad * 0.5)
			val cp = cos(pitchRad * 0.5)
			val sp = sin(pitchRad * 0.5)
			
			return Quaternion(
				(-sy * sp).toFloat(),
				( cy * sp).toFloat(),
				( sy * cp).toFloat(),
				( cy * cp).toFloat()
			)
		}
	}
	
	val length
		get() = sqrt(square(x) + square(y) + square(z) + square(w))
	
	val normalized
		get() = this * (1F / length)
	
	val rotationYaw: Float
		get(){
			val sinYcosP = 2.0 * ((w * z) + (x * y))
			val cosYcosP = 1.0 - 2.0 * ((y * y) + (z * z))
			return atan2(sinYcosP, cosYcosP).toDegrees().toFloat()
		}
	
	val rotationPitch: Float
		get(){
			val sinP = 2.0 * ((w * y) - (z * x))
			
			val rad = if (abs(sinP) >= 1)
				(PI / 2).withSign(sinP)
			else
				asin(sinP)
			
			return rad.toDegrees().toFloat()
		}
	
	operator fun unaryPlus(): Quaternion{
		return this
	}
	
	operator fun unaryMinus(): Quaternion{
		return Quaternion(-x, -y, -z, -w)
	}
	
	operator fun plus(q: Quaternion): Quaternion{
		return Quaternion(x + q.x, y + q.y, z + q.z, w + q.w)
	}
	
	operator fun minus(q: Quaternion): Quaternion{
		return Quaternion(x - q.x, y - q.y, z - q.z, w - q.w)
	}
	
	operator fun times(f: Float): Quaternion{
		return Quaternion(x * f, y * f, z * f, w * f)
	}
	
	operator fun times(f: Double): Quaternion{
		return times(f.toFloat())
	}
	
	fun dot(q: Quaternion): Float{
		return (x * q.x) + (y * q.y) + (z * q.z) + (w * q.w)
	}
	
	fun slerp(target: Quaternion, progress: Float): Quaternion{
		var q1 = this.normalized
		val q2 = target.normalized
		
		var dot = q1.dot(q2)
		
		if (dot < 0F){
			q1 = -q1
			dot = -dot
		}
		
		if (dot > 0.9995F){
			return (q1 + ((q2 - q1) * progress)).normalized
		}
		
		val t0 = acos(dot)
		val t = t0 * progress
		
		val st0 = sin(t0)
		val st = sin(t)
		
		val s0 = cos(t) - (dot * st / st0)
		val s1 = st / st0
		
		return (q1 * s0) + (q2 * s1)
	}
}
