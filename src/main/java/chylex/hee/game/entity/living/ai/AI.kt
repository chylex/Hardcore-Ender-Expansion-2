package chylex.hee.game.entity.living.ai

import chylex.hee.system.migration.EntityCreature
import chylex.hee.system.migration.EntityLivingBase
import net.minecraft.entity.ai.goal.LookAtGoal
import net.minecraft.entity.ai.goal.LookRandomlyGoal
import net.minecraft.entity.ai.goal.MeleeAttackGoal
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal
import net.minecraft.entity.ai.goal.SwimGoal
import java.util.function.Predicate

typealias AttackLeap = AIAttackLeap
typealias FollowLeaderJumping = AIFollowLeaderJumping
typealias ForceWanderTiming = AIForceWanderTiming
typealias HideInBlock = AIHideInBlock
typealias PickUpBlock = AIPickUpBlock
typealias PickUpItemDetour = AIPickUpItemDetour
typealias SummonFromBlock = AISummonFromBlock
typealias Swim = SwimGoal
typealias TargetAttacker = AITargetAttackerFixed
typealias Wander = AIWander
typealias WanderLand = AIWanderLand
typealias WanderLightStartle = AIWanderLightStartle
typealias WanderOnFirePanic = AIWanderOnFirePanic
typealias WatchDyingLeader = AIWatchDyingLeader
typealias WatchIdle = LookRandomlyGoal
typealias WatchIdleJumping = AIWatchIdleJumping

// Movement

inline fun <reified T : EntityLivingBase> WanderLandStopNear(entity: EntityCreature, movementSpeed: Double, chancePerTick: Int, maxDistanceXZ: Int = 10, maxDistanceY: Int = 7, detectDistance: Double) =
	AIWanderLandStopNear(entity, movementSpeed, chancePerTick, maxDistanceXZ, maxDistanceY, T::class.java, detectDistance)

// Looking

inline fun <reified T : EntityLivingBase> WatchClosest(entity: EntityCreature, maxDistance: Float) =
	LookAtGoal(entity, T::class.java, maxDistance)

// Actions

fun AttackMelee(entity: EntityCreature, movementSpeed: Double, chaseAfterLosingSight: Boolean) =
	MeleeAttackGoal(entity, movementSpeed, chaseAfterLosingSight)

// Targeting

inline fun <reified T : EntityLivingBase> TargetNearby(entity: EntityCreature, chancePerTick: Int, checkSight: Boolean, easilyReachableOnly: Boolean, noinline targetPredicate: ((T) -> Boolean)? = null) =
	NearestAttackableTargetGoal(entity, T::class.java, chancePerTick, checkSight, easilyReachableOnly, targetPredicate?.let { p -> Predicate { p(it as T) } })

inline fun <reified T : EntityLivingBase> TargetEyeContact(entity: EntityCreature, fieldOfView: Float, headRadius: Float, minStareTicks: Int, easilyReachableOnly: Boolean, noinline targetPredicate: ((T) -> Boolean)? = null) =
	AITargetEyeContact(entity, easilyReachableOnly, T::class.java, targetPredicate, fieldOfView, headRadius, minStareTicks)

inline fun <reified T : EntityLivingBase> TargetRandom(entity: EntityCreature, chancePerTick: Int, checkSight: Boolean, easilyReachableOnly: Boolean, noinline targetPredicate: ((T) -> Boolean)? = null) =
	AITargetRandom(entity, checkSight, easilyReachableOnly, T::class.java, targetPredicate, chancePerTick)

inline fun <reified T : EntityLivingBase> TargetSwarmSwitch(entity: EntityCreature, rangeMultiplier: Float, checkSight: Boolean, easilyReachableOnly: Boolean, noinline targetPredicate: ((T) -> Boolean)? = null) =
	AITargetSwarmSwitch(entity, checkSight, easilyReachableOnly, T::class.java, targetPredicate, rangeMultiplier)
