package chylex.hee.game.particle
import chylex.hee.game.particle.data.ParticleDataColorScale
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.color.IntColor
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.random.IRandomColor
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.SpellParticle
import net.minecraft.client.world.ClientWorld

object ParticleSpellCustom : IParticleMaker.WithData<ParticleDataColorScale>(){
	@Sided(Side.CLIENT)
	override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorScale?): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	fun Data(
		color: IntColor,
		scale: ClosedFloatingPointRange<Float>
	) = ParticleDataColorScale.Generator(IRandomColor.Static(color), scale)
	
	@Sided(Side.CLIENT)
	private class Instance(
		world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: ParticleDataColorScale?
	) : SpellParticle(
		world, posX, posY, posZ, 0.0, 0.0, 0.0, sprite
	){
		init{
			motionX = motX
			motionY = motY
			motionZ = motZ
			
			if (data == null){
				setExpired()
			}
			else{
				val color = data.color
				
				particleRed = color.red / 255F
				particleGreen = color.green / 255F
				particleBlue = color.blue / 255F
				
				particleScale *= data.scale
			}
		}
	}
}
