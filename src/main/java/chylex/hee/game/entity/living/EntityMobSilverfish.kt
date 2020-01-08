package chylex.hee.game.entity.living
import chylex.hee.HEE
import chylex.hee.game.entity.living.ai.AIHideInBlock
import chylex.hee.game.entity.living.ai.AISummonFromBlock
import chylex.hee.game.entity.living.ai.AITargetSwarmSwitch
import chylex.hee.game.entity.living.ai.AIWander
import chylex.hee.game.entity.util.ICritTracker
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ALL_PROTECTIONS
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_KNOCKBACK
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.RAPID_DAMAGE
import chylex.hee.init.ModEntities
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.BlockSilverfish
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.EntitySilverfish
import chylex.hee.system.util.AIAttackMelee
import chylex.hee.system.util.AISwim
import chylex.hee.system.util.AITargetAttacker
import chylex.hee.system.util.AITargetRandom
import chylex.hee.system.util.AITargetSwarmSwitch
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.use
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
import net.minecraft.world.World
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.network.NetworkHooks
import kotlin.math.floor

@SubscribeAllEvents(modid = HEE.ID)
class EntityMobSilverfish(type: EntityType<EntityMobSilverfish>, world: World) : EntitySilverfish(type, world), ICritTracker{
	constructor(world: World) : this(ModEntities.SILVERFISH, world)
	
	companion object{
		private val DAMAGE_GENERAL          = Damage(DIFFICULTY_SCALING, PEACEFUL_EXCLUSION, *ALL_PROTECTIONS, RAPID_DAMAGE(5))
		private val DAMAGE_HAUNTWOOD_FOREST = Damage(DIFFICULTY_SCALING, PEACEFUL_KNOCKBACK, *ALL_PROTECTIONS, RAPID_DAMAGE(5))
		
		private const val HIDE_DELAY_TAG = "HideDelay"
		
		@JvmStatic
		@SubscribeEvent(EventPriority.LOWEST)
		fun onEntityJoinWorld(e: EntityJoinWorldEvent){
			val entity = e.entity
			
			if (entity::class.java === EntitySilverfish::class.java){
				e.isCanceled = true
				
				val world = e.world
				
				EntityMobSilverfish(world).apply {
					copyLocationAndAnglesFrom(entity)
					world.addEntity(this)
					
					if (rotationYaw == 0F && rotationPitch == 0F && posY == floor(posY) && (posX - 0.5) == floor(posX) && (posZ - 0.5) == floor(posZ)){
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
	
	override fun registerAttributes(){
		super.registerAttributes()
		
		getAttribute(MAX_HEALTH).baseValue = 8.0
		getAttribute(ATTACK_DAMAGE).baseValue = 2.0
		getAttribute(FOLLOW_RANGE).baseValue = 12.0
		
		experienceValue = 3
	}
	
	override fun registerGoals(){
		aiSummonFromBlock = AISummonFromBlock(this, searchAttempts = 500, searchDistance = 6, searchingFor = ::isSilverfishBlock)
		
		goalSelector.addGoal(1, AISwim(this))
		goalSelector.addGoal(2, AIAttackMelee(this, movementSpeed = 1.0, chaseAfterLosingSight = false))
		goalSelector.addGoal(3, AIWander(this, movementSpeed = 1.0, chancePerTick = 10))
		goalSelector.addGoal(4, AIHideInBlock(this, chancePerTick = 15, tryHideInBlock = ::tryHideInBlock))
		goalSelector.addGoal(5, aiSummonFromBlock)
		
		aiTargetSwarmSwitch = AITargetSwarmSwitch(this, rangeMultiplier = 0.5F, checkSight = true, easilyReachableOnly = false)
		
		targetSelector.addGoal(1, AITargetAttacker(this, callReinforcements = true))
		targetSelector.addGoal(2, AITargetRandom<EntityPlayer>(this, chancePerTick = 5, checkSight = true, easilyReachableOnly = false))
		targetSelector.addGoal(3, aiTargetSwarmSwitch)
	}
	
	override fun createSpawnPacket(): IPacket<*>{
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun livingTick(){
		super.livingTick()
		
		if (hideInBlockDelayTicks > 0){
			--hideInBlockDelayTicks
		}
	}
	
	fun delayHideInBlockAI(delayTicks: Int){
		hideInBlockDelayTicks = delayTicks
	}
	
	fun disableHideInBlockAI(){
		hideInBlockDelayTicks = Int.MAX_VALUE
	}
	
	private fun isSilverfishBlock(block: Block): Boolean{
		return block is BlockSilverfish
	}
	
	private fun tryHideInBlock(state: BlockState): BlockState?{
		return if (BlockSilverfish.canContainSilverfish(state) && hideInBlockDelayTicks == 0)
			BlockSilverfish.infest(state.block)
		else
			null
	}
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean{
		if (!super.attackEntityFrom(source, amount)){
			return false
		}
		
		if (source is EntityDamageSource || source === DamageSource.MAGIC){
			aiSummonFromBlock.triggerSummonInTicks(20)
		}
		
		return true
	}
	
	override fun attackEntityAsMob(entity: Entity): Boolean{
		val source = DAMAGE_GENERAL // TODO implement Hauntwood Forest checking
		
		if (!source.dealToFrom(entity, this)){
			return false
		}
		
		if (rand.nextInt(4) != 0){
			aiTargetSwarmSwitch.triggerRetarget()
		}
		
		return true
	}
	
	override fun getLootTable(): ResourceLocation{
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
