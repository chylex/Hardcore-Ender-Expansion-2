package chylex.hee.game.world.util
import chylex.hee.HEE
import chylex.hee.game.mechanics.instability.Instability
import chylex.hee.game.particle.ParticleTeleport
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Line
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.client.PacketClientFX.IFXData
import chylex.hee.network.client.PacketClientFX.IFXHandler
import chylex.hee.system.util.Pos
import chylex.hee.system.util.center
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.offsetTowards
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.readCompactVec
import chylex.hee.system.util.readString
import chylex.hee.system.util.use
import chylex.hee.system.util.writeCompactVec
import chylex.hee.system.util.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.EnderTeleportEvent
import java.util.Random

class Teleporter(
	private val resetFall: Boolean,
	private val resetPathfinding: Boolean = true,
	private val damageDealt: Float = 0F,
	private val causedInstability: UShort = 0u,
	private val extendedEffectRange: Float = 0F
){
	companion object{
		private const val EXTENDED_MINIMUM_VOLUME = 0.1F
		private const val RANGE_FOR_MINIMUM_VOLUME = 16.0 - (16.0 * EXTENDED_MINIMUM_VOLUME)
		
		private val PARTICLE_POS = InBox(0.2F)
		private val PARTICLE_MOT = InBox(0.035F)
		
		class FxTeleportData(
			private val startPoint: Vec3d,
			private val endPoint: Vec3d,
			private val soundCategory: SoundCategory,
			private val soundVolume: Float,
			private val extendedRange: Float
		) : IFXData{
			override fun write(buffer: ByteBuf) = buffer.use {
				writeCompactVec(startPoint)
				writeCompactVec(endPoint)
				writeString(soundCategory.getName())
				writeByte((soundVolume * 10F).floorToInt().coerceIn(0, 250))
				writeByte(extendedRange.floorToInt().coerceIn(0, 255))
			}
		}
		
		@JvmStatic
		val FX_TELEPORT = object : IFXHandler{
			override fun handle(buffer: ByteBuf, world: World, rand: Random) = buffer.use {
				val player = HEE.proxy.getClientSidePlayer() ?: return
				val playerPos = player.posVec
				
				val startPoint = readCompactVec()
				val endPoint = readCompactVec()
				
				val soundCategory = SoundCategory.getByName(readString())
				val soundVolume = readByte() / 10F
				val soundRange = 16F + readByte()
				
				val soundPosition = if (playerPos.squareDistanceTo(startPoint) < playerPos.squareDistanceTo(endPoint))
					startPoint
				else
					endPoint
				
				val soundDistance = playerPos.distanceTo(soundPosition)
				
				if (soundDistance < RANGE_FOR_MINIMUM_VOLUME){
					SoundEvents.ENTITY_ENDERMEN_TELEPORT.playClient(soundPosition, soundCategory, volume = soundVolume)
				}
				else if (soundDistance < soundRange){
					val closerPosition = playerPos.add(soundPosition.subtract(playerPos).normalize().scale(RANGE_FOR_MINIMUM_VOLUME))
					SoundEvents.ENTITY_ENDERMEN_TELEPORT.playClient(closerPosition, soundCategory, volume = soundVolume)
				}
				
				ParticleSpawnerCustom(
					type = ParticleTeleport,
					pos = PARTICLE_POS,
					mot = PARTICLE_MOT,
					maxRange = 16.0 + soundRange,
					hideOnMinimalSetting = false
				).spawn(Line(startPoint, endPoint, 0.5), rand)
			}
		}
		
		fun sendTeleportFX(world: World, startPoint: Vec3d, endPoint: Vec3d, soundCategory: SoundCategory, soundVolume: Float, extraRange: Float = 0F){
			val middlePoint = startPoint.offsetTowards(endPoint, 0.5)
			val traveledDistance = startPoint.distanceTo(endPoint)
			
			PacketClientFX(FX_TELEPORT, FxTeleportData(startPoint, endPoint, soundCategory, soundVolume, extraRange)).sendToAllAround(world, middlePoint, (traveledDistance * 0.5) + 32F + extraRange)
		}
	}
	
	fun toBlock(entity: EntityLivingBase, position: BlockPos, soundCategory: SoundCategory): Boolean{
		return toLocation(entity, position.center.add(0.0, -0.49, 0.0), soundCategory)
	}
	
	fun toLocation(entity: EntityLivingBase, position: Vec3d, soundCategory: SoundCategory): Boolean{
		val event = EnderTeleportEvent(entity, position.x, position.y, position.z, damageDealt)
		
		if (MinecraftForge.EVENT_BUS.post(event)){
			return false
		}
		
		if (entity.isRiding){
			entity.dismountRidingEntity()
		}
		
		if (entity.isPlayerSleeping && entity is EntityPlayer){
			entity.wakeUpPlayer(true, true, false)
		}
		
		val prevPos = entity.posVec
		entity.setPositionAndUpdate(event.targetX, event.targetY, event.targetZ)
		sendTeleportFX(entity.world, prevPos, entity.posVec, soundCategory, 1F, extendedEffectRange)
		
		if (resetFall){
			entity.fallDistance = 0F
		}
		
		if (resetPathfinding && entity is EntityCreature){
			entity.navigator.clearPath()
		}
		
		if (causedInstability > 0u){
			Instability.get(entity.world).triggerAction(causedInstability, Pos(position))
		}
		
		return true
	}
}
