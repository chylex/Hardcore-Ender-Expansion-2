package chylex.hee.game.potion

import chylex.hee.HEE
import chylex.hee.game.entity.CustomCreatureType
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.entity.posVec
import chylex.hee.game.entity.selectExistingEntities
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.FIRE_TYPE
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.IGNORE_INVINCIBILITY
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.spawner.properties.IOffset.Gaussian
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.potion.PotionBanishment.EntityKind.DEMON_EYE
import chylex.hee.game.potion.PotionBanishment.EntityKind.ENDERDEMON
import chylex.hee.game.potion.PotionBanishment.EntityKind.GENERIC_DEMON
import chylex.hee.game.potion.PotionBanishment.EntityKind.GENERIC_SHADOW
import chylex.hee.game.potion.PotionBanishment.EntityKind.GENERIC_UNDEAD
import chylex.hee.game.potion.PotionBanishment.EntityKind.NONE
import chylex.hee.game.potion.PotionBanishment.EntityKind.ZOMBIE_VILLAGER
import chylex.hee.game.potion.brewing.PotionTypeMap
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.client.PacketClientLaunchInstantly
import chylex.hee.network.fx.FxEntityData
import chylex.hee.network.fx.FxEntityHandler
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.EventPriority
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.math.Vec
import chylex.hee.system.math.component1
import chylex.hee.system.math.component2
import chylex.hee.system.math.component3
import chylex.hee.system.math.floorToInt
import chylex.hee.system.migration.EntityItem
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.EntityZombieVillager
import chylex.hee.system.migration.Potion
import chylex.hee.system.migration.Potions
import net.minecraft.entity.CreatureAttribute.UNDEAD
import net.minecraft.entity.Entity
import net.minecraft.entity.ai.attributes.AbstractAttributeMap
import net.minecraft.particles.ParticleTypes.EXPLOSION
import net.minecraft.potion.EffectType.BENEFICIAL
import net.minecraft.util.DamageSource
import net.minecraftforge.event.entity.living.LivingDamageEvent
import java.util.Random
import kotlin.math.max
import kotlin.math.pow

@SubscribeAllEvents(modid = HEE.ID)
object PotionBanishment : Potion(BENEFICIAL, RGB(253, 253, 253).i) {
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
	
	val TYPE
		get() = PotionTypeMap.getType(this)
	
	enum class EntityKind {
		DEMON_EYE,
		ENDERDEMON,
		GENERIC_DEMON,
		GENERIC_SHADOW,
		ZOMBIE_VILLAGER,
		GENERIC_UNDEAD,
		NONE
	}
	
	fun determineEntityKind(entity: EntityLivingBase): EntityKind {
		return when {
			entity is EntityBossEnderEye && entity.isDemonEye -> DEMON_EYE
			CustomCreatureType.isDemon(entity)                -> GENERIC_DEMON
			CustomCreatureType.isShadow(entity)               -> GENERIC_SHADOW
			entity is EntityZombieVillager                    -> ZOMBIE_VILLAGER
			entity.creatureAttribute == UNDEAD                -> GENERIC_UNDEAD
			else                                              -> NONE
		}
	}
	
	private fun banish(entity: EntityLivingBase, damageEvent: LivingDamageEvent?) {
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
			if (entity.isNonBoss) {
				PacketClientFX(FX_BANISH, FxEntityData(entity)).sendToAllAround(entity, 24.0)
				entity.remove()
				
				val instantLaunch = mutableListOf<EntityPlayer>()
				
				for(nearby in entity.world.selectExistingEntities.allInRange(entity.posVec, 6.0)) {
					if (nearby is EntityLivingBase) {
						val (prevX, prevY, prevZ) = nearby.motion
						nearby.knockBack(entity, 1.25F, entity.posX - nearby.posX, entity.posZ - nearby.posZ)
						val (newX, newY, newZ) = nearby.motion
						
						nearby.motion = Vec(prevX * 0.1 + newX, max(prevY, newY + 0.1), prevZ * 0.1 + newZ)
						
						if (nearby is EntityPlayer) {
							instantLaunch.add(nearby)
						}
					}
					else if (nearby is EntityItem) {
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
			(entity as? EntityZombieVillager)?.startConverting(source?.uniqueID, 1)
		}
		else if (kind == GENERIC_UNDEAD) {
			entity.removePotionEffect(Potions.FIRE_RESISTANCE)
			entity.fire = Int.MAX_VALUE
			
			val damage = (entity.maxHealth * 0.5F).coerceAtMost(20F)
			
			if (damageEvent == null) {
				DAMAGE_BANISH.dealTo(damage, entity)
			}
			else {
				damageEvent.amount = max(damageEvent.amount, damage)
			}
		}
	}
	
	fun canBanish(entity: EntityLivingBase, source: DamageSource): Boolean {
		val attacker = source.trueSource
		return attacker is EntityPlayer && (entity.isPotionActive(this) || attacker.isPotionActive(this))
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	fun onLivingDamage(e: LivingDamageEvent) {
		if (canBanish(e.entityLiving, e.source)) {
			banish(e.entityLiving, e)
		}
	}
	
	override fun applyAttributesModifiersToEntity(entity: EntityLivingBase, attributes: AbstractAttributeMap, amplifier: Int) {
		super.applyAttributesModifiersToEntity(entity, attributes, amplifier)
		banish(entity, null)
	}
}
