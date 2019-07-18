package chylex.hee.game.particle
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.system.util.color.IColor
import chylex.hee.system.util.color.RGB
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.nextFloat
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleSmokeNormal
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

object ParticleSmokeCustom : IParticleMaker{
	@SideOnly(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	class Data(
		private val color: (Random) -> IColor = { rand -> RGB((rand.nextFloat(0F, 0.3F) * 255F).floorToInt().toUByte()) },
		private val scale: Float = 1F
	) : IParticleData{
		constructor(color: IColor, scale: Float = 1F) : this({ color }, scale)
		
		override fun generate(rand: Random): IntArray{
			return intArrayOf(color(rand).toInt(), (scale * 100F).floorToInt())
		}
	}
	
	private val DEFAULT_DATA = IParticleData.Static(intArrayOf(
		RGB(0u).toInt(), 0
	))
	
	@SideOnly(Side.CLIENT)
	private class Instance(
		world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, unsafeData: IntArray
	) : ParticleSmokeNormal(
		world, posX, posY, posZ, motX, motY, motZ, DEFAULT_DATA.validate(unsafeData)[1] * 0.01F
	){
		init{
			val color = DEFAULT_DATA.validate(unsafeData)[0]
			
			particleRed = ((color shr 16) and 255) / 255F
			particleGreen = ((color shr 8) and 255) / 255F
			particleBlue = (color and 255) / 255F
		}
	}
}
