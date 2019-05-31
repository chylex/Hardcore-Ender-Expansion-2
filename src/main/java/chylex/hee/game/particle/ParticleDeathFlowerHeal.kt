package chylex.hee.game.particle
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.game.particle.util.ParticleTexture
import chylex.hee.system.util.color.RGB
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.offsetTowards
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleSuspendedTown
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

object ParticleDeathFlowerHeal : IParticleMaker{
	@SideOnly(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	class Data(
		healLevel: Float = 1F
	) : IParticleData.Static(intArrayOf(
		(healLevel * 100F).floorToInt()
	))
	
	private val DEFAULT_DATA = Data()
	
	private val COLOR_MIN = RGB(164, 78, 202).toVec()
	private val COLOR_MAX = RGB(232, 85, 252).toVec()
	
	@SideOnly(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, unsafeData: IntArray) : ParticleSuspendedTown(world, posX, posY, posZ, motX, motY, motZ){
		init{
			particleTexture = ParticleTexture.STAR
			
			val level = DEFAULT_DATA.validate(unsafeData)[0] * 0.01F
			
			if (level < 1F){
				particleMaxAge /= 2
			}
			
			val (r, g, b) = COLOR_MIN.offsetTowards(COLOR_MAX, level.toDouble())
			
			particleRed = r.toFloat()
			particleGreen = g.toFloat()
			particleBlue = b.toFloat()
		}
		
		override fun getFXLayer() = 1
		override fun setParticleTextureIndex(index: Int){}
	}
}
