package chylex.hee.game.entity

import chylex.hee.client.util.MC
import chylex.hee.game.entity.Teleporter.FxRange.Extended
import chylex.hee.game.entity.Teleporter.FxRange.Normal
import chylex.hee.game.entity.Teleporter.FxRange.Silent
import chylex.hee.game.entity.damage.Damage
import chylex.hee.game.entity.damage.IDamageDealer.Companion.TITLE_FALL
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.MAGIC_TYPE
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.fx.util.playClient
import chylex.hee.game.mechanics.instability.Instability
import chylex.hee.game.particle.ParticleTeleport
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Line
import chylex.hee.game.world.util.blocksMovement
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.client.PacketClientMoveYourAss
import chylex.hee.network.client.PacketClientRotateInstantly
import chylex.hee.network.client.PacketClientTeleportInstantly
import chylex.hee.util.buffer.readCompactVec
import chylex.hee.util.buffer.readEnum
import chylex.hee.util.buffer.writeCompactVec
import chylex.hee.util.buffer.writeEnum
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Vec
import chylex.hee.util.math.addY
import chylex.hee.util.math.center
import chylex.hee.util.math.directionTowards
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.lerpTowards
import chylex.hee.util.math.subtractY
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextVector
import net.minecraft.entity.CreatureEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.monster.EndermanEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
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
	private val effectRange: FxRange = Normal,
) {
	companion object {
		private val DAMAGE = Damage(MAGIC_TYPE)
		
		private const val EXTENDED_MINIMUM_VOLUME = 0.1F
		private const val RANGE_FOR_MINIMUM_VOLUME = 16.0 - (16.0 * EXTENDED_MINIMUM_VOLUME)
		
		private val PARTICLE_MOT = InBox(0.035F)
		
		class FxTeleportData(
			private val startPoint: Vector3d,
			private val endPoint: Vector3d,
			private val width: Float,
			private val height: Float,
			private val soundEvent: SoundEvent,
			private val soundCategory: SoundCategory,
			private val soundVolume: Float,
			private val extraRange: Float = 0F,
		) : IFxData {
			override fun write(buffer: PacketBuffer) {
				buffer.writeCompactVec(startPoint)
				buffer.writeCompactVec(endPoint)
				buffer.writeByte((width * 10F).floorToInt().coerceIn(0, 100))
				buffer.writeByte((height * 10F).floorToInt().coerceIn(0, 100))
				
				buffer.writeRegistryId(soundEvent)
				buffer.writeEnum(soundCategory)
				buffer.writeByte((soundVolume * 10F).floorToInt().coerceIn(0, 250))
				buffer.writeByte(extraRange.floorToInt().coerceIn(0, 255))
			}
			
			fun send(world: World) {
				val middlePoint = startPoint.lerpTowards(endPoint, 0.5)
				val traveledDistance = startPoint.distanceTo(endPoint)
				
				PacketClientFX(FX_TELEPORT, this).sendToAllAround(world, middlePoint, (traveledDistance * 0.5) + 32F + extraRange)
			}
		}
		
		val FX_TELEPORT = object : IFxHandler<FxTeleportData> {
			override fun handle(buffer: PacketBuffer, world: World, rand: Random) {
				val player = MC.player ?: return
				val playerPos = player.posVec
				
				val startPoint = buffer.readCompactVec()
				val endPoint = buffer.readCompactVec()
				
				val halfWidth = (buffer.readByte() / 10F) * 0.5F
				val halfHeight = (buffer.readByte() / 10F) * 0.5F
				
				val soundEvent = buffer.readRegistryIdSafe(SoundEvent::class.java)
				val soundCategory = buffer.readEnum() ?: SoundCategory.NEUTRAL
				val soundVolume = buffer.readByte() / 10F
				val soundRange = 16F + buffer.readByte()
				
				val soundPosition = if (playerPos.squareDistanceTo(startPoint) < playerPos.squareDistanceTo(endPoint))
					startPoint
				else
					endPoint
				
				val soundDistance = playerPos.distanceTo(soundPosition)
				
				if (soundDistance < RANGE_FOR_MINIMUM_VOLUME) {
					soundEvent.playClient(soundPosition, soundCategory, volume = soundVolume)
				}
				else if (soundDistance < soundRange) {
					val closerPosition = playerPos.add(playerPos.directionTowards(soundPosition).scale(RANGE_FOR_MINIMUM_VOLUME))
					soundEvent.playClient(closerPosition, soundCategory, volume = soundVolume)
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
	
	sealed class FxRange {
		object Silent : FxRange()
		object Normal : FxRange()
		class Extended(val extraRange: Float) : FxRange()
	}
	
	// Target
	
	fun toLocation(entity: LivingEntity, position: Vector3d, soundCategory: SoundCategory = entity.soundCategory): Boolean {
		val event = EnderTeleportEvent(entity, position.x, position.y, position.z, damageDealt)
		
		if (postEvent && MinecraftForge.EVENT_BUS.post(event)) {
			return false
		}
		
		if (entity.isPassenger) {
			entity.stopRiding()
			
			if (entity is ServerPlayerEntity) {
				PacketClientMoveYourAss(position).sendToPlayer(entity) // dismounting client ignores any attempts at teleporting
			}
		}
		
		if (entity.isSleeping && entity is PlayerEntity) {
			entity.stopSleepInBed(true, true)
		}
		
		val world = entity.world
		val prevPos = entity.posVec
		val newPos = Vec(event.targetX, event.targetY, event.targetZ)
		
		PacketClientTeleportInstantly(entity, newPos).sendToTracking(entity)
		entity.setPositionAndUpdate(newPos.x, newPos.y, newPos.z)
		
		if (effectRange != Silent) {
			val extraRange = when (effectRange) {
				is Extended -> effectRange.extraRange
				else        -> 0F
			}
			
			val soundEvent = when (entity) {
				is PlayerEntity   -> ModSounds.ENTITY_PLAYER_TELEPORT
				is EndermanEntity -> SoundEvents.ENTITY_ENDERMAN_TELEPORT
				else              -> ModSounds.ENTITY_GENERIC_TELEPORT
			}
			
			val halfHeight = entity.height * 0.5
			FxTeleportData(prevPos.addY(halfHeight), newPos.addY(halfHeight), entity.width, entity.height, soundEvent, soundCategory, 1F, extraRange).send(world)
		}
		
		if (resetFall) {
			entity.fallDistance = 0F
		}
		
		if (damageDealt > 0F) {
			DAMAGE.dealTo(damageDealt, entity, damageTitle)
		}
		
		if (entity is CreatureEntity) {
			if (resetPathfinding) {
				entity.navigator.clearPath()
			}
			
			if (entity.lookController.isLooking) { // must be called inside updateAITasks to apply
				with(entity.lookController) {
					setLookPosition(lookPosX, lookPosY, lookPosZ, 360F, 360F)
					tick()
				}
				
				val newYaw = entity.rotationYawHead
				
				entity.rotationYaw = newYaw
				entity.setRenderYawOffset(newYaw)
				
				PacketClientRotateInstantly(entity, newYaw, entity.rotationPitch).sendToTracking(entity)
			}
		}
		
		if (causedInstability > 0u) {
			Instability.get(world).triggerAction(causedInstability, Pos(position))
		}
		
		return true
	}
	
	fun nearLocation(entity: LivingEntity, rand: Random, position: Vector3d, distance: ClosedFloatingPointRange<Double>, attempts: Int, soundCategory: SoundCategory = entity.soundCategory): Boolean {
		val world = entity.world
		val originalPos = entity.posVec
		val originalBox = entity.boundingBox
		
		repeat(attempts) {
			val randomPos = position.add(rand.nextVector(rand.nextFloat(distance)))
			val newPos = Vec(randomPos.x, randomPos.y.floorToInt() + 0.01, randomPos.z)
			
			if (Pos(newPos).down().blocksMovement(world) && world.hasNoCollisions(entity, originalBox.offset(newPos.subtract(originalPos)))) {
				return toLocation(entity, newPos, soundCategory)
			}
		}
		
		return false
	}
	
	fun nearLocation(entity: LivingEntity, position: Vector3d, distance: ClosedFloatingPointRange<Double>, attempts: Int, soundCategory: SoundCategory = entity.soundCategory): Boolean {
		return nearLocation(entity, entity.rng, position, distance, attempts, soundCategory)
	}
	
	fun toBlock(entity: LivingEntity, position: BlockPos, soundCategory: SoundCategory = entity.soundCategory): Boolean {
		return toLocation(entity, position.center.subtractY(0.49), soundCategory)
	}
}
