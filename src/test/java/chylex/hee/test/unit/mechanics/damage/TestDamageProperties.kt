package chylex.hee.test.unit.mechanics.damage

import chylex.hee.game.mechanics.damage.DamageProperties
import chylex.hee.game.mechanics.damage.DamageType
import chylex.hee.system.migration.EntitySnowball
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.util.DamageSource
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TestDamageProperties {
	private companion object {
		private fun DamageProperties.createDamageSource(triggeringSource: Entity? = null, remoteSource: Entity? = triggeringSource): DamageSource {
			return createDamageSource("", triggeringSource, remoteSource)
		}
	}
	
	@Nested inner class Types {
		@Test fun `'hasType' returns false if no types are set`() = with(DamageProperties()) {
			assertFalse(Reader().hasType(DamageType.PROJECTILE))
			assertFalse(Reader().hasType(DamageType.FIRE))
			assertFalse(Reader().hasType(DamageType.BLAST))
			assertFalse(Reader().hasType(DamageType.MAGIC))
		}
		
		@Test fun `setting 'PROJECTILE' type updates state correctly`() = with(DamageProperties()) {
			Writer().addType(DamageType.PROJECTILE)
			assertTrue(Reader().hasType(DamageType.PROJECTILE))
			assertTrue(createDamageSource().isProjectile)
		}
		
		@Test fun `setting 'FIRE' type updates state correctly`() = with(DamageProperties()) {
			Writer().addType(DamageType.FIRE)
			assertTrue(Reader().hasType(DamageType.FIRE))
			assertTrue(createDamageSource().isFireDamage)
		}
		
		@Test fun `setting 'BLAST' type updates state correctly`() = with(DamageProperties()) {
			Writer().addType(DamageType.BLAST)
			assertTrue(Reader().hasType(DamageType.BLAST))
			assertTrue(createDamageSource().isExplosion)
		}
		
		@Test fun `setting 'MAGIC' type updates state correctly`() = with(DamageProperties()) {
			Writer().addType(DamageType.MAGIC)
			assertTrue(Reader().hasType(DamageType.MAGIC))
			assertTrue(createDamageSource().isMagicDamage)
		}
		
		@Test fun `setting multiple types updates state correctly`() = with(DamageProperties()) {
			Writer().addType(DamageType.PROJECTILE)
			Writer().addType(DamageType.BLAST)
			
			assertTrue(Reader().hasType(DamageType.PROJECTILE))
			assertTrue(createDamageSource().isProjectile)
			
			assertFalse(Reader().hasType(DamageType.FIRE))
			assertFalse(createDamageSource().isFireDamage)
			
			assertTrue(Reader().hasType(DamageType.BLAST))
			assertTrue(createDamageSource().isExplosion)
			
			assertFalse(Reader().hasType(DamageType.MAGIC))
			assertFalse(createDamageSource().isMagicDamage)
		}
	}
	
	@Nested inner class Properties {
		@Test fun `default state is correct`() = with(DamageProperties()) {
			assertTrue(Reader().ignoreArmor)
			assertTrue(Reader().ignoreShield)
			assertTrue(createDamageSource().isUnblockable)
			
			assertFalse(Reader().dealCreative)
			assertFalse(createDamageSource().canHarmInCreative())
			
			assertTrue(createDamageSource().isDamageAbsolute)
			assertFalse(createDamageSource().isDifficultyScaled)
			@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
			assertNull(createDamageSource(EntitySnowball(EntityType.SNOWBALL, null)).damageLocation)
		}
		
		@Test fun `using 'setAllowArmor' updates state correctly`() = with(DamageProperties()) {
			Writer().setAllowArmor()
			
			assertFalse(Reader().ignoreArmor)
			assertTrue(Reader().ignoreShield)
			
			assertFalse(createDamageSource().isUnblockable)
			@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
			assertNull(createDamageSource(EntitySnowball(EntityType.SNOWBALL, null)).damageLocation)
		}
		
		@Test fun `using 'setAllowArmorAndShield' updates state correctly`() = with(DamageProperties()) {
			Writer().setAllowArmorAndShield()
			
			assertFalse(Reader().ignoreArmor)
			assertFalse(Reader().ignoreShield)
			
			assertFalse(createDamageSource().isUnblockable)
			@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
			assertNotNull(createDamageSource(EntitySnowball(EntityType.SNOWBALL, null)).damageLocation)
		}
		
		@Test fun `using 'setDealCreative' updates state correctly`() = with(DamageProperties()) {
			Writer().setDealCreative()
			assertTrue(Reader().dealCreative)
			assertTrue(createDamageSource().canHarmInCreative())
		}
	}
}
