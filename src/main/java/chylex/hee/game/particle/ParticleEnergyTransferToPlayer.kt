package chylex.hee.game.particle
import chylex.hee.client.model.ModelHelper
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.item.ItemAbstractEnergyUser
import chylex.hee.game.particle.ParticleEnergyTransferToPlayer.TransferData
import chylex.hee.game.particle.base.ParticleBaseEnergyTransfer
import chylex.hee.game.particle.data.IParticleData
import chylex.hee.game.particle.data.ParticleDataColorScale
import chylex.hee.game.particle.spawner.IParticleMaker
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Hand.MAIN_HAND
import chylex.hee.system.migration.Hand.OFF_HAND
import net.minecraft.client.particle.Particle
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.lang.ref.WeakReference
import java.util.Random

object ParticleEnergyTransferToPlayer : IParticleMaker.WithData<TransferData?>(){
	@Sided(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: TransferData?): Particle{
		return Instance(world, posX, posY, posZ, data)
	}
	
	class Data(
		cluster: TileEntityEnergyCluster,
		player: EntityPlayer,
		val speed: Double
	) : IParticleData<TransferData?>{
		private val clusterDataGenerator = cluster.particleDataGenerator
		private val player = WeakReference(player)
		
		override fun generate(rand: Random): TransferData?{
			return clusterDataGenerator?.let { TransferData(it.generate(rand), player, speed) }
		}
	}
	
	class TransferData(val cluster: ParticleDataColorScale, val player: WeakReference<EntityPlayer>, val speed: Double)
	
	@Sided(Side.CLIENT)
	class Instance(world: World, posX: Double, posY: Double, posZ: Double, data: TransferData?) : ParticleBaseEnergyTransfer(world, posX, posY, posZ){
		override val targetPos: Vec3d
			get() = newTargetPos
		
		private val speed: Double
		private val player: WeakReference<EntityPlayer>?
		private var newTargetPos = Vec3d.ZERO
		
		init{
			selectSpriteRandomly(ParticleEnergyTransferToPlayer.sprite)
			
			if (data == null){
				speed = 0.0
				player = null
				setExpired()
			}
			else{
				loadColor(data.cluster.color)
				particleAlpha = 0.75F
				
				particleScale = 0.3F + (data.cluster.scale * 0.1F)
				
				speed = data.speed
				player = data.player
			}
		}
		
		override fun tick(){
			val player = player?.get()
			
			if (player == null){
				setExpired()
				return
			}
			
			newTargetPos = ModelHelper.getHandPosition(player, if (player.getHeldItem(MAIN_HAND).item is ItemAbstractEnergyUser) MAIN_HAND else OFF_HAND)
			
			setupMotion(speed + (age * 0.005))
			super.tick()
		}
	}
}
