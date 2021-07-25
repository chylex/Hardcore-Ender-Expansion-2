package chylex.hee.game.mechanics.causatum.events

import chylex.hee.game.entity.Teleporter.Companion.FxTeleportData
import chylex.hee.game.entity.living.EntityMobEnderman
import chylex.hee.game.entity.living.EntityMobEndermanMuppet
import chylex.hee.game.entity.living.EntityMobEndermanMuppet.MuppetType.FIRST_KILL
import chylex.hee.game.entity.technical.EntityTechnicalCausatumEvent
import chylex.hee.game.entity.technical.EntityTechnicalCausatumEvent.ICausatumEventHandler
import chylex.hee.game.entity.util.SerializedEntity
import chylex.hee.game.entity.util.lookDirVec
import chylex.hee.game.entity.util.lookPosVec
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.world.util.blocksMovement
import chylex.hee.game.world.util.offsetUntil
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextVector2
import chylex.hee.system.random.removeItemOrNull
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Vec
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.addY
import chylex.hee.util.math.component1
import chylex.hee.util.math.component2
import chylex.hee.util.math.component3
import chylex.hee.util.math.square
import chylex.hee.util.nbt.NBTObjectList
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getListOfCompounds
import chylex.hee.util.nbt.putList
import chylex.hee.util.nbt.use
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.SoundEvents
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World

class CausatumEventEndermanKill() : ICausatumEventHandler {
	private companion object {
		private const val ENDERMAN_X_TAG = "EndermanX"
		private const val ENDERMAN_Y_TAG = "EndermanY"
		private const val ENDERMAN_Z_TAG = "EndermanZ"
		private const val TIMER_TAG = "Timer"
		private const val KILLER_TAG = "Killer"
		private const val MUPPETS_TAG = "Muppets"
		private const val MUPPET_TAG = "Muppet"
		
		private const val DESPAWN_START_TIME = 270
	}
	
	constructor(enderman: EntityMobEnderman, killer: PlayerEntity) : this() {
		this.deathPos = enderman.posVec
		this.killer.set(killer)
	}
	
	private lateinit var deathPos: Vector3d
	private var timer = 0
	private val killer = SerializedEntity()
	private val muppets = mutableListOf<SerializedEntity>()
	
	override fun update(entity: EntityTechnicalCausatumEvent) {
		++timer
		
		val world = entity.world
		val rand = world.rand
		
		if (timer == DESPAWN_START_TIME + 120) {
			for (muppet in muppets) {
				despawn(world, muppet)
			}
			
			entity.remove()
			return
		}
		
		if (timer == DESPAWN_START_TIME || (timer > DESPAWN_START_TIME + 10 && timer % 3 == 0 && rand.nextInt(5) <= 2)) {
			repeat(rand.nextInt(1, rand.nextInt(2, 3))) {
				val muppet = rand.removeItemOrNull(muppets)
				
				if (muppet == null) {
					entity.remove()
					return
				}
				
				despawn(world, muppet)
			}
		}
		
		if (timer in 30..145 && timer % 5 == 0 && muppets.size < rand.nextInt(16, 28)) {
			repeat(rand.nextInt(1, rand.nextInt(1, 2))) {
				spawn(world)?.let(muppets::add)
			}
		}
		
		val (lookX, lookY, lookZ) = getLookPos(world)
		
		muppets.removeAll {
			with(it.get(world) as? EntityMobEndermanMuppet ?: return@removeAll true) {
				lookController.setLookPosition(lookX, lookY, lookZ)
				
				if (lastDamageSource?.trueSource is PlayerEntity || world.getClosestPlayer(this, 2.0) != null) {
					if (timer < DESPAWN_START_TIME) {
						timer = DESPAWN_START_TIME
					}
					
					despawn(world, it)
					return@removeAll true
				}
				
				return@removeAll false
			}
		}
	}
	
	private fun spawn(world: World): SerializedEntity? {
		val rand = world.rand
		val muppet = EntityMobEndermanMuppet(world, FIRST_KILL)
		
		val killerLookDir = killer.get(world)?.lookDirVec?.scale(3.0) ?: Vec3.ZERO
		
		for (attempt in 1..1000) {
			val testVec = deathPos.add(killerLookDir).add(rand.nextVector2(xz = rand.nextFloat(5.0, 11.0), y = 0.0))
			val testY = Pos(testVec).offsetUntil(DOWN, -4..7) { it.blocksMovement(world) }
			
			if (testY != null) {
				muppet.setPosition(testVec.x, testY.y + 1.01, testVec.z)
				
				if (muppet.getDistanceSq(deathPos.x, deathPos.y, deathPos.z) > square(4.0) &&
				    world.getClosestPlayer(muppet, 7.0) == null &&
				    world.hasNoCollisions(muppet) &&
				    muppet.isNotColliding(world)
				) {
					val endPoint = muppet.posVec.addY(muppet.height * 0.5)
					val startPoint = endPoint.addY(64.0)
					
					FxTeleportData(startPoint, endPoint, muppet.width, muppet.height, SoundEvents.ENTITY_ENDERMAN_TELEPORT, muppet.soundCategory, soundVolume = 0.7F).send(world)
					
					val (lookX, lookY, lookZ) = getLookPos(world)
					
					with(muppet) {
						lookController.setLookPosition(lookX, lookY, lookZ, 360F, 180F)
						lookController.tick()
						
						prevRotationYawHead = rotationYawHead
						prevRotationYaw = rotationYawHead
						rotationYaw = rotationYawHead
					}
					
					world.addEntity(muppet)
					return SerializedEntity(muppet)
				}
			}
		}
		
		return null
	}
	
	private fun despawn(world: World, muppet: SerializedEntity) {
		(muppet.get(world) as? EntityMobEndermanMuppet)?.despawnOutOfWorld()
	}
	
	private fun getLookPos(world: World): Vector3d {
		return killer.get(world)?.lookPosVec ?: deathPos
	}
	
	override fun serializeNBT() = TagCompound().apply {
		putDouble(ENDERMAN_X_TAG, deathPos.x)
		putDouble(ENDERMAN_Y_TAG, deathPos.y)
		putDouble(ENDERMAN_Z_TAG, deathPos.z)
		
		killer.writeToNBT(this, KILLER_TAG)
		putList(MUPPETS_TAG, NBTObjectList.of(muppets.map { TagCompound().apply { it.writeToNBT(this, MUPPET_TAG) } }))
		
		putInt(TIMER_TAG, timer)
	}
	
	override fun deserializeNBT(nbt: TagCompound) = nbt.use {
		deathPos = Vec(getDouble(ENDERMAN_X_TAG), getDouble(ENDERMAN_Y_TAG), getDouble(ENDERMAN_Z_TAG))
		
		killer.readFromNBT(this, KILLER_TAG)
		muppets.clear()
		muppets.addAll(getListOfCompounds(MUPPETS_TAG).map { SerializedEntity().apply { readFromNBT(it, MUPPET_TAG) } })
		
		timer = getInt(TIMER_TAG)
	}
}
