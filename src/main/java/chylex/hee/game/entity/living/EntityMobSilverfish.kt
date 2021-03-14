package chylex.hee.game.entity.living

import chylex.hee.HEE
import chylex.hee.game.entity.living.ai.AISummonFromBlock
import chylex.hee.game.entity.living.ai.AITargetSwarmSwitch
import chylex.hee.game.entity.living.ai.AttackMelee
import chylex.hee.game.entity.living.ai.HideInBlock
import chylex.hee.game.entity.living.ai.SummonFromBlock
import chylex.hee.game.entity.living.ai.Swim
import chylex.hee.game.entity.living.ai.TargetAttacker
import chylex.hee.game.entity.living.ai.TargetRandom
import chylex.hee.game.entity.living.ai.TargetSwarmSwitch
import chylex.hee.game.entity.living.ai.Wander
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ALL_PROTECTIONS
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_KNOCKBACK
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.RAPID_DAMAGE
import chylex.hee.init.ModEntities
import chylex.hee.network.fx.FxVecHandler
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.EventPriority
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.migration.BlockSilverfish
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.EntitySilverfish
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.heeTag
import chylex.hee.system.serialization.use
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.network.IPacket
import net.minecraft.util.DamageSource
import net.minecraft.util.EntityDamageSource
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.network.NetworkHooks
import java.util.Random
import kotlin.math.floor

class EntityMobSilverfish(type: EntityType<EntityMobSilverfish>, world: World) : EntitySilverfish(type, world), ICritTracker {
	constructor(world: World) : this(ModEntities.SILVERFISH, world)
	
	@SubscribeAllEvents(modid = HEE.ID)
	companion object {
		private val DAMAGE_GENERAL          = Damage(DIFFICULTY_SCALING, PEACEFUL_EXCLUSION, *ALL_PROTECTIONS, RAPID_DAMAGE(5))
		private val DAMAGE_HAUNTWOOD_FOREST = Damage(DIFFICULTY_SCALING, PEACEFUL_KNOCKBACK, *ALL_PROTECTIONS, RAPID_DAMAGE(5))
		
		private const val HIDE_DELAY_TAG = "HideDelay"
		
		val FX_SPAWN_PARTICLE = object : FxVecHandler() {
			override fun handle(world: World, rand: Random, vec: Vec3d) {
				EntityMobSilverfish(world).apply {
					setLocationAndAngles(vec.x, vec.y, vec.z, 0F, 0F)
					spawnExplosionParticle()
				}
			}
		}
		
		@SubscribeEvent(EventPriority.LOWEST)
		fun onEntityJoinWorld(e: EntityJoinWorldEvent) {
			val entity = e.entity
			
			if (entity::class.java === EntitySilverfish::class.java) {
				e.isCanceled = true
				
				val world = e.world
				
				EntityMobSilverfish(world).apply {
					copyLocationAndAnglesFrom(entity)
					world.addEntity(this)
					
					if (rotationYaw == 0F && rotationPitch == 0F && posY == floor(posY) && (posX - 0.5) == floor(posX) && (posZ - 0.5) == floor(posZ)) {
						// high confidence of being spawned from BlockSilverfish
						spawnExplosionParticle()
					}
				}
			}
		}
	}
	
	private lateinit var aiSummonFromBlock: AISummonFromBlock
	private lateinit var aiTargetSwarmSwitch: AITargetSwarmSwitch<EntityPlayer>
	
	private var hideInBlockDelayTicks = 120
	
	override var wasLastHitCritical = false
	
	override fun registerAttributes() {
		super.registerAttributes()
		
		getAttribute(MAX_HEALTH).baseValue = 8.0
		getAttribute(ATTACK_DAMAGE).baseValue = 2.0
		getAttribute(FOLLOW_RANGE).baseValue = 12.0
		
		experienceValue = 3
	}
	
	override fun registerGoals() {
		aiSummonFromBlock = SummonFromBlock(this, searchAttempts = 500, searchDistance = 6, searchingFor = ::isSilverfishBlock)
		
		goalSelector.addGoal(1, Swim(this))
		goalSelector.addGoal(2, AttackMelee(this, movementSpeed = 1.0, chaseAfterLosingSight = false))
		goalSelector.addGoal(3, Wander(this, movementSpeed = 1.0, chancePerTick = 10))
		goalSelector.addGoal(4, HideInBlock(this, chancePerTick = 15, tryHideInBlock = ::tryHideInBlock))
		goalSelector.addGoal(5, aiSummonFromBlock)
		
		aiTargetSwarmSwitch = TargetSwarmSwitch(this, rangeMultiplier = 0.5F, checkSight = true, easilyReachableOnly = false)
		
		targetSelector.addGoal(1, TargetAttacker(this, callReinforcements = true))
		targetSelector.addGoal(2, TargetRandom<EntityPlayer>(this, chancePerTick = 5, checkSight = true, easilyReachableOnly = false))
		targetSelector.addGoal(3, aiTargetSwarmSwitch)
	}
	
	override fun createSpawnPacket(): IPacket<*> {
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun livingTick() {
		super.livingTick()
		
		if (hideInBlockDelayTicks > 0) {
			--hideInBlockDelayTicks
		}
	}
	
	fun delayHideInBlockAI(delayTicks: Int) {
		hideInBlockDelayTicks = delayTicks
	}
	
	fun disableHideInBlockAI() {
		hideInBlockDelayTicks = Int.MAX_VALUE
	}
	
	private fun isSilverfishBlock(block: Block): Boolean {
		return block is BlockSilverfish
	}
	
	private fun tryHideInBlock(state: BlockState): BlockState? {
		return if (BlockSilverfish.canContainSilverfish(state) && hideInBlockDelayTicks == 0)
			BlockSilverfish.infest(state.block)
		else
			null
	}
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean {
		if (!super.attackEntityFrom(source, amount)) {
			return false
		}
		
		if (source is EntityDamageSource || source === DamageSource.MAGIC) {
			aiSummonFromBlock.triggerSummonInTicks(20)
		}
		
		return true
	}
	
	override fun attackEntityAsMob(entity: Entity): Boolean {
		val source = DAMAGE_GENERAL // TODO implement Hauntwood Forest checking
		
		if (!source.dealToFrom(entity, this)) {
			return false
		}
		
		if (rand.nextInt(4) != 0) {
			aiTargetSwarmSwitch.triggerRetarget()
		}
		
		return true
	}
	
	override fun getLootTable(): ResourceLocation {
		return Resource.Custom("entities/silverfish")
	}
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		
		putInt(HIDE_DELAY_TAG, hideInBlockDelayTicks)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		hideInBlockDelayTicks = getInt(HIDE_DELAY_TAG)
	}
}
