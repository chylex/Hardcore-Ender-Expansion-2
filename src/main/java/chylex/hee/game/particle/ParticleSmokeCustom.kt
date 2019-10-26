package chylex.hee.game.particle
import chylex.hee.game.particle.data.ParticleDataColorScale
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IRandomColor
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.nextFloat
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleSmokeNormal
import net.minecraft.world.World
import java.util.Random

object ParticleSmokeCustom : IParticleMaker<ParticleDataColorScale>{
	private val rand = Random()
	
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorScale?): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data ?: DEFAULT_DATA.generate(rand))
	}
	
	fun Data(
		color: IRandomColor = DefaultColor,
		scale: Float
	) = ParticleDataColorScale.Generator(color, scale..scale)
	
	fun Data(
		color: IntColor,
		scale: Float
	) = ParticleDataColorScale.Generator(IRandomColor.Static(color), scale..scale)
	
	private object DefaultColor : IRandomColor{
		override fun next(rand: Random): IntColor{
			return RGB((rand.nextFloat(0F, 0.3F) * 255F).floorToInt().toUByte())
		}
	}
	
	private val DEFAULT_DATA = Data(scale = 1F)
	
	@Sided(Side.CLIENT)
	private class Instance(
		world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorScale
	) : ParticleSmokeNormal(
		world, posX, posY, posZ, motX, motY, motZ, data.scale
	){
		init{
			val color = data.color
			
			particleRed = color.red / 255F
			particleGreen = color.green / 255F
			particleBlue = color.blue / 255F
		}
	}
}
