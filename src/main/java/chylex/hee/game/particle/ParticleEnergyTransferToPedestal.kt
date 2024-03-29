package chylex.hee.game.particle

import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.particle.ParticleEnergyTransferToPedestal.Data
import chylex.hee.game.particle.base.ParticleBaseEnergyTransfer
import chylex.hee.game.particle.data.IParticleData
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.game.world.util.getBlock
import chylex.hee.init.ModBlocks
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Vec
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.addY
import chylex.hee.util.math.bottomCenter
import net.minecraft.client.particle.Particle
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d

object ParticleEnergyTransferToPedestal : IParticleMaker.WithData<Data>() {
	@Sided(Side.CLIENT)
	override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: Data?): Particle {
		return Instance(world, posX, posY, posZ, data)
	}
	
	class Data(
		val targetPos: BlockPos,
		val travelTime: Int,
	) : IParticleData.Self<Data>()
	
	@Sided(Side.CLIENT)
	class Instance(world: ClientWorld, posX: Double, posY: Double, posZ: Double, data: Data?) : ParticleBaseEnergyTransfer(world, posX, posY, posZ) {
		override val targetPos: Vector3d
		
		init {
			selectSpriteRandomly(ParticleEnergyTransferToPedestal.sprite)
			
			if (data == null) {
				targetPos = Vec3.ZERO
				setExpired()
			}
			else {
				loadColor(RGB(40u))
				particleAlpha = 0.9F
				
				particleScale = 0.75F
				
				targetPos = data.targetPos.bottomCenter.addY(BlockTablePedestal.PARTICLE_TARGET_Y)
				setupMotion(Vec(posX, posY, posZ).distanceTo(targetPos) / data.travelTime)
			}
		}
		
		override fun tick() {
			super.tick()
			
			if (Pos(targetPos).getBlock(world) !== ModBlocks.TABLE_PEDESTAL) {
				if (age < maxAge - 5) {
					maxAge = age + 5
				}
				
				particleAlpha -= 0.2F
			}
		}
	}
}
