package chylex.hee.game.particle
import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.particle.base.ParticleBaseEnergyTransfer
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.game.render.util.RGB
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.getBlock
import net.minecraft.client.particle.Particle
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

object ParticleEnergyTransferToPedestal : IParticleMaker{
	@SideOnly(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, data)
	}
	
	class Data(private val targetPos: BlockPos, private val travelTime: Int) : IParticleData{
		override fun generate(rand: Random) = intArrayOf(
			targetPos.x,
			targetPos.y,
			targetPos.z,
			travelTime
		)
	}
	
	@SideOnly(Side.CLIENT)
	class Instance(world: World, posX: Double, posY: Double, posZ: Double, unsafeData: IntArray) : ParticleBaseEnergyTransfer(world, posX, posY, posZ){
		override val targetPos: Vec3d
		
		init{
			if (unsafeData.size < 4){
				particleAlpha = 0F
				particleMaxAge = 0
				
				targetPos = Vec3d.ZERO
			}
			else{
				loadColor(RGB(40u).toInt())
				particleAlpha = 0.9F
				
				particleScale = 0.75F
				
				targetPos = Vec3d(unsafeData[0] + 0.5, unsafeData[1] + BlockTablePedestal.PARTICLE_TARGET_Y, unsafeData[2] + 0.5)
				setupMotion(Vec3d(posX, posY, posZ).distanceTo(targetPos) / unsafeData[3])
			}
		}
		
		override fun onUpdate(){
			super.onUpdate()
			
			if (Pos(targetPos).getBlock(world) !== ModBlocks.TABLE_PEDESTAL){
				if (particleAge < particleMaxAge - 5){
					particleMaxAge = particleAge + 5
				}
				
				particleAlpha -= 0.2F
			}
		}
	}
}
