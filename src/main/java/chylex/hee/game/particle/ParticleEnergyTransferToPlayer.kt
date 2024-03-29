package chylex.hee.game.particle

import chylex.hee.client.model.util.ModelHelper
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.item.interfaces.getHeeInterface
import chylex.hee.game.mechanics.energy.IEnergyItem
import chylex.hee.game.particle.ParticleEnergyTransferToPlayer.TransferData
import chylex.hee.game.particle.base.ParticleBaseEnergyTransfer
import chylex.hee.game.particle.data.IParticleData
import chylex.hee.game.particle.data.ParticleDataColorScale
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.Vec3
import net.minecraft.client.particle.Particle
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand.MAIN_HAND
import net.minecraft.util.Hand.OFF_HAND
import net.minecraft.util.math.vector.Vector3d
import java.lang.ref.WeakReference
import java.util.Random

object ParticleEnergyTransferToPlayer : IParticleMaker.WithData<TransferData?>() {
	@Sided(Side.CLIENT)
	override fun create(world: ClientWorld, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: TransferData?): Particle {
		return Instance(world, posX, posY, posZ, data)
	}
	
	class Data(
		cluster: TileEntityEnergyCluster,
		player: PlayerEntity,
		val speed: Double,
	) : IParticleData<TransferData?> {
		private val clusterDataGenerator = cluster.particleDataGenerator
		private val player = WeakReference(player)
		
		override fun generate(rand: Random): TransferData? {
			return clusterDataGenerator?.let { TransferData(it.generate(rand), player, speed) }
		}
	}
	
	class TransferData(val cluster: ParticleDataColorScale, val player: WeakReference<PlayerEntity>, val speed: Double)
	
	@Sided(Side.CLIENT)
	class Instance(world: ClientWorld, posX: Double, posY: Double, posZ: Double, data: TransferData?) : ParticleBaseEnergyTransfer(world, posX, posY, posZ) {
		override val targetPos: Vector3d
			get() = newTargetPos
		
		private val speed: Double
		private val player: WeakReference<PlayerEntity>?
		private var newTargetPos = Vec3.ZERO
		
		init {
			selectSpriteRandomly(ParticleEnergyTransferToPlayer.sprite)
			
			if (data == null) {
				speed = 0.0
				player = null
				setExpired()
			}
			else {
				loadColor(data.cluster.color)
				particleAlpha = 0.75F
				
				particleScale = 0.3F + (data.cluster.scale * 0.1F)
				
				speed = data.speed
				player = data.player
			}
		}
		
		override fun tick() {
			val player = player?.get()
			
			if (player == null) {
				setExpired()
				return
			}
			
			newTargetPos = ModelHelper.getHandPosition(player, if (player.getHeldItem(OFF_HAND).item.getHeeInterface<IEnergyItem>() != null) OFF_HAND else MAIN_HAND)
			
			setupMotion(speed + (age * 0.005))
			super.tick()
		}
	}
}
