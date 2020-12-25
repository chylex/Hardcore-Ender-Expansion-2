package chylex.hee.game.particle

import chylex.hee.game.particle.ParticleDeathFlowerHeal.Data
import chylex.hee.game.particle.data.IParticleData
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.component1
import chylex.hee.system.math.component2
import chylex.hee.system.math.component3
import chylex.hee.system.math.offsetTowards
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.SuspendedTownParticle
import net.minecraft.world.World

object ParticleDeathFlowerHeal : IParticleMaker.WithData<Data>() {
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: Data?): Particle {
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	class Data(val healLevel: Float) : IParticleData.Self<Data>()
	
	private val COLOR_MIN = RGB(164, 78, 202).asVec
	private val COLOR_MAX = RGB(232, 85, 252).asVec
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: Data?) : SuspendedTownParticle(world, posX, posY, posZ, motX, motY, motZ) {
		init {
			selectSpriteRandomly(ParticleDeathFlowerHeal.sprite)
			
			val level = data?.healLevel ?: 1F
			
			if (level < 1F) {
				maxAge /= 2
			}
			
			val (r, g, b) = COLOR_MIN.offsetTowards(COLOR_MAX, level.toDouble())
			
			particleRed = r.toFloat()
			particleGreen = g.toFloat()
			particleBlue = b.toFloat()
		}
	}
}
