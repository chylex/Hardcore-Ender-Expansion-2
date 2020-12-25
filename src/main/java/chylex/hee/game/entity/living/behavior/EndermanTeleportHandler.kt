package chylex.hee.game.entity.living.behavior

import chylex.hee.HEE
import chylex.hee.game.entity.Teleporter
import chylex.hee.game.entity.Teleporter.Companion.FxTeleportData
import chylex.hee.game.entity.Teleporter.FxRange.Extended
import chylex.hee.game.entity.Teleporter.FxRange.Silent
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.game.entity.lookPosVec
import chylex.hee.game.entity.motionX
import chylex.hee.game.entity.motionZ
import chylex.hee.game.entity.posVec
import chylex.hee.game.particle.ParticleFadingSpot
import chylex.hee.game.particle.ParticleTeleport
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.Constant
import chylex.hee.game.particle.spawner.properties.IOffset.Gaussian
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Line
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.potion.makeEffect
import chylex.hee.game.world.Pos
import chylex.hee.game.world.blocksMovement
import chylex.hee.game.world.isLoaded
import chylex.hee.game.world.offsetUntilExcept
import chylex.hee.game.world.playClient
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.fx.FxEntityData
import chylex.hee.network.fx.FxEntityHandler
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.math.Vec
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.addY
import chylex.hee.system.math.component1
import chylex.hee.system.math.component2
import chylex.hee.system.math.component3
import chylex.hee.system.math.directionTowards
import chylex.hee.system.math.floorToInt
import chylex.hee.system.math.toRadians
import chylex.hee.system.math.withY
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.Potions
import chylex.hee.system.migration.Sounds
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextVector2
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.use
import net.minecraft.entity.Entity
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.common.util.INBTSerializable
import java.util.Random
import java.util.UUID
import kotlin.math.min
import kotlin.math.sqrt

class EndermanTeleportHandler(private val enderman: EntityMobAbstractEnderman) : INBTSerializable<TagCompound> {
	companion object {
		private const val DEFAULT_RESTORE_Y = -256.0
		
		private const val COOLDOWN_TAG = "Cooldown"
		private const val DELAY_TICKS_TAG = "DelayTicks"
		
		private val TELEPORTER_GENERAL = Teleporter(resetFall = true, causedInstability = 15u, effectRange = Extended(16F))
		private val TELEPORTER_SILENT = Teleporter(resetFall = true, causedInstability = 15u, effectRange = Silent)
		private val TELEPORTER_WEAK = Teleporter(resetFall = false, resetPathfinding = false)
		
		private fun PARTICLE_TELEPORT_FAIL(target: Entity) = ParticleSpawnerCustom(
			type = ParticleFadingSpot,
			data = ParticleFadingSpot.Data(color = RGB(40u), lifespan = 20..32, scale = (0.08F)..(0.12F)),
			pos = InBox(target, 0.33F),
			mot = Constant(0.012F, UP) + Gaussian(0.008F)
		)
		
		private val PARTICLE_TELEPORT_OUT_OF_WORLD = ParticleSpawnerCustom(
			type = ParticleTeleport,
			pos = InBox(0.1F, 0.25F, 0.1F),
			mot = InBox(0.05F),
			maxRange = 64.0,
			hideOnMinimalSetting = false
		)
		
		val FX_TELEPORT_FAIL = object : FxEntityHandler() {
			override fun handle(entity: Entity, rand: Random) {
				PARTICLE_TELEPORT_FAIL(entity).spawn(Point(entity, heightMp = 0.5F, amount = 55), rand)
				ModSounds.MOB_ENDERMAN_TELEPORT_FAIL.playClient(entity.posVec, SoundCategory.HOSTILE, volume = 2.5F)
			}
		}
		
		val FX_TELEPORT_OUT_OF_WORLD = object : FxEntityHandler() {
			override fun handle(entity: Entity, rand: Random) {
				val player = HEE.proxy.getClientSidePlayer() ?: return
				
				val startPoint = entity.posVec
				val endPoint = startPoint.addY(256.0)
				
				val lookPos = player.lookPosVec
				val soundPoint = startPoint.withY((startPoint.y + lookPos.y) * 0.5)
				val volume = 1F - sqrt(lookPos.distanceTo(soundPoint) / 104F).toFloat()
				
				PARTICLE_TELEPORT_OUT_OF_WORLD.spawn(Line(startPoint, endPoint, 0.5), rand)
				
				repeat(2) {
					ModSounds.MOB_ENDERMAN_TELEPORT_OUT.playClient(soundPoint, SoundCategory.HOSTILE, if (it == 0) volume else volume * 0.75F)
				}
			}
		}
		
		private fun sendDelayedTeleportFX(entity: Entity) {
			val point = entity.posVec.addY(entity.height.toDouble())
			FxTeleportData(point, point, entity.width, entity.height, Sounds.ENTITY_ENDERMAN_TELEPORT, entity.soundCategory, soundVolume = 0.8F).send(entity.world)
		}
	}
	
	val preventDespawn
		get() = tpDelayTicks > 0
	
	private val world = enderman.world
	private val rand = enderman.rng
	
	private var tpCooldown = 0
	private var failCooldown: Byte = 0
	
	private var tpDelayTicks = 0
	private var tpDelayedReappearing = false
	private var tpDelayedCallback: (() -> Boolean)? = null
	private var tpDelayRestoreY = DEFAULT_RESTORE_Y
	
	private var lastDodged: UUID? = null
	
	fun update() {
		if (tpCooldown > 0) {
			--tpCooldown
		}
		
		if (failCooldown > 0) {
			--failCooldown
		}
		
		if (tpDelayTicks > 0 && --tpDelayTicks == 0) {
			enderman.setNoAI(false)
			enderman.setPositionAndUpdate(enderman.posX, tpDelayRestoreY, enderman.posZ)
			
			tpDelayedReappearing = true
			
			if (tpDelayedCallback?.invoke() != true) {
				enderman.remove()
			}
			
			tpDelayedReappearing = false
			tpDelayedCallback = null
			tpDelayRestoreY = DEFAULT_RESTORE_Y
		}
	}
	
	// Helpers
	
	fun checkCooldownSilent(): Boolean {
		return tpCooldown == 0 && tpDelayTicks == 0
	}
	
	private fun checkCooldown(): Boolean {
		if (tpDelayTicks > 0) {
			return false
		}
		
		if (tpCooldown == 0) {
			return true
		}
		
		if (failCooldown == 0.toByte()) {
			PacketClientFX(FX_TELEPORT_FAIL, FxEntityData(enderman)).sendToAllAround(enderman, 16.0)
			failCooldown = rand.nextInt(26, 34).toByte()
		}
		
		return false
	}
	
	private fun checkPositionSuitable(target: Vec3d): Boolean {
		val pos = Pos(target)
		
		if (!pos.isLoaded(world) || !pos.down().blocksMovement(world)) {
			return false
		}
		
		val (x, y, z) = target
		val hw = enderman.width * 0.5F
		
		val aabb = AxisAlignedBB(
			x - hw,
			y,
			z - hw,
			x + hw,
			y + enderman.height,
			z + hw
		)
		
		return enderman.canTeleportTo(aabb)
	}
	
	private fun teleportCheckLocation(teleporter: Teleporter, target: Vec3d): Boolean {
		return checkPositionSuitable(target) && teleporter.toLocation(enderman, target)
	}
	
	private fun getPositionInsideBlock(target: BlockPos): Vec3d {
		val xzMaxOffset = 0.5F - (enderman.width * 0.5F)
		
		return Vec(
			target.x + 0.5 + rand.nextFloat(-xzMaxOffset, xzMaxOffset),
			target.y.toDouble(),
			target.z + 0.5 + rand.nextFloat(-xzMaxOffset, xzMaxOffset)
		)
	}
	
	private fun findTop(target: BlockPos, maxDecreaseY: Int): BlockPos? {
		return target.offsetUntilExcept(DOWN, 0..maxDecreaseY) { it.blocksMovement(world) }
	}
	
	// General teleports
	
	fun teleportTo(target: Vec3d): Boolean {
		if (!checkCooldown()) {
			return false
		}
		
		if (tpDelayedReappearing) {
			if (teleportCheckLocation(TELEPORTER_SILENT, target)) {
				sendDelayedTeleportFX(enderman)
				tpDelayedReappearing = false
				
				tpCooldown = enderman.teleportCooldown
				return true
			}
			
			return false
		}
		
		if (teleportCheckLocation(TELEPORTER_GENERAL, target)) {
			tpCooldown = enderman.teleportCooldown
			return true
		}
		
		return false
	}
	
	fun teleportTo(target: BlockPos): Boolean {
		return teleportTo(getPositionInsideBlock(target))
	}
	
	fun teleportAround(target: Entity, angleRange: ClosedFloatingPointRange<Float>, distanceRange: ClosedFloatingPointRange<Double>): Boolean {
		if (!checkCooldown()) {
			return false
		}
		
		val targetVec = target.posVec
		
		for(attempt in 1..50) {
			val dir = Vec3.fromYaw(target.rotationYaw + rand.nextFloat(angleRange))
			val distance = rand.nextFloat(distanceRange)
			
			val offsetVec = targetVec.add(dir.scale(distance))
			val targetPos = findTop(Pos(offsetVec).add(0, rand.nextInt(-4, 8), 0), maxDecreaseY = 4)
			
			if (targetPos != null && teleportTo(targetPos)) {
				return true
			}
		}
		
		return false
	}
	
	fun teleportRandom(distanceRange: ClosedFloatingPointRange<Double>): Boolean {
		if (!checkCooldown()) {
			return false
		}
		
		val endermanPos = Pos(enderman)
		
		for(attempt in 1..25) {
			val (x, y, z) = rand.nextVector2(xz = rand.nextFloat(distanceRange), y = rand.nextFloat(-24.0, 48.0))
			val targetPos = findTop(endermanPos.add(x, y, z), maxDecreaseY = 24)
			
			if (targetPos != null && teleportTo(targetPos)) {
				return true
			}
		}
		
		return false
	}
	
	// Special teleports
	
	fun teleportDelayed(delayTicks: Int, callback: () -> Boolean): Boolean {
		if (enderman.isAIDisabled || !checkCooldown()) {
			return false
		}
		
		sendDelayedTeleportFX(enderman)
		
		tpDelayTicks = delayTicks
		tpDelayedCallback = callback
		tpDelayRestoreY = enderman.posY
		
		enderman.setNoAI(true)
		enderman.addPotionEffect(Potions.INVISIBILITY.makeEffect(delayTicks, enderman.getActivePotionEffect(Potions.INVISIBILITY)?.amplifier ?: 0, isAmbient = true, showParticles = false))
		enderman.setPositionAndUpdate(enderman.posX, 4095.0, enderman.posZ)
		
		return true
	}
	
	fun teleportDodge(dodge: Entity): Boolean {
		val uuid = dodge.uniqueID
		
		if (uuid == lastDodged) {
			return false
		}
		
		val endermanPos = enderman.posVec
		val dodgePos = dodge.posVec
		
		val perpendicularVec = Vec3.xz(-dodge.motionZ, dodge.motionX).normalize()
		val dodgeDist = (enderman.width * 0.75) + (dodge.width * 0.75) + 0.25
		
		val dir = maxOf(-1, 1, compareBy {
			endermanPos.add(perpendicularVec.scale(it * dodgeDist)).squareDistanceTo(dodgePos) + (rand.nextDouble() * 0.1)
		})
		
		for(attempt in 1..3) {
			val basePos = endermanPos
				.add(perpendicularVec.scale(dir * rand.nextFloat(dodgeDist, dodgeDist + 0.4)))
				.add(rand.nextVector2(xz = rand.nextFloat(0.0, 0.15), y = 1.5))
			
			for(offset in 0..2) {
				val targetPos = Vec(basePos.x, basePos.y.floorToInt() - offset + 0.01, basePos.z)
				
				if (teleportCheckLocation(TELEPORTER_WEAK, targetPos)) {
					lastDodged = uuid
					return true
				}
			}
		}
		
		return false
	}
	
	fun teleportTowards(target: Entity, angleRange: ClosedFloatingPointRange<Float>, distanceRange: ClosedFloatingPointRange<Double>): Boolean {
		if (!checkCooldown()) {
			return false
		}
		
		val endermanVec = enderman.posVec
		val diffVec = endermanVec.directionTowards(target.posVec)
		
		for(attempt in 1..50) {
			val dir = diffVec.rotateYaw(rand.nextFloat(angleRange).toRadians())
			val distance = rand.nextFloat(distanceRange)
			
			val offsetVec = endermanVec.add(dir.scale(distance))
			val targetPos = findTop(Pos(offsetVec).add(0, rand.nextInt(-4, 8), 0), maxDecreaseY = 4)
			
			if (targetPos != null && teleportCheckLocation(TELEPORTER_WEAK, getPositionInsideBlock(targetPos))) {
				return true
			}
		}
		
		return false
	}
	
	fun teleportOutOfWorld(force: Boolean = false): Boolean {
		if (!force && !checkCooldown()) {
			return false
		}
		
		PacketClientFX(FX_TELEPORT_OUT_OF_WORLD, FxEntityData(enderman)).sendToAllAround(enderman, 96.0)
		enderman.remove()
		return true
	}
	
	// Serialization
	
	override fun serializeNBT() = TagCompound().apply {
		putShort(COOLDOWN_TAG, tpCooldown.toShort())
		putShort(DELAY_TICKS_TAG, tpDelayTicks.toShort())
	}
	
	override fun deserializeNBT(nbt: TagCompound) = nbt.use {
		tpCooldown = getShort(COOLDOWN_TAG).toInt()
		tpDelayTicks = min(1, getShort(DELAY_TICKS_TAG).toInt())
	}
}
