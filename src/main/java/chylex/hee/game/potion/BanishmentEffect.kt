package chylex.hee.game.potion

import chylex.hee.HEE
import chylex.hee.game.entity.CustomCreatureType
import chylex.hee.game.entity.damage.Damage
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.FIRE_TYPE
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.IGNORE_INVINCIBILITY
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.entity.living.MobTypes
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectExistingEntities
import chylex.hee.game.fx.FxEntityData
import chylex.hee.game.fx.FxEntityHandler
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.spawner.properties.IOffset.Gaussian
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.potion.BanishmentEffect.EntityKind.DEMON_EYE
import chylex.hee.game.potion.BanishmentEffect.EntityKind.ENDERDEMON
import chylex.hee.game.potion.BanishmentEffect.EntityKind.GENERIC_DEMON
import chylex.hee.game.potion.BanishmentEffect.EntityKind.GENERIC_SHADOW
import chylex.hee.game.potion.BanishmentEffect.EntityKind.GENERIC_UNDEAD
import chylex.hee.game.potion.BanishmentEffect.EntityKind.NONE
import chylex.hee.game.potion.BanishmentEffect.EntityKind.ZOMBIE_VILLAGER
import chylex.hee.game.potion.brewing.PotionTypeMap
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.client.PacketClientLaunchInstantly
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.math.Vec
import chylex.hee.util.math.component1
import chylex.hee.util.math.component2
import chylex.hee.util.math.component3
import chylex.hee.util.math.floorToInt
import net.minecraft.entity.CreatureAttribute.UNDEAD
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.attributes.AttributeModifierManager
import net.minecraft.entity.item.ItemEntity
import net.minecraft.entity.monster.ZombieVillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particles.ParticleTypes.EXPLOSION
import net.minecraft.potion.EffectType.BENEFICIAL
import net.minecraft.potion.Effects
import net.minecraft.util.DamageSource
import net.minecraftforge.event.entity.living.LivingDamageEvent
import java.util.Random
import kotlin.math.max
import kotlin.math.pow

@SubscribeAllEvents(modid = HEE.ID)
object BanishmentEffect : HeeEffect(BENEFICIAL, RGB(253, 253, 253)) {
	private val DAMAGE_BANISH = Damage(FIRE_TYPE(Int.MAX_VALUE), IGNORE_INVINCIBILITY())
	
	val FX_BANISH = object : FxEntityHandler() {
		override fun handle(entity: Entity, rand: Random) {
			val pos = InBox(entity, 0.1F)
			val mot = Gaussian(0.03F)
			val size = entity.width * entity.height
			
			ParticleSpawnerCustom(
				type = ParticleSmokeCustom,
				data = ParticleSmokeCustom.Data(scale = 1.9F),
				pos = pos,
				mot = mot
			).spawn(Point(entity, heightMp = 0.5F, amount = (size * 2.5F).pow(3).floorToInt().coerceIn(4, 40)), rand)
			
			ParticleSpawnerVanilla(
				type = EXPLOSION,
				pos = pos,
				mot = mot,
				hideOnMinimalSetting = false
			).spawn(Point(entity, heightMp = 0.5F, amount = (size * 2F).pow(2).floorToInt().coerceIn(3, 10)), rand)
		}
	}
	
	val POTION
		get() = PotionTypeMap.getPotion(this)
	
	enum class EntityKind {
		DEMON_EYE,
		ENDERDEMON,
		GENERIC_DEMON,
		GENERIC_SHADOW,
		ZOMBIE_VILLAGER,
		GENERIC_UNDEAD,
		NONE
	}
	
	fun determineEntityKind(entity: LivingEntity): EntityKind {
		return when {
			entity is EntityBossEnderEye && entity.isDemonEye -> DEMON_EYE
			CustomCreatureType.isDemon(entity)                -> GENERIC_DEMON
			CustomCreatureType.isShadow(entity)               -> GENERIC_SHADOW
			entity is ZombieVillagerEntity                    -> ZOMBIE_VILLAGER
			entity.creatureAttribute == UNDEAD                -> GENERIC_UNDEAD
			else                                              -> NONE
		}
	}
	
	private fun banish(entity: LivingEntity, damageEvent: LivingDamageEvent?) {
		val source = damageEvent?.source?.trueSource
		val kind = determineEntityKind(entity)
		
		if (kind == DEMON_EYE) {
			// handled in EntityBossEnderEye
		}
		else if (kind == ENDERDEMON) {
			// TODO
		}
		else if (kind == GENERIC_DEMON) {
			damageEvent?.let { it.amount *= 2F }
		}
		else if (kind == GENERIC_SHADOW) {
			if (!MobTypes.isBoss(entity)) {
				PacketClientFX(FX_BANISH, FxEntityData(entity)).sendToAllAround(entity, 24.0)
				entity.remove()
				
				val instantLaunch = mutableListOf<PlayerEntity>()
				
				for (nearby in entity.world.selectExistingEntities.allInRange(entity.posVec, 6.0)) {
					if (nearby is LivingEntity) {
						val (prevX, prevY, prevZ) = nearby.motion
						nearby.applyKnockback(1.25F, entity.posX - nearby.posX, entity.posZ - nearby.posZ)
						val (newX, newY, newZ) = nearby.motion
						
						nearby.motion = Vec(prevX * 0.1 + newX, max(prevY, newY + 0.1), prevZ * 0.1 + newZ)
						
						if (nearby is PlayerEntity) {
							instantLaunch.add(nearby)
						}
					}
					else if (nearby is ItemEntity) {
						entity.posVec.subtract(nearby.posVec).normalize().let { nearby.addVelocity(it.x, it.y, it.z) }
					}
				}
				
				instantLaunch.forEach { PacketClientLaunchInstantly(it, it.motion).sendToPlayer(it) }
			}
			else {
				// TODO
			}
		}
		else if (kind == ZOMBIE_VILLAGER) {
			(entity as? ZombieVillagerEntity)?.startConverting(source?.uniqueID, 1)
		}
		else if (kind == GENERIC_UNDEAD) {
			entity.removePotionEffect(Effects.FIRE_RESISTANCE)
			entity.forceFireTicks(Int.MAX_VALUE)
			
			val damage = (entity.maxHealth * 0.5F).coerceAtMost(20F)
			
			if (damageEvent == null) {
				DAMAGE_BANISH.dealTo(damage, entity)
			}
			else {
				damageEvent.amount = max(damageEvent.amount, damage)
			}
		}
	}
	
	fun canBanish(entity: LivingEntity, source: DamageSource): Boolean {
		val attacker = source.trueSource
		return attacker is PlayerEntity && (entity.isPotionActive(this) || attacker.isPotionActive(this))
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	fun onLivingDamage(e: LivingDamageEvent) {
		if (canBanish(e.entityLiving, e.source)) {
			banish(e.entityLiving, e)
		}
	}
	
	override fun applyAttributesModifiersToEntity(entity: LivingEntity, attributes: AttributeModifierManager, amplifier: Int) {
		super.applyAttributesModifiersToEntity(entity, attributes, amplifier)
		banish(entity, null)
	}
}
