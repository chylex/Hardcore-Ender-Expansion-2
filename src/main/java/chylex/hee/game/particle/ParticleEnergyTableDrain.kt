package chylex.hee.game.particle
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.particle.base.ParticleBaseEnergy
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.floorToInt
import net.minecraft.client.particle.Particle
import net.minecraft.world.World
import org.apache.commons.lang3.ArrayUtils.EMPTY_INT_ARRAY
import java.util.Random

object ParticleEnergyTableDrain : IParticleMaker{
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, motX, motY, motZ, data)
	}
	
	class Data(private val cluster: TileEntityEnergyCluster) : IParticleData{
		override fun generate(rand: Random): IntArray{
			val data = cluster.particleDataGenerator?.next(rand) ?: return EMPTY_INT_ARRAY
			
			return intArrayOf(
				data.color,
				((0.45F + (data.scale * 0.15F)) * 100F).floorToInt()
			)
		}
	}
	
	@Sided(Side.CLIENT)
	private class Instance(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, unsafeData: IntArray) : ParticleBaseEnergy(world, posX, posY, posZ, motX, motY, motZ){
		init{
			if (unsafeData.size < 2){
				particleAlpha = 0F
				particleMaxAge = 0
			}
			else{
				loadColor(unsafeData[0])
				particleAlpha = 1F
				
				particleScale = unsafeData[1] * 0.01F
				
				particleMaxAge = 6
			}
		}
		
		override fun onUpdate(){
			super.onUpdate()
			
			particleAlpha -= 0.16F
		}
	}
}
