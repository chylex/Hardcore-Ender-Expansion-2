package chylex.hee.game.particle

import chylex.hee.game.block.BlockTablePedestal
import chylex.hee.game.particle.ParticleEnergyTransferToPedestal.Data
import chylex.hee.game.particle.base.ParticleBaseEnergyTransfer
import chylex.hee.game.particle.data.IParticleData
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.game.world.Pos
import chylex.hee.game.world.bottomCenter
import chylex.hee.game.world.getBlock
import chylex.hee.init.ModBlocks
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.Vec
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.addY
import net.minecraft.client.particle.Particle
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

object ParticleEnergyTransferToPedestal : IParticleMaker.WithData<Data>() {
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: Data?): Particle {
		return Instance(world, posX, posY, posZ, data)
	}
	
	class Data(
		val targetPos: BlockPos,
		val travelTime: Int,
	) : IParticleData.Self<Data>()
	
	@Sided(Side.CLIENT)
	class Instance(world: World, posX: Double, posY: Double, posZ: Double, data: Data?) : ParticleBaseEnergyTransfer(world, posX, posY, posZ) {
		override val targetPos: Vec3d
		
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
