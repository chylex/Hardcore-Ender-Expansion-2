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
import chylex.hee.init.ModLoot
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.AIAttackMelee
import chylex.hee.system.util.AISwim
import chylex.hee.system.util.AITargetAttacker
import chylex.hee.system.util.AITargetRandom
import chylex.hee.system.util.AITargetSwarmSwitch
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.getAttribute
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.with
import net.minecraft.block.BlockSilverfish
import net.minecraft.block.BlockSilverfish.EnumType.forModelBlock
import net.minecraft.block.BlockSilverfish.VARIANT
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.entity.monster.EntitySilverfish
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.DamageSource
import net.minecraft.util.EntityDamageSource
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import kotlin.math.floor

@SubscribeAllEvents(modid = HEE.ID)
class EntityMobSilverfish(world: World) : EntitySilverfish(world), ICritTracker{
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
					world.spawnEntity(this)
					
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
	
	init{
		setSize(0.575F, 0.35F)
	}
	
	override fun applyEntityAttributes(){
		super.applyEntityAttributes()
		
		getAttribute(MAX_HEALTH).baseValue = 8.0
		getAttribute(ATTACK_DAMAGE).baseValue = 2.0
		getAttribute(FOLLOW_RANGE).baseValue = 12.0
		
		experienceValue = 3
	}
	
	override fun initEntityAI(){
		aiSummonFromBlock = AISummonFromBlock(this, searchAttempts = 500, searchDistance = 6, searchingFor = Blocks.MONSTER_EGG)
		
		tasks.addTask(1, AISwim(this))
		tasks.addTask(2, AIAttackMelee(this, movementSpeed = 1.0, chaseAfterLosingSight = false))
		tasks.addTask(3, AIWander(this, movementSpeed = 1.0, chancePerTick = 10))
		tasks.addTask(4, AIHideInBlock(this, chancePerTick = 15, tryHideInBlock = ::tryHideInBlock))
		tasks.addTask(5, aiSummonFromBlock)
		
		aiTargetSwarmSwitch = AITargetSwarmSwitch(this, rangeMultiplier = 0.5F, checkSight = true, easilyReachableOnly = false)
		
		targetTasks.addTask(1, AITargetAttacker(this, callReinforcements = true))
		targetTasks.addTask(2, AITargetRandom<EntityPlayer>(this, chancePerTick = 5, checkSight = true, easilyReachableOnly = false))
		targetTasks.addTask(3, aiTargetSwarmSwitch)
	}
	
	override fun onLivingUpdate(){
		super.onLivingUpdate()
		
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
	
	private fun tryHideInBlock(state: IBlockState): IBlockState?{
		return if (BlockSilverfish.canContainSilverfish(state) && hideInBlockDelayTicks == 0)
			Blocks.MONSTER_EGG.with(VARIANT, forModelBlock(state))
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
		return ModLoot.SILVERFISH
	}
	
	override fun writeEntityToNBT(nbt: TagCompound) = with(nbt.heeTag){
		super.writeEntityToNBT(nbt)
		
		setInteger(HIDE_DELAY_TAG, hideInBlockDelayTicks)
	}
	
	override fun readEntityFromNBT(nbt: TagCompound) = with(nbt.heeTag){
		super.readEntityFromNBT(nbt)
		
		hideInBlockDelayTicks = getInteger(HIDE_DELAY_TAG)
	}
}
