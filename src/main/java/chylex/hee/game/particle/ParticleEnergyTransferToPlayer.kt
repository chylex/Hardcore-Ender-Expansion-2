package chylex.hee.game.particle
import chylex.hee.HEE
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.item.ItemAbstractEnergyUser
import chylex.hee.game.particle.base.ParticleBaseEnergyTransfer
import chylex.hee.game.particle.spawner.factory.IParticleData
import chylex.hee.game.particle.spawner.factory.IParticleMaker
import chylex.hee.system.util.Vec3
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.subtractY
import net.minecraft.client.particle.Particle
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumHand.MAIN_HAND
import net.minecraft.util.EnumHandSide.RIGHT
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.commons.lang3.ArrayUtils
import java.lang.ref.WeakReference
import java.util.Random
import kotlin.math.abs
import kotlin.math.pow

object ParticleEnergyTransferToPlayer : IParticleMaker{
	@SideOnly(Side.CLIENT)
	override fun create(world: World, posX: Double, posY: Double, posZ: Double, motX: Double, motY: Double, motZ: Double, data: IntArray): Particle{
		return Instance(world, posX, posY, posZ, data)
	}
	
	class Data(private val cluster: TileEntityEnergyCluster, private val player: EntityPlayer, private val speed: Double) : IParticleData{
		override fun generate(rand: Random): IntArray{
			val data = cluster.particleDataGenerator?.next(rand) ?: return ArrayUtils.EMPTY_INT_ARRAY
			
			return intArrayOf(
				data.color,
				((0.3F + (data.scale * 0.1F)) * 100F).floorToInt(),
				(speed * 100F).floorToInt(),
				player.entityId
			)
		}
	}
	
	@SideOnly(Side.CLIENT)
	class Instance(world: World, posX: Double, posY: Double, posZ: Double, unsafeData: IntArray) : ParticleBaseEnergyTransfer(world, posX, posY, posZ){
		override val targetPos: Vec3d
			get() = newTargetPos
		
		private val speed: Double
		private val player: WeakReference<EntityPlayer?>
		private var newTargetPos = Vec3d.ZERO
		
		init{
			if (unsafeData.size < 4){
				particleAlpha = 0F
				particleMaxAge = 0
				
				speed = 0.0
				player = WeakReference(null)
			}
			else{
				loadColor(unsafeData[0])
				particleAlpha = 0.75F
				
				particleScale = unsafeData[1] * 0.01F
				
				speed = unsafeData[2] * 0.01
				player = WeakReference(world.getEntityByID(unsafeData[3]) as? EntityPlayer)
			}
		}
		
		override fun onUpdate(){
			val player = player.get()
			
			if (player == null){
				setExpired()
				return
			}
			
			val yawOffsetMp = (if (player.primaryHand == RIGHT) 1 else -1) * (if (player.getHeldItem(MAIN_HAND).item is ItemAbstractEnergyUser) 1 else -1)
			
			if (player === HEE.proxy.getClientSidePlayer() && settings.thirdPersonView == 0){
				val pitch = MathHelper.wrapDegrees(player.rotationPitch)
				val yaw = MathHelper.wrapDegrees(player.rotationYaw)
				val fov = settings.fovSetting
				
				newTargetPos = player
					.lookPosVec
					.subtractY(0.1 + (pitch.coerceIn(-90F, 45F) * 0.0034))
					.add(Vec3.fromYaw(yaw + (yawOffsetMp * fov * 0.6F)).scale(0.25))
					.add(Vec3.fromYaw(yaw + 165F - (fov - 90F) / 2F).scale(abs(pitch.coerceIn(0F, 90F) / 90.0).pow(1.5) * (0.3 - abs(fov - 90.0) / 600F)))
				
				// TODO kinda weird and inaccurate, maybe use the camera transformations somehow?
				
			}
			else{
				val handOffset = if (player.isSneaking) 1.15 else 0.75
				
				newTargetPos = player
					.lookPosVec
					.subtractY(handOffset)
					.add(Vec3d.fromPitchYaw(0F, player.renderYawOffset + yawOffsetMp * 39F).scale(0.52))
			}
			
			setupMotion(speed + (particleAge * 0.005))
			super.onUpdate()
		}
	}
}
