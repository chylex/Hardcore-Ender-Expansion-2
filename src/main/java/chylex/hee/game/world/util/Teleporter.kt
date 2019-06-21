package chylex.hee.game.world.util
import chylex.hee.HEE
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.Damage.Companion.TITLE_FALL
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.MAGIC_TYPE
import chylex.hee.game.mechanics.instability.Instability
import chylex.hee.game.particle.ParticleTeleport
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Line
import chylex.hee.game.world.util.Teleporter.FxRange.Extended
import chylex.hee.game.world.util.Teleporter.FxRange.Normal
import chylex.hee.game.world.util.Teleporter.FxRange.Silent
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.client.PacketClientMoveYourAss
import chylex.hee.network.client.PacketClientRotateInstantly
import chylex.hee.network.client.PacketClientTeleportInstantly
import chylex.hee.system.util.Pos
import chylex.hee.system.util.addY
import chylex.hee.system.util.blocksMovement
import chylex.hee.system.util.center
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextVector
import chylex.hee.system.util.offsetTowards
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.readCompactVec
import chylex.hee.system.util.readString
import chylex.hee.system.util.subtractY
import chylex.hee.system.util.use
import chylex.hee.system.util.writeCompactVec
import chylex.hee.system.util.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.SoundEvents
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.EnderTeleportEvent
import java.util.Random

class Teleporter(
	private val postEvent: Boolean = true,
	private val resetFall: Boolean = true,
	private val resetPathfinding: Boolean = true,
	private val damageDealt: Float = 0F,
	private val damageTitle: String = TITLE_FALL,
	private val causedInstability: UShort = 0u,
	private val effectRange: FxRange = Normal
){
	companion object{
		private val DAMAGE = Damage(MAGIC_TYPE)
		
		private const val EXTENDED_MINIMUM_VOLUME = 0.1F
		private const val RANGE_FOR_MINIMUM_VOLUME = 16.0 - (16.0 * EXTENDED_MINIMUM_VOLUME)
		
		private val PARTICLE_MOT = InBox(0.035F)
		
		class FxTeleportData(
			private val startPoint: Vec3d,
			private val endPoint: Vec3d,
			private val width: Float,
			private val height: Float,
			private val soundCategory: SoundCategory,
			private val soundVolume: Float,
			private val extraRange: Float = 0F
		) : IFxData{
			override fun write(buffer: ByteBuf) = buffer.use {
				writeCompactVec(startPoint)
				writeCompactVec(endPoint)
				writeByte((width * 10F).floorToInt().coerceIn(0, 100))
				writeByte((height * 10F).floorToInt().coerceIn(0, 100))
				writeString(soundCategory.getName())
				writeByte((soundVolume * 10F).floorToInt().coerceIn(0, 250))
				writeByte(extraRange.floorToInt().coerceIn(0, 255))
			}
			
			fun send(world: World){
				val middlePoint = startPoint.offsetTowards(endPoint, 0.5)
				val traveledDistance = startPoint.distanceTo(endPoint)
				
				PacketClientFX(FX_TELEPORT, this).sendToAllAround(world, middlePoint, (traveledDistance * 0.5) + 32F + extraRange)
			}
		}
		
		@JvmStatic
		val FX_TELEPORT = object : IFxHandler<FxTeleportData>{
			override fun handle(buffer: ByteBuf, world: World, rand: Random) = buffer.use {
				val player = HEE.proxy.getClientSidePlayer() ?: return
				val playerPos = player.posVec
				
				val startPoint = readCompactVec()
				val endPoint = readCompactVec()
				
				val halfWidth = (readByte() / 10F) * 0.5F
				val halfHeight = (readByte() / 10F) * 0.5F
				
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
					val closerPosition = playerPos.add(playerPos.directionTowards(soundPosition).scale(RANGE_FOR_MINIMUM_VOLUME))
					SoundEvents.ENTITY_ENDERMEN_TELEPORT.playClient(closerPosition, soundCategory, volume = soundVolume)
				}
				
				ParticleSpawnerCustom(
					type = ParticleTeleport,
					pos = InBox(halfWidth, halfHeight, halfWidth),
					mot = PARTICLE_MOT,
					maxRange = 16.0 + soundRange,
					hideOnMinimalSetting = false
				).spawn(Line(startPoint, endPoint, 0.5), rand)
			}
		}
	}
	
	sealed class FxRange{
		object Silent : FxRange()
		object Normal : FxRange()
		class Extended(val extraRange: Float): FxRange()
	}
	
	// Target
	
	fun toLocation(entity: EntityLivingBase, position: Vec3d, soundCategory: SoundCategory = entity.soundCategory): Boolean{
		val event = EnderTeleportEvent(entity, position.x, position.y, position.z, damageDealt)
		
		if (postEvent && MinecraftForge.EVENT_BUS.post(event)){
			return false
		}
		
		if (entity.isRiding){
			entity.dismountRidingEntity()
			
			if (entity is EntityPlayerMP){
				PacketClientMoveYourAss(position).sendToPlayer(entity) // dismounting client ignores any attempts at teleporting
			}
		}
		
		if (entity.isPlayerSleeping && entity is EntityPlayer){
			entity.wakeUpPlayer(true, true, false)
		}
		
		val world = entity.world
		val prevPos = entity.posVec
		val newPos = Vec3d(event.targetX, event.targetY, event.targetZ)
		
		PacketClientTeleportInstantly(entity, newPos).sendToTracking(entity)
		entity.setPositionAndUpdate(newPos.x, newPos.y, newPos.z)
		
		if (effectRange != Silent){
			val extraRange = when(effectRange){
				is Extended -> effectRange.extraRange
				else -> 0F
			}
			
			val halfHeight = entity.height * 0.5
			FxTeleportData(prevPos.addY(halfHeight), newPos.addY(halfHeight), entity.width, entity.height, soundCategory, 1F, extraRange).send(world)
		}
		
		if (resetFall){
			entity.fallDistance = 0F
		}
		
		if (damageDealt > 0F){
			DAMAGE.dealTo(damageDealt, entity, damageTitle)
		}
		
		if (entity is EntityCreature){
			if (resetPathfinding){
				entity.navigator.clearPath()
			}
			
			if (entity.lookHelper.isLooking){
				with(entity.lookHelper){
					setLookPosition(lookPosX, lookPosY, lookPosZ, 360F, 360F)
					onUpdateLook()
				}
				
				val newYaw = entity.rotationYawHead
				
				entity.rotationYaw = newYaw
				entity.setRenderYawOffset(newYaw)
				
				PacketClientRotateInstantly(entity, newYaw, entity.rotationPitch).sendToTracking(entity)
			}
		}
		
		if (causedInstability > 0u){
			Instability.get(world).triggerAction(causedInstability, Pos(position))
		}
		
		return true
	}
	
	fun nearLocation(entity: EntityLivingBase, rand: Random, position: Vec3d, distance: ClosedFloatingPointRange<Double>, attempts: Int, soundCategory: SoundCategory = entity.soundCategory): Boolean{
		val world = entity.world
		val originalPos = entity.posVec
		val originalBox = entity.entityBoundingBox
		
		repeat(attempts){
			val randomPos = position.add(rand.nextVector(rand.nextFloat(distance.start, distance.endInclusive)))
			val newPos = Vec3d(randomPos.x, randomPos.y.floorToInt() + 0.01, randomPos.z)
			
			if (Pos(newPos).down().blocksMovement(world) && world.getCollisionBoxes(entity, originalBox.offset(newPos.subtract(originalPos))).isEmpty()){
				return toLocation(entity, newPos, soundCategory)
			}
		}
		
		return false
	}
	
	fun nearLocation(entity: EntityLivingBase, position: Vec3d, distance: ClosedFloatingPointRange<Double>, attempts: Int, soundCategory: SoundCategory = entity.soundCategory): Boolean{
		return nearLocation(entity, entity.rng, position, distance, attempts, soundCategory)
	}
	
	fun toBlock(entity: EntityLivingBase, position: BlockPos, soundCategory: SoundCategory = entity.soundCategory): Boolean{
		return toLocation(entity, position.center.subtractY(0.49), soundCategory)
	}
}
