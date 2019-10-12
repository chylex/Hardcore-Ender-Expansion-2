package chylex.hee.game.entity.living.behavior
import chylex.hee.game.entity.living.EntityMobAbstractEnderman
import chylex.hee.game.fx.FxEntityData
import chylex.hee.game.fx.FxEntityHandler
import chylex.hee.game.particle.ParticleFadingSpot
import chylex.hee.game.particle.ParticleTeleport
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.Gaussian
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Line
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.game.world.util.Teleporter
import chylex.hee.game.world.util.Teleporter.Companion.FxTeleportData
import chylex.hee.game.world.util.Teleporter.FxRange.Extended
import chylex.hee.game.world.util.Teleporter.FxRange.Silent
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.vanilla.Potions
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.Vec3
import chylex.hee.system.util.addY
import chylex.hee.system.util.blocksMovement
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.isLoaded
import chylex.hee.system.util.makeEffect
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextVector2
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import net.minecraft.entity.Entity
import net.minecraft.util.ITickable
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.common.util.INBTSerializable
import java.util.Random
import java.util.UUID
import kotlin.math.min

class EndermanTeleportHandler(private val enderman: EntityMobAbstractEnderman) : ITickable, INBTSerializable<TagCompound>{
	companion object{
		private const val DEFAULT_RESTORE_Y = -256.0
		
		private const val COOLDOWN_TAG = "Cooldown"
		private const val DELAY_TICKS_TAG = "DelayTicks"
		
		private val TELEPORTER_GENERAL = Teleporter(resetFall = true, causedInstability = 15u, effectRange = Extended(16F))
		private val TELEPORTER_SILENT = Teleporter(resetFall = true, causedInstability = 15u, effectRange = Silent)
		private val TELEPORTER_DODGE = Teleporter(resetFall = false, resetPathfinding = false)
		
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
		
		val FX_TELEPORT_FAIL = object : FxEntityHandler(){
			override fun handle(entity: Entity, rand: Random){
				PARTICLE_TELEPORT_FAIL(entity).spawn(Point(entity, heightMp = 0.5F, amount = 55), rand)
				Sounds.ENTITY_ENDERMEN_TELEPORT.playClient(entity.posVec, SoundCategory.HOSTILE, volume = 0.75F) // TODO custom sound
			}
		}
		
		val FX_TELEPORT_OUT_OF_WORLD = object : FxEntityHandler(){
			override fun handle(entity: Entity, rand: Random){
				val startPoint = entity.posVec
				val endPoint = startPoint.addY(256.0)
				
				PARTICLE_TELEPORT_OUT_OF_WORLD.spawn(Line(startPoint, endPoint, 0.5), rand)
				Sounds.ENTITY_ENDERMEN_TELEPORT.playClient(startPoint, SoundCategory.HOSTILE, volume = 2F) // TODO custom sound
			}
		}
		
		private fun sendDelayedTeleportFX(entity: Entity){
			val point = entity.posVec.addY(entity.height.toDouble())
			FxTeleportData(point, point, entity.width, entity.height, entity.soundCategory, soundVolume = 0.8F).send(entity.world)
		}
	}
	
	private val world = enderman.world
	private val rand = enderman.rng
	
	private var tpCooldown = 0
	private var failCooldown: Byte = 0
	
	private var tpDelayTicks = 0
	private var tpDelayedReappearing = false
	private var tpDelayedCallback: (() -> Boolean)? = null
	private var tpDelayRestoreY = DEFAULT_RESTORE_Y
	
	private var lastDodged: UUID? = null
	
	override fun update(){
		if (tpCooldown > 0){
			--tpCooldown
		}
		
		if (failCooldown > 0){
			--failCooldown
		}
		
		if (tpDelayTicks > 0 && --tpDelayTicks == 0){
			enderman.setNoAI(false)
			enderman.setPositionAndUpdate(enderman.posX, tpDelayRestoreY, enderman.posZ)
			
			tpDelayedReappearing = true
			
			if (tpDelayedCallback?.invoke() != true){
				enderman.setDead()
			}
			
			tpDelayedReappearing = false
			tpDelayedCallback = null
			tpDelayRestoreY = DEFAULT_RESTORE_Y
		}
	}
	
	// Helpers
	
	fun checkCooldownSilent(): Boolean{
		return tpCooldown == 0 && tpDelayTicks == 0
	}
	
	private fun checkCooldown(): Boolean{
		if (tpDelayTicks > 0){
			return false
		}
		
		if (tpCooldown == 0){
			return true
		}
		
		if (failCooldown == 0.toByte()){
			PacketClientFX(FX_TELEPORT_FAIL, FxEntityData(enderman)).sendToAllAround(enderman, 16.0)
			failCooldown = rand.nextInt(26, 34).toByte()
		}
		
		return false
	}
	
	private fun checkPositionSuitable(target: Vec3d): Boolean{
		val pos = Pos(target)
		
		if (!pos.isLoaded(world) || !pos.down().blocksMovement(world)){
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
		
		return world.getCollisionBoxes(null, aabb).isEmpty() && !world.containsAnyLiquid(aabb)
	}
	
	private fun teleportCheckLocation(teleporter: Teleporter, target: Vec3d): Boolean{
		return checkPositionSuitable(target) && teleporter.toLocation(enderman, target)
	}
	
	// General teleports
	
	fun teleportTo(target: Vec3d): Boolean{
		if (!checkCooldown()){
			return false
		}
		
		if (tpDelayedReappearing){
			if (teleportCheckLocation(TELEPORTER_SILENT, target)){
				sendDelayedTeleportFX(enderman)
				tpDelayedReappearing = false
				
				tpCooldown = enderman.teleportCooldown
				return true
			}
			
			return false
		}
		
		if (teleportCheckLocation(TELEPORTER_GENERAL, target)){
			tpCooldown = enderman.teleportCooldown
			return true
		}
		
		return false
	}
	
	fun teleportTo(target: BlockPos): Boolean{
		val xzMaxOffset = 0.5F - (enderman.width * 0.5F)
		
		return teleportTo(Vec3d(
			target.x + 0.5 + rand.nextFloat(-xzMaxOffset, xzMaxOffset),
			target.y.toDouble(),
			target.z + 0.5 + rand.nextFloat(-xzMaxOffset, xzMaxOffset)
		))
	}
	
	fun teleportAround(target: Entity, angleRange: ClosedFloatingPointRange<Float>, distanceRange: ClosedFloatingPointRange<Double>): Boolean{
		if (!checkCooldown()){
			return false
		}
		
		val targetVec = target.posVec
		
		for(attempt in 1..50){
			val dir = Vec3.fromYaw(target.rotationYaw + rand.nextFloat(angleRange))
			val distance = rand.nextFloat(distanceRange)
			
			val offsetVec = targetVec.add(dir.scale(distance))
			val targetPos = Pos(offsetVec).add(0, rand.nextInt(-4, 8), 0).offsetUntil(DOWN, 0..4){ it.blocksMovement(world) }?.up()
			
			if (targetPos != null && teleportTo(targetPos)){
				return true
			}
		}
		
		return false
	}
	
	fun teleportRandom(distanceRange: ClosedFloatingPointRange<Double>): Boolean{
		if (!checkCooldown()){
			return false
		}
		
		val endermanPos = Pos(enderman)
		
		for(attempt in 1..25){
			val (x, y, z) = rand.nextVector2(xz = rand.nextFloat(distanceRange), y = rand.nextFloat(-24.0, 48.0))
			val targetPos = endermanPos.add(x, y, z).offsetUntil(DOWN, 0..24){ it.blocksMovement(world) }?.up()
			
			if (targetPos != null && teleportTo(targetPos)){
				return true
			}
		}
		
		return false
	}
	
	// Special teleports
	
	fun teleportDelayed(delayTicks: Int, callback: () -> Boolean): Boolean{
		if (enderman.isAIDisabled || !checkCooldown()){
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
	
	fun teleportDodge(dodge: Entity): Boolean{
		val uuid = dodge.uniqueID
		
		if (uuid == lastDodged){
			return false
		}
		
		val endermanPos = enderman.posVec
		val dodgePos = dodge.posVec
		
		val perpendicularVec = Vec3.fromXZ(-dodge.motionZ, dodge.motionX).normalize()
		val dodgeDist = (enderman.width * 0.75) + (dodge.width * 0.75) + 0.25
		
		val dir = maxOf(-1, 1, compareBy {
			endermanPos.add(perpendicularVec.scale(it * dodgeDist)).squareDistanceTo(dodgePos) + (rand.nextDouble() * 0.1)
		})
		
		for(attempt in 1..3){
			val basePos = endermanPos
				.add(perpendicularVec.scale(dir * rand.nextFloat(dodgeDist, dodgeDist + 0.4))
				.add(rand.nextVector2(xz = rand.nextFloat(0.0, 0.15), y = 1.5)))
			
			for(offset in 0..2){
				val targetPos = Vec3d(basePos.x, basePos.y.floorToInt() - offset + 0.01, basePos.z)
				
				if (teleportCheckLocation(TELEPORTER_DODGE, targetPos)){
					lastDodged = uuid
					return true
				}
			}
		}
		
		return false
	}
	
	fun teleportOutOfWorld(force: Boolean = false): Boolean{
		if (!force && !checkCooldown()){
			return false
		}
		
		PacketClientFX(FX_TELEPORT_OUT_OF_WORLD, FxEntityData(enderman)).sendToAllAround(enderman, 64.0)
		enderman.setDead()
		return true
	}
	
	// Serialization
	
	override fun serializeNBT() = TagCompound().apply {
		setShort(COOLDOWN_TAG, tpCooldown.toShort())
		setShort(DELAY_TICKS_TAG, tpDelayTicks.toShort())
	}
	
	override fun deserializeNBT(nbt: TagCompound) = with(nbt){
		tpCooldown = getShort(COOLDOWN_TAG).toInt()
		tpDelayTicks = min(1, getShort(DELAY_TICKS_TAG).toInt())
	}
}
