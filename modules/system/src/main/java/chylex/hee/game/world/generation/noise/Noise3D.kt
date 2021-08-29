package chylex.hee.game.world.generation.noise

import chylex.hee.game.world.generation.noise.OpenSimplex2S.GenerateContext3D
import chylex.hee.game.world.generation.noise.OpenSimplex2S.LatticeOrientation3D
import chylex.hee.util.math.MutablePos
import net.minecraft.util.math.BlockPos
import java.util.Random

sealed class Noise3D {
	
	// Generation
	
	abstract fun getRawValue(x: Double, y: Double, z: Double): Double
	
	// Implementations
	
	sealed class SuperSimplex(rand: Random) : Noise3D() {
		protected val generator = OpenSimplex2S(rand.nextLong())
		protected abstract val orientation: LatticeOrientation3D
		
		class AreaNoise(val buffer: Array<Array<DoubleArray>>) {
			operator fun get(x: Int, y: Int, z: Int) = buffer[z][y][x]
			
			inline fun forEach(callback: (BlockPos.Mutable, Double) -> Unit) {
				val pos = MutablePos()
				
				for ((z, yArray) in buffer.withIndex()) {
					pos.z = z
					
					for ((y, xArray) in yArray.withIndex()) {
						pos.y = y
						
						for ((x, value) in xArray.withIndex()) {
							pos.x = x
							callback(pos, value)
						}
					}
				}
			}
		}
		
		class AreaGenerator(private val generator: OpenSimplex2S, private val context: GenerateContext3D) {
			fun generate(x: Int, y: Int, z: Int, xSize: Int, ySize: Int, zSize: Int): AreaNoise {
				val buffer = Array(zSize) { Array(ySize) { DoubleArray(xSize) { 0.0 } } }
				return buffer.apply { generator.generate3(context, this, x, y, z) }.let(::AreaNoise)
			}
		}
		
		fun getAreaGenerator(xFreq: Double, yFreq: Double, zFreq: Double, amplitude: Double): AreaGenerator {
			return AreaGenerator(generator, GenerateContext3D(orientation, xFreq, yFreq, zFreq, amplitude))
		}
		
		fun getAreaGenerator(xzFreq: Double, yFreq: Double, amplitude: Double): AreaGenerator {
			return getAreaGenerator(xzFreq, yFreq, xzFreq, amplitude)
		}
		
		class Classic(rand: Random) : SuperSimplex(rand) {
			override val orientation
				get() = LatticeOrientation3D.Classic
			
			override fun getRawValue(x: Double, y: Double, z: Double): Double {
				return generator.noise3_Classic(x, y, z)
			}
		}
		
		class Terrain(rand: Random) : SuperSimplex(rand) {
			override val orientation
				get() = LatticeOrientation3D.XZBeforeY
			
			override fun getRawValue(x: Double, y: Double, z: Double): Double {
				return generator.noise3_XZBeforeY(x, y, z)
			}
		}
	}
}
