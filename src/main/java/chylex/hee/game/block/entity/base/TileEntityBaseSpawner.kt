package chylex.hee.game.block.entity.base

import chylex.hee.HEE
import chylex.hee.game.block.entity.base.TileEntityBase.Context.STORAGE
import chylex.hee.game.entity.OPERATION_MUL_INCR_INDIVIDUAL
import chylex.hee.game.entity.getAttributeInstance
import chylex.hee.game.entity.heeTag
import chylex.hee.game.entity.heeTagOrNull
import chylex.hee.game.inventory.size
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.totalTime
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.fx.FxEntityData
import chylex.hee.network.fx.FxEntityHandler
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.EventPriority
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.math.LerpedFloat
import chylex.hee.system.math.ceilToInt
import chylex.hee.system.migration.EntityLiving
import chylex.hee.system.random.IRandomColor.Companion.IRandomColor
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getPosOrNull
import chylex.hee.system.serialization.putPos
import chylex.hee.system.serialization.use
import net.minecraft.entity.Entity
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.ai.attributes.Attributes.FLYING_SPEED
import net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH
import net.minecraft.entity.ai.attributes.Attributes.MOVEMENT_SPEED
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.math.BlockPos
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent
import java.util.Random
import java.util.UUID
import kotlin.math.roundToInt

abstract class TileEntityBaseSpawner(type: TileEntityType<out TileEntityBaseSpawner>) : TileEntityBaseSpecialFirstTick(type) {
	@SubscribeAllEvents(modid = HEE.ID)
	companion object {
		private const val LAST_POS_TAG = "LastPos"
		private const val NEXT_SPAWN_COOLDOWN_TAG = "NextSpawnCooldown"
		
		private const val ENTITY_TAINTED_TAG = "Tainted"
		private val BUFF_HEALTH = AttributeModifier(UUID.fromString("2A023243-1D08-4955-8056-C35959129CDF"), "Tainted health buff", 1.0, OPERATION_MUL_INCR_INDIVIDUAL)
		private val DEBUFF_SPEED = AttributeModifier(UUID.fromString("9BEF400C-05DD-49A7-91E1-036EF7B73766"), "Tainted speed debuff", -0.33, OPERATION_MUL_INCR_INDIVIDUAL)
		
		private val PARTICLE_TAINT_COLOR = IRandomColor { RGB(nextInt(4, 40).toUByte()) }
		
		private val PARTICLE_TAINT_BLOCK = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(color = PARTICLE_TAINT_COLOR, scale = 0.75F),
			pos = InBox(0.7F, 0.7F, 0.7F)
		)
		
		val FX_TAINT_TICK = object : FxEntityHandler() {
			override fun handle(entity: Entity, rand: Random) {
				ParticleSpawnerCustom(
					type = ParticleSmokeCustom,
					data = ParticleSmokeCustom.Data(color = PARTICLE_TAINT_COLOR, scale = 0.45F),
					pos = InBox(entity, 0.2F, 0F, 0.2F)
				).spawn(Point(entity, heightMp = 0.5F, amount = rand.nextInt(4, 5)), rand)
			}
		}
		
		@SubscribeEvent
		fun onLivingUpdate(e: LivingUpdateEvent) {
			val entity = e.entityLiving
			val world = entity.world
			
			if (!world.isRemote && world.totalTime % 3L == 0L && entity.heeTagOrNull?.getBoolean(ENTITY_TAINTED_TAG) == true) {
				PacketClientFX(FX_TAINT_TICK, FxEntityData(entity)).sendToAllAround(entity, 9.0)
			}
		}
		
		@SubscribeEvent(EventPriority.HIGH)
		fun onLivingDrops(e: LivingDropsEvent) {
			if (e.entityLiving.heeTagOrNull?.getBoolean(ENTITY_TAINTED_TAG) == true) {
				val rand = e.entityLiving.rng
				
				e.drops.removeAll {
					repeat(it.item.size) { _ ->
						if (rand.nextBoolean()) {
							it.item.size--
						}
					}
					
					it.item.isEmpty
				}
			}
		}
		
		@SubscribeEvent(EventPriority.HIGH)
		fun onLivingExperienceDrop(e: LivingExperienceDropEvent) {
			if (e.entityLiving.heeTagOrNull?.getBoolean(ENTITY_TAINTED_TAG) == true) {
				e.droppedExperience /= 2
			}
		}
	}
	
	private val isTainted
		get() = hasWorld() && lastPos != null && lastPos != pos
	
	private var lastPos: BlockPos? = null
	private var nextSpawnCooldown = 0
	
	val clientEntity by lazy { createClientEntity() }
	val clientRotation = LerpedFloat(0F)
	
	protected abstract val clientRotationSpeed: Float
	protected abstract fun createClientEntity(): Entity
	
	// Ticking
	
	override fun firstTick() {
		if (lastPos == null) {
			lastPos = pos
			markDirty()
		}
		
		if (wrld.isRemote) {
			clientRotation.update(wrld.rand.nextFloat(0F, 360F))
		}
	}
	
	final override fun tick() {
		super.tick()
		
		if (wrld.isRemote) {
			clientRotation.update(clientRotation.currentValue + clientRotationSpeed)
			tickClient()
			
			if (isTainted) {
				val rand = wrld.rand
				PARTICLE_TAINT_BLOCK.spawn(Point(pos, rand.nextInt(1, 2)), rand)
			}
		}
		else {
			tickServer()
		}
	}
	
	protected abstract fun tickClient()
	protected abstract fun tickServer()
	
	// Logic
	
	protected fun startSpawnCooldown(cooldown: Int) {
		if (cooldown <= nextSpawnCooldown) {
			return
		}
		
		nextSpawnCooldown = if (isTainted)
			(cooldown * wrld.rand.nextFloat(1.5F, 2F)).ceilToInt()
		else
			cooldown
		
		markDirty()
	}
	
	protected fun tickSpawnCooldown(): Boolean {
		markDirty()
		return --nextSpawnCooldown < 0
	}
	
	protected fun modifySpawnAmount(amount: Int): Int {
		return if (isTainted)
			(amount * (if (wrld.rand.nextBoolean()) 0.5F else 0.75F)).roundToInt().coerceAtLeast(1)
		else
			amount
	}
	
	@Suppress("UNNECESSARY_SAFE_CALL")
	protected fun spawnMob(entity: EntityLiving) {
		if (isTainted) {
			entity.heeTag.putBoolean(ENTITY_TAINTED_TAG, true)
			entity.getAttributeInstance(MAX_HEALTH)?.applyPersistentModifier(BUFF_HEALTH)
			entity.getAttributeInstance(MOVEMENT_SPEED)?.applyPersistentModifier(DEBUFF_SPEED)
			entity.getAttributeInstance(FLYING_SPEED)?.applyPersistentModifier(DEBUFF_SPEED)
			entity.health = entity.maxHealth
		}
		
		entity.world.addEntity(entity)
		entity.spawnExplosionParticle()
	}
	
	// Serialization
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		lastPos?.let { putPos(LAST_POS_TAG, it) }
		
		if (context == STORAGE) {
			putInt(NEXT_SPAWN_COOLDOWN_TAG, nextSpawnCooldown)
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		lastPos = getPosOrNull(LAST_POS_TAG)
		
		if (context == STORAGE) {
			nextSpawnCooldown = getInt(NEXT_SPAWN_COOLDOWN_TAG)
		}
	}
}
