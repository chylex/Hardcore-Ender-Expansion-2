package chylex.hee.game.entity.living.behavior

import chylex.hee.client.MC
import chylex.hee.client.sound.UndreadFuseSound
import chylex.hee.game.entity.OPERATION_ADD
import chylex.hee.game.entity.OPERATION_MUL_INCR_INDIVIDUAL
import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.game.entity.posVec
import chylex.hee.game.mechanics.dust.DustType
import chylex.hee.game.mechanics.explosion.ExplosionBuilder
import chylex.hee.game.particle.ParticleCriticalHitCustom
import chylex.hee.game.particle.ParticleEnchantedHitCustom
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.Constant
import chylex.hee.game.particle.spawner.properties.IOffset.Gaussian
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IOffset.OutlineBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.potion.makeEffect
import chylex.hee.game.world.playPlayer
import chylex.hee.init.ModEntities
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.fx.FxEntityData
import chylex.hee.network.fx.FxEntityHandler
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.Vec
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.addY
import chylex.hee.system.math.ceilToInt
import chylex.hee.system.math.withY
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Potions
import chylex.hee.system.random.IRandomColor
import chylex.hee.system.random.IRandomColor.Companion.IRandomColor
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextRounded
import chylex.hee.system.serialization.NBTObjectList
import com.google.common.collect.Sets
import net.minecraft.entity.Entity
import net.minecraft.entity.ILivingEntityData
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE
import net.minecraft.entity.SharedMonsterAttributes.ATTACK_KNOCKBACK
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.entity.SharedMonsterAttributes.MOVEMENT_SPEED
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.util.DamageSource
import net.minecraft.util.SoundCategory
import java.util.EnumSet
import java.util.Random
import java.util.UUID

class UndreadDustEffects(private val dustTypes: EnumSet<DustType>) : ILivingEntityData {
	companion object {
		val NONE = UndreadDustEffects(EnumSet.noneOf(DustType::class.java))
		
		fun fromNBT(types: NBTObjectList<String>): UndreadDustEffects {
			return UndreadDustEffects(Sets.newEnumSet(types.mapNotNull(DustType.Companion::fromKey), DustType::class.java))
		}
		
		private val STAT_MULTIPLIERS = mapOf(
			1 to 1.0,
			2 to 0.6,
			3 to 0.4
		)
		
		private val SPECIAL_MULTIPLIERS = mapOf(
			1 to 1.0,
			2 to 0.9,
			3 to 0.8
		)
		
		private val ATTRIBUTES = STAT_MULTIPLIERS.mapValues { (count, mp) ->
			mapOf(
				DustType.END_POWDER to mapOf(
					MAX_HEALTH     to AttributeModifier(UUID.fromString("D8083013-0F87-4B8F-A45A-0DF66FB22D11"), "Undread End Powder health $count", (3.00 * mp) - 1.0, OPERATION_MUL_INCR_INDIVIDUAL).setSaved(true),
					MOVEMENT_SPEED to AttributeModifier(UUID.fromString("E4610BDE-9D6C-46EC-B95F-2A2FF7B6FACF"), "Undread End Powder speed $count",  (0.75 * mp) - 1.0, OPERATION_MUL_INCR_INDIVIDUAL).setSaved(true),
				),
				
				DustType.REDSTONE to mapOf(
					ATTACK_DAMAGE    to AttributeModifier(UUID.fromString("1C26CF5A-4FA0-4357-A87C-1F0AE69F5B1A"), "Undread Redstone damage $count",    (1.75 * mp) - 1.0, OPERATION_MUL_INCR_INDIVIDUAL).setSaved(true),
					ATTACK_KNOCKBACK to AttributeModifier(UUID.fromString("7619AFD6-DC12-42B8-BA77-8427F1CF891A"), "Undread Redstone knockback $count", (2.40 * mp),       OPERATION_ADD).setSaved(true), // 0.4 + (0.5 * ATTACK_KNOCKBACK) = 1.6 (4x)
				),
				
				DustType.SUGAR to mapOf(
					MOVEMENT_SPEED to AttributeModifier(UUID.fromString("3EE0A661-9824-4AA4-B62D-04A24A4DA99A"), "Undread Sugar speed $count", (1.88 * mp) - 1.0, OPERATION_MUL_INCR_INDIVIDUAL).setSaved(true)
				)
			)
		}
		
		private val PARTICLE_END_POWDER_DATA = ParticleEnchantedHitCustom.Data(color = IRandomColor { RGB(nextInt(68, 96), nextInt(138, 202), nextInt(198, 244)) }, lifespan = 17..32, scale = (0.078F)..(0.092F))
		private val PARTICLE_END_POWDER_POS = ModEntities.UNDREAD.size.let { OutlineBox(it.width * 0.55F, it.height * 0.5F, it.width * 0.55F) } + InBox(-0.1F, 0.1F, 0F, 0.1F, -0.1F, 0.1F)
		
		private val PARTICLE_ANCIENT_DUST_DATA = ParticleCriticalHitCustom.Data(color = IRandomColor { RGB(nextInt(110, 148).toUByte()) }, lifespan = 12..24, scale = (0.062F)..(0.072F))
		private val PARTICLE_ANCIENT_DUST = ParticleSpawnerCustom(
			type = ParticleCriticalHitCustom,
			data = PARTICLE_ANCIENT_DUST_DATA,
			pos = ModEntities.UNDREAD.size.let { InBox(it.width * 0.85F, it.height * 0.55F, it.width * 0.85F) },
			mot = InBox(0.004F)
		)
		
		private val PARTICLE_GUNPOWDER = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(color = IRandomColor { if (nextBoolean()) RGB(40u) else RGB(220u) }, lifespan = 20, scale = 0.65F),
			pos = InBox(0.32F, 0.25F, 0.32F)
		)
		
		private val PARTICLE_REDSTONE = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(color = IRandomColor.Static(RGB(255, 0, 0)), lifespan = 9..14, scale = (0.32F)..(0.44F)),
			pos = InBox(0.22F),
			mot = InBox(0F, 0F, 0F, 0.002F, 0F, 0F)
		)
		
		val FX_CURSE = object : FxEntityHandler() {
			override fun handle(entity: Entity, rand: Random) {
				ParticleSpawnerCustom(
					type = ParticleCriticalHitCustom,
					data = PARTICLE_ANCIENT_DUST_DATA,
					pos = OutlineBox(entity.width * 0.825F, entity.height * 0.51F, entity.width * 0.825F) + InBox(0.3F),
					mot = InBox(0.004F)
				).spawn(Point(entity, heightMp = 0.53F, amount = 44), rand)
			}
		}
	}
	
	private val dustCountForMultipliers
		get() = dustTypes.size.coerceIn(1, 3)
	
	@Sided(Side.CLIENT)
	fun tickClient(entity: EntityMobUndread) {
		if (entity.ticksExisted == 1 && dustTypes.contains(DustType.GUNPOWDER)) {
			MC.instance.soundHandler.play(UndreadFuseSound(entity))
		}
		
		val rand = entity.rng
		
		if (dustTypes.contains(DustType.END_POWDER) && rand.nextInt(6) == 0) {
			val mot = entity.motion.withY(0.0)
			
			ParticleSpawnerCustom(
				type = ParticleEnchantedHitCustom,
				data = PARTICLE_END_POWDER_DATA,
				pos = PARTICLE_END_POWDER_POS + Constant(mot.normalize().scale(0.5)),
				mot = Constant(mot.scale(2.5)) + Gaussian(0.003F)
			).spawn(Point(entity, heightMp = 0.55F, amount = rand.nextRounded(1.1F)), rand)
		}
		
		if (dustTypes.contains(DustType.ANCIENT_DUST) && rand.nextInt(5) == 0) {
			PARTICLE_ANCIENT_DUST.spawn(Point(entity, heightMp = 0.6F, amount = rand.nextInt(1, 3)), rand)
		}
		
		if (dustTypes.contains(DustType.GUNPOWDER)) {
			PARTICLE_GUNPOWDER.spawn(Point(entity, heightMp = 0.91F, amount = 1), rand)
		}
		
		if (dustTypes.contains(DustType.REDSTONE) && rand.nextBoolean()) {
			val dir = Vec3.fromYaw(entity.renderYawOffset)
			val side = Vec3.xz(dir.z, -dir.x)
			val end = entity.posVec.addY(if (entity.isAggressive) 1.285 else 1.24).add(dir.scale(0.69))
			PARTICLE_REDSTONE.spawn(Point(end.add(side.scale(0.325)), 1), rand)
			PARTICLE_REDSTONE.spawn(Point(end.add(side.scale(-0.325)), 1), rand)
		}
		
		if (dustTypes.contains(DustType.SUGAR)) {
			entity.createRunningParticles()
		}
	}
	
	fun applyAttributes(entity: EntityMobUndread) {
		val count = dustCountForMultipliers
		
		for(dustType in dustTypes) {
			val modifiers = ATTRIBUTES.getValue(count)[dustType] ?: continue
			
			for((attribute, modifier) in modifiers) {
				entity.getAttribute(attribute).applyModifier(modifier)
			}
		}
		
		entity.health = entity.maxHealth
	}
	
	fun onAttack(entity: EntityMobUndread): Boolean {
		if (dustTypes.contains(DustType.GUNPOWDER)) {
			explode(entity)
			return true
		}
		
		return false
	}
	
	fun onHit(entity: EntityMobUndread, amount: Float): Float {
		if (dustTypes.contains(DustType.ANCIENT_DUST)) {
			return entity.maxHealth * (if (dustTypes.contains(DustType.END_POWDER)) 0.51F else 1.01F)
		}
		
		return amount
	}
	
	fun onHurt(entity: EntityMobUndread, source: DamageSource) {
		val attacker = source.immediateSource as? EntityPlayer
		if (attacker != null && dustTypes.contains(DustType.ANCIENT_DUST)) {
			val weakness = attacker.getActivePotionEffect(Potions.WEAKNESS)
			if (weakness == null || weakness.duration <= 50) {
				attacker.addPotionEffect(Potions.WEAKNESS.makeEffect(100))
			}
			else if (weakness.amplifier < 2) {
				attacker.addPotionEffect(Potions.WEAKNESS.makeEffect(100, weakness.amplifier + 1))
			}
			else {
				attacker.addPotionEffect(Potions.WEAKNESS.makeEffect(100, weakness.amplifier))
				attacker.addPotionEffect(Potions.BLINDNESS.makeEffect((400 * SPECIAL_MULTIPLIERS.getValue(dustCountForMultipliers)).ceilToInt()))
			}
			
			ModSounds.MOB_UNDREAD_CURSE.playPlayer(attacker, attacker.posVec, SoundCategory.HOSTILE, volume = 0.85F, pitch = attacker.rng.nextFloat(0.9F, 1F))
			PacketClientFX(FX_CURSE, FxEntityData(attacker)).sendToAllAround(attacker, 24.0)
		}
		
		if (source.isExplosion && dustTypes.contains(DustType.GUNPOWDER)) {
			explode(entity)
		}
	}
	
	private fun explode(entity: EntityMobUndread) {
		val explosionStrength = 2.5F * SPECIAL_MULTIPLIERS.getValue(dustCountForMultipliers).toFloat()
		
		with(ExplosionBuilder()) {
			if (dustTypes.contains(DustType.REDSTONE)) {
				this.knockbackMultiplier = Vec(2.0, 1.0, 2.0)
			}
			
			trigger(entity.world, entity, entity.posX, entity.posY + (entity.height * 0.5), entity.posZ, explosionStrength)
		}
		
		entity.remove()
	}
	
	fun serializeNBT(): NBTObjectList<String> {
		return NBTObjectList.of(dustTypes.map(DustType::key))
	}
}
