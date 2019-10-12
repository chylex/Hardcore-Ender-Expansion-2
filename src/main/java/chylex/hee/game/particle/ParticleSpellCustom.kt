package chylex.hee.game.particle
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IRandomColor
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.nextFloat
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleSpell
import net.minecraft.world.World
import java.util.Random

object ParticleSpellCustom : IParticleMaker{
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	class Data(
		private val color: IRandomColor,
		private val scale: ClosedFloatingPointRange<Float> = 0F..0F
	) : IParticleData{
		constructor(color: IntColor, scale: ClosedFloatingPointRange<Float> = 0F..0F) : this(IRandomColor.Static(color), scale)
		
		override fun generate(rand: Random): IntArray{
			return intArrayOf(color.next(rand).i, (rand.nextFloat(scale) * 100F).floorToInt())
		}
	}
	
	private val DEFAULT_DATA = IParticleData.Static(intArrayOf(
		RGB(0u).i, 0
	))
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, unsafeData: IntArray) : ParticleSpell(world, posX, posY, posZ, 0.0, 0.0, 0.0){
		init{
			val data = DEFAULT_DATA.validate(unsafeData)
			val color = IntColor(data[0])
			
			motionX = motX
			motionY = motY
			motionZ = motZ
			
			particleRed = color.red / 255F
			particleGreen = color.green / 255F
			particleBlue = color.blue / 255F
			
			particleScale *= data[1] * 0.01F
		}
	}
}
