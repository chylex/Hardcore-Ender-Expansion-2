package chylex.hee.game.entity.living
import chylex.hee.HEE
import chylex.hee.game.entity.living.ai.AIPickUpBlock
import chylex.hee.game.entity.living.ai.AITargetEyeContact
import chylex.hee.game.entity.living.ai.AIWanderLand
import chylex.hee.game.entity.living.ai.AIWatchTargetInShock
import chylex.hee.game.entity.living.ai.util.AIToggle
import chylex.hee.game.entity.living.ai.util.AIToggle.Companion.addTask
import chylex.hee.game.entity.living.behavior.EndermanBlockHandler
import chylex.hee.game.entity.living.behavior.EndermanTeleportHandler
import chylex.hee.game.entity.living.behavior.EndermanWaterHandler
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.game.mechanics.causatum.CausatumStage
import chylex.hee.game.mechanics.causatum.CausatumStage.S0_INITIAL
import chylex.hee.game.mechanics.causatum.EnderCausatum
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ALL_PROTECTIONS_WITH_SHIELD
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.NUDITY_DANGER
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.init.ModLoot
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.AIAttackMelee
import chylex.hee.system.util.AISwim
import chylex.hee.system.util.AITargetEyeContact
import chylex.hee.system.util.AIWatchIdle
import chylex.hee.system.util.Pos
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.square
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.EnumCreatureType.MONSTER
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntityLlamaSpit
import net.minecraft.entity.projectile.EntityPotion
import net.minecraft.entity.projectile.EntityThrowable
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.datasync.DataParameter
import net.minecraft.util.DamageSource
import net.minecraft.util.EntityDamageSourceIndirect
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.EnumSkyBlock.BLOCK
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.Biome.SpawnListEntry
import net.minecraft.world.biome.BiomeEnd
import net.minecraft.world.biome.BiomeHell
import net.minecraft.world.biome.BiomeHellDecorator
import net.minecraftforge.event.entity.ProjectileImpactEvent
import kotlin.math.min
import kotlin.math.pow

@SubscribeAllEvents(modid = HEE.ID)
class EntityMobEnderman(world: World) : EntityMobAbstractEnderman(world){
	companion object{
		private val DAMAGE_GENERAL = Damage(DIFFICULTY_SCALING, PEACEFUL_EXCLUSION, *ALL_PROTECTIONS_WITH_SHIELD, NUDITY_DANGER)
		
		private val TELEPORT_AFTER_DAMAGE = setOf(
			DamageSource.ANVIL,
			DamageSource.CACTUS,
			DamageSource.CRAMMING,
			DamageSource.DROWN,
			DamageSource.FALLING_BLOCK,
			DamageSource.HOT_FLOOR,
			DamageSource.IN_FIRE,
			DamageSource.IN_WALL,
			DamageSource.LAVA,
			DamageSource.LIGHTNING_BOLT,
			DamageSource.FIREWORKS
		)
		
		private val TELEPORT_RANGE_AVOID_BATTLE = (12.0)..(24.0)
		private val TELEPORT_RANGE_RANDOM_IDLE = (8.0)..(64.0)
		private val TELEPORT_RANGE_RANDOM_BUSY = (4.0)..(12.0)
		
		// Projectile event
		
		@JvmStatic
		@SubscribeEvent
		fun onProjectileImpact(e: ProjectileImpactEvent){
			val enderman = e.rayTraceResult.entityHit
			
			if (enderman !is EntityMobEnderman){
				return
			}
			
			if (enderman.world.isRemote){
				e.isCanceled = true // stops arrows from bouncing off (and other projectiles from reacting to the canceled hit)
				return
			}
			
			val projectile = e.entity
			val tp = enderman.teleportHandler
			
			e.isCanceled = when(projectile){
				is EntityPotion ->
					tp.teleportRandom(TELEPORT_RANGE_AVOID_BATTLE)
				
				is EntityArrow,
				is EntityLlamaSpit,
				is EntityThrowable,
				is EntityProjectileSpatialDash ->
					tp.teleportDodge(projectile)
				
				else ->
					tp.teleportRandom(TELEPORT_RANGE_AVOID_BATTLE)
			}
			
			enderman.revengeTarget = when(projectile){
				is EntityThrowable -> projectile.thrower
				is EntityArrow -> projectile.shootingEntity as? EntityLivingBase
				else -> return
			}
		}
		
		// Biome spawns
		
		private fun isBiomeBlacklisted(biome: Biome): Boolean{
			return biome is BiomeEnd || biome is BiomeHell || biome.decorator is BiomeHellDecorator || biome.topBlock.block === Blocks.NETHERRACK
		}
		
		private fun isEndermanEntry(entry: SpawnListEntry): Boolean{
			return EntityEnderman::class.java.isAssignableFrom(entry.entityClass)
		}
		
		fun setupBiomeSpawns(){
			for(biome in Biome.REGISTRY){
				val monsters = biome.getSpawnableList(MONSTER)
				
				if (isBiomeBlacklisted(biome)){
					monsters.removeIf(::isEndermanEntry)
				}
				else{
					val totalWeight = monsters.sumBy { it.itemWeight }                              // default 515
					val endermanWeight = monsters.filter(::isEndermanEntry).sumBy { it.itemWeight } // default 10
					
					if (endermanWeight > 0){
						val newSingleWeight = (2 * endermanWeight) + (totalWeight / 20) // should be about 45 for most biomes
						val newGroupWeight = (2 * endermanWeight) / 3                   // should be about 4 for most biomes
						
						monsters.removeIf(::isEndermanEntry)
						monsters.add(SpawnListEntry(EntityMobEnderman::class.java, newSingleWeight, 1, 1))
						monsters.add(SpawnListEntry(EntityMobEnderman::class.java, newGroupWeight, 2, 3))
					}
				}
			}
		}
	}
	
	// Instance
	
	private lateinit var teleportHandler: EndermanTeleportHandler
	private lateinit var waterHandler: EndermanWaterHandler
	private lateinit var blockHandler: EndermanBlockHandler
	
	private lateinit var aiAttackTarget: AIToggle
	private lateinit var aiPickUpBlocks: AIToggle
	private lateinit var aiWatchTargetInShock: AIWatchTargetInShock
	
	private var heldBlockTimer: Short = 0
	private var heldBlockDespawns = false
	
	private var trackedCausatumStage: CausatumStage? = null
	private var teleportAfterStare = false
	private var teleportAfterStareDelayedChance = 0F
	private var wasFirstKill = false
	
	override val teleportCooldown
		get() = when(trackedCausatumStage){
			null       -> rand.nextInt(20 * 5, 20 * 10)
			S0_INITIAL -> rand.nextInt(20 * 7, 20 *  8)
			else       -> rand.nextInt(20 * 4, 20 *  5)
		}
	
	// Initialization
	
	override fun applyEntityAttributes(){
		super.applyEntityAttributes()
		
		getEntityAttribute(MAX_HEALTH).baseValue = 40.0
		getEntityAttribute(ATTACK_DAMAGE).baseValue = 5.0
		getEntityAttribute(FOLLOW_RANGE).baseValue = 64.0
		
		experienceValue = 10
	}
	
	override fun initEntityAI(){
		teleportHandler = EndermanTeleportHandler(this)
		waterHandler = EndermanWaterHandler(this)
		blockHandler = EndermanBlockHandler(this)
		
		aiWatchTargetInShock = AIWatchTargetInShock(this, maxDistance = 72.0)
		
		aiAttackTarget = AIToggle()
		aiAttackTarget.enabled = true
		
		aiPickUpBlocks = AIToggle()
		aiPickUpBlocks.enabled = rand.nextInt(5) == 0
		
		tasks.addTask(1, AISwim(this))
		tasks.addTask(2, aiWatchTargetInShock)
		tasks.addTask(3, AIAttackMelee(this, movementSpeed = 1.0, chaseAfterLosingSight = false), aiAttackTarget)
		tasks.addTask(4, AIWanderLand(this, movementSpeed = 0.9, chancePerTick = 70))
		tasks.addTask(5, AIWatchIdle(this))
		tasks.addTask(6, AIPickUpBlock(this, ticksPerAttempt = 20 * 30, handler = blockHandler), aiPickUpBlocks)
		
		targetTasks.addTask(1, AITargetEyeContact(this, fieldOfView = 140F, headRadius = 0.19F, minStareTicks = 9, easilyReachableOnly = false, targetPredicate = ::canTriggerEyeContact))
	}
	
	// Behavior
	
	override fun onLivingUpdate(){
		if (isAggressive){
			with(HEE.proxy){
				pauseParticles()
				super.onLivingUpdate()
				resumeParticles()
			}
		}
		else{
			super.onLivingUpdate()
		}
		
		if (!world.isRemote){
			teleportHandler.update()
			waterHandler.update()
			
			if (heldBlockTimer > 0 && --heldBlockTimer == 0.toShort()){
				if (heldBlockDespawns || !blockHandler.tryPlaceBlock(allowPlayerProximity = false)){
					teleportHandler.teleportOutOfWorld(force = rand.nextBoolean())
				}
			}
			
			if (idleTime > 150 && ticksExisted % 15 == 0){
				var despawnChance = 300
				despawnChance -= 15 * (11 - world.skylightSubtracted) // skylightSubtracted goes from 0 (day) to 11 (night)
				despawnChance -= if (heldBlockState != null) 75 else 0
				despawnChance /= if (isWet) 8 else 1
				
				if (rand.nextInt(despawnChance) == 0){
					teleportHandler.teleportOutOfWorld()
				}
			}
			
			if (attackTarget != null && !aiAttackTarget.enabled && !aiWatchTargetInShock.isWatching){
				attackTarget = null
			}
			
			if (teleportAfterStare){
				if (!aiWatchTargetInShock.isWatching){
					teleportAfterStare = false
					
					if (rand.nextFloat() < teleportAfterStareDelayedChance){
						teleportHandler.teleportDelayed(rand.nextInt(20, 50), ::teleportBehindTarget)
					}
					else{
						teleportBehindTarget()
					}
				}
			}
			else if (idleTime > 50 && teleportHandler.checkCooldownSilent() && rand.nextInt(300) == 0){
				val xzDistance = if (attackTarget == null)
					TELEPORT_RANGE_RANDOM_IDLE
				else
					TELEPORT_RANGE_RANDOM_BUSY
				
				teleportHandler.teleportRandom(xzDistance)
			}
		}
	}
	
	override fun notifyDataManagerChange(key: DataParameter<*>){
		super.notifyDataManagerChange(key)
		
		if (key === SCREAMING && world.isRemote && isAggressive){
			// TODO sound fx
		}
	}
	
	override fun setHeldBlockState(state: IBlockState?){
		super.setHeldBlockState(state)
		
		if (state == null){
			heldBlockTimer = 0
		}
		else{
			val seconds = rand.nextInt(5, 45)
			
			heldBlockTimer = (20 * seconds).toShort()
			heldBlockDespawns = rand.nextFloat() > (seconds / 35F).pow(1.2F)
		}
	}
	
	// Battle (Interactions)
	
	private fun canTriggerEyeContact(target: EntityPlayer): Boolean{
		return when(EnderCausatum.getStage(target)){
			S0_INITIAL -> getDistanceSq(target) <= square(32)
			else -> true
		}
	}
	
	private fun beginStare(durationTicks: Int){
		aiWatchTargetInShock.startWatching(durationTicks)
		aiWatchTargetInShock.updateTask() // forces look update for teleportation
		idleTime = 0
	}
	
	private fun beginDeathStare(delayedTeleportChance: Float){
		beginStare(rand.nextInt(35, 55))
		teleportAfterStare = true
		teleportAfterStareDelayedChance = delayedTeleportChance
	}
	
	private fun beginScaredStare(){
		beginStare(rand.nextInt(120, 200))
	}
	
	private fun forceDropHeldBlock(){
		if (!blockHandler.tryPlaceBlock(allowPlayerProximity = true)){
			blockHandler.dropBlockAsItem()
		}
	}
	
	private fun teleportBehindTarget(): Boolean{
		forceDropHeldBlock()
		return attackTarget?.let { teleportHandler.teleportAround(it, (-80F)..(80F), (-7.0)..(-2.0)) } ?: false
	}
	
	// Battle (Targeting)
	
	override fun setAttackTarget(newTarget: EntityLivingBase?){
		if (attackTarget === newTarget){
			return
		}
		
		if (newTarget == null){
			super.setAttackTarget(null)
			trackedCausatumStage = null
			isAggressive = false
		}
		else if (newTarget is EntityPlayer){
			val currentStage = trackedCausatumStage
			val newTargetStage = EnderCausatum.getStage(newTarget)
			
			if (currentStage == null && newTargetStage == S0_INITIAL && teleportHandler.teleportRandom(TELEPORT_RANGE_AVOID_BATTLE)){
				return
			}
			
			super.setAttackTarget(newTarget)
			
			trackedCausatumStage = maxOf(newTargetStage, currentStage ?: newTargetStage)
			
			if (trackedCausatumStage == S0_INITIAL){
				aiAttackTarget.enabled = false
			}
			else{
				aiAttackTarget.enabled = true
				isAggressive = true
			}
			
			if (targetTasks.taskEntries.any { it.using && it.action is AITargetEyeContact<*> }){
				if (trackedCausatumStage == S0_INITIAL){
					beginScaredStare()
				}
				else{
					beginDeathStare(0.4F)
				}
			}
			
			heldBlockTimer = 0
		}
	}
	
	override fun setRevengeTarget(entity: EntityLivingBase?){
		if (attackTarget == null && entity is EntityPlayer && rng.nextInt(4) == 0 && canEntityBeSeen(entity) && getDistanceSq(entity) < square(32)){
			attackTarget = entity
			beginDeathStare(0.85F)
		}
	}
	
	// Battle (Damage)
	
	override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean{
		if (!world.isRemote){
			val attacker = source.immediateSource as? EntityPlayer
			
			if (attacker != null){
				if (trackedCausatumStage == S0_INITIAL || EnderCausatum.getStage(attacker) == S0_INITIAL){
					trackedCausatumStage = S0_INITIAL // prevents teleport from setting attackTarget
					attackTarget = attacker
					beginScaredStare()
					forceDropHeldBlock()
					
					if (teleportHandler.teleportRandom(TELEPORT_RANGE_AVOID_BATTLE)){
						return false
					}
				}
				else if (attackTarget == null){
					attackTarget = attacker
					forceDropHeldBlock()
					
					if (teleportHandler.teleportDelayed(rand.nextInt(25, 55), ::teleportBehindTarget)){
						return false
					}
				}
			}
		}
		
		if (!super.attackEntityFrom(source, amount)){
			return false
		}
		
		if (trackedCausatumStage == S0_INITIAL){
			beginScaredStare()
		}
		else{
			aiWatchTargetInShock.stopWatching()
		}
		
		if (TELEPORT_AFTER_DAMAGE.contains(source)){
			if (health < rand.nextFloat(6F, 12F) || !teleportHandler.teleportRandom(TELEPORT_RANGE_AVOID_BATTLE)){
				teleportHandler.teleportOutOfWorld()
			}
		}
		else if (source is EntityDamageSourceIndirect){
			teleportHandler.teleportRandom(TELEPORT_RANGE_AVOID_BATTLE)
			
			if (revengeTarget == null){
				revengeTarget = source.trueSource as? EntityLivingBase
			}
		}
		
		return true
	}
	
	override fun attackEntityAsMob(entity: Entity): Boolean{
		return DAMAGE_GENERAL.dealToFrom(entity, this)
	}
	
	override fun onDeath(cause: DamageSource){
		if (dead){
			return
		}
		
		val attacker = cause.trueSource as? EntityPlayer ?: attackingPlayer
		
		if (attacker != null && EnderCausatum.triggerStage(attacker, CausatumStage.S1_KILLED_ENDERMAN)){
			// TODO trigger stage for everyone nearby & spawn event
			wasFirstKill = true
		}
		
		super.onDeath(cause)
	}
	
	// Spawning
	
	override fun getBlockPathWeight(pos: BlockPos): Float{
		return 1F
	}
	
	override fun isValidLightLevel(): Boolean{
		val pos = Pos(this)
		
		if (world.isRainingAt(pos)){
			return false
		}
		
		if (world.getLightFor(BLOCK, pos) >= rand.nextInt(4, 8)){
			return false
		}
		
		return !world.provider.isSurfaceWorld || checkSkylightLevel()
	}
	
	private fun checkSkylightLevel(): Boolean{
		val skylight = world.skylightSubtracted // goes from 0 (day) to 11 (night)
		val endermen = world.loadedEntityList.count { it is EntityMobEnderman }
		
		val extra = min(5, world.playerEntities.count { !it.isSpectator } - 1) // TODO use chunk set for better precision
		
		return (skylight >= 11 && endermen <= 40 + (extra * 5)) || (skylight >= rand.nextInt(3, 9) && endermen < (skylight * 2) + (extra * 2))
	}
	
	// Properties
	
	override fun getLootTable(): ResourceLocation{
		return if (wasFirstKill)
			ModLoot.ENDERMAN_FIRST_KILL
		else
			ModLoot.ENDERMAN
	}
	
	override fun getEyeHeight(): Float{
		return 2.62F
	}
	
	// Serialization
	
	override fun writeEntityToNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		super.writeEntityToNBT(nbt)
		
		setTag("Teleport", teleportHandler.serializeNBT())
		setTag("Water", waterHandler.serializeNBT())
		
		setBoolean("CanPickUpBlocks", aiPickUpBlocks.enabled)
		setShort("HeldBlockTimer", heldBlockTimer)
		setBoolean("HeldBlockDespawns", heldBlockDespawns)
	}
	
	override fun readEntityFromNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		super.readEntityFromNBT(nbt)
		
		teleportHandler.deserializeNBT(getCompoundTag("Teleport"))
		waterHandler.deserializeNBT(getCompoundTag("Water"))
		
		aiPickUpBlocks.enabled = getBoolean("CanPickUpBlocks")
		heldBlockTimer = getShort("HeldBlockTimer")
		heldBlockDespawns = getBoolean("HeldBlockDespawns")
	}
}
