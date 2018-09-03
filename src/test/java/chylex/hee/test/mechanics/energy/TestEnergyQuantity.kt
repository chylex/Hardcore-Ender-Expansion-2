package chylex.hee.test.mechanics.energy
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Floating
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Internal
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TestEnergyQuantity{
	@Nested inner class Equality{
		@Test fun `same representations of same amounts are equal`(){
			assertEquals(Internal(25_000_000), Internal(25_000_000))
			assertEquals(Floating(25F), Floating(25F))
			assertEquals(Units(500), Units(500))
		}
		
		@Test fun `different representations of same amounts are equal`(){
			assertEquals(Internal(25_000_000), Floating(25F))
			assertEquals(Internal(25_000_000), Units(500))
			
			assertEquals(Floating(25F), Internal(25_000_000))
			assertEquals(Floating(25F), Units(500))
			
			assertEquals(Units(500), Internal(25_000_000))
			assertEquals(Units(500), Floating(25F))
		}
		
		@Test fun `same representations of different amounts are not equal`(){
			assertNotEquals(Internal(25_000_000), Internal(25_050_000))
			assertNotEquals(Floating(25F), Floating(25.05F))
			assertNotEquals(Units(500), Units(501))
		}
		
		@Test fun `different representations of different amounts are not equal`(){
			assertNotEquals(Internal(25_000_000), Floating(25.05F))
			assertNotEquals(Internal(25_000_000), Units(501))
			
			assertNotEquals(Floating(25F), Internal(25_050_000))
			assertNotEquals(Floating(25F), Units(501))
			
			assertNotEquals(Units(500), Internal(25_050_000))
			assertNotEquals(Units(500), Floating(25.05F))
		}
	}
	
	@Nested inner class Comparisons{
		@Test fun `comparing amounts using same representations works`(){
			assertTrue(Internal(25_000_000) < Internal(25_050_000))
			assertTrue(Floating(25F) < Floating(25.05F))
			assertTrue(Units(500) < Units(501))
			
			assertFalse(Internal(25_000_000) > Internal(25_050_000))
			assertFalse(Floating(25F) > Floating(25.05F))
			assertFalse(Units(500) > Units(501))
		}
		
		@Test fun `comparing amounts using different representations works`(){
			assertTrue(Internal(25_000_000) < Floating(25.05F))
			assertTrue(Internal(25_000_000) < Units(501))
			
			assertTrue(Floating(25F) < Internal(25_050_000))
			assertTrue(Floating(25F) < Units(501))
			
			assertTrue(Units(500) < Internal(25_050_000))
			assertTrue(Units(500) < Floating(25.05F))
			
			assertFalse(Internal(25_000_000) > Floating(25.05F))
			assertFalse(Internal(25_000_000) > Units(501))
			
			assertFalse(Floating(25F) > Internal(25_050_000))
			assertFalse(Floating(25F) > Units(501))
			
			assertFalse(Units(500) > Internal(25_050_000))
			assertFalse(Units(500) > Floating(25.05F))
		}
	}
	
	@Nested inner class ConstructorValidation{
		@Test fun `values below minimum are clamped correctly`(){
			assertEquals(Internal(0), Internal(-1))
			assertEquals(Floating(0F), Floating(-1F))
			assertEquals(Units(0), Units(-1))
		}
		
		@Test fun `values above maximum are clamped correctly`(){
			assertEquals(Internal(100_000_000), Internal(100_000_001))
			assertEquals(Floating(100F), Floating(100.01F))
			assertEquals(Units(2000), Units(2001))
		}
	}
	
	@Nested inner class Conversions{
		@Test fun `conversion to 'Internal' returns correct value`(){
			assertEquals(1_100_000, Floating(1.1F).internal.value)
			assertEquals(1_100_000, Units(22).internal.value)
		}
		
		@Test fun `conversion to 'Floating' returns correct value`(){
			assertEquals(1.1F, Internal(1_100_000).floating.value)
			assertEquals(1.1F, Units(22).floating.value)
		}
		
		@Test fun `conversion to 'Units' returns correct value`(){
			assertEquals(22, Internal(1_100_000).units.value)
			assertEquals(22, Floating(1.1F).units.value)
		}
		
		@Test fun `conversion to 'Units' rounds down if needed`(){
			assertEquals(22, Internal(1_109_999).units.value)
			assertEquals(22, Floating(1.109999F).units.value)
		}
	}
	
	@Nested inner class ArithmeticOperations{
		@Test fun `addition and subtraction with same representations have correct results`(){
			assertEquals(Internal(40_000_000), Internal(30_000_000) + Internal(10_000_000))
			assertEquals(Internal(20_000_000), Internal(30_000_000) - Internal(10_000_000))
			
			assertEquals(Floating(40F), Floating(30F) + Floating(10F))
			assertEquals(Floating(20F), Floating(30F) - Floating(10F))
			
			assertEquals(Units(800), Units(600) + Units(200))
			assertEquals(Units(400), Units(600) - Units(200))
		}
		
		@Test fun `addition and subtraction with different representations have correct results`(){
			assertEquals(Internal(40_000_000), Internal(30_000_000) + Floating(10F))
			assertEquals(Internal(20_000_000), Internal(30_000_000) - Floating(10F))
			assertEquals(Internal(40_000_000), Internal(30_000_000) + Units(200))
			assertEquals(Internal(20_000_000), Internal(30_000_000) - Units(200))
			
			assertEquals(Internal(40_000_000), Floating(30F) + Internal(10_000_000))
			assertEquals(Internal(20_000_000), Floating(30F) - Internal(10_000_000))
			assertEquals(Internal(40_000_000), Floating(30F) + Units(200))
			assertEquals(Internal(20_000_000), Floating(30F) - Units(200))
			
			assertEquals(Internal(40_000_000), Units(600) + Internal(10_000_000))
			assertEquals(Internal(20_000_000), Units(600) - Internal(10_000_000))
			assertEquals(Internal(40_000_000), Units(600) + Floating(10F))
			assertEquals(Internal(20_000_000), Units(600) - Floating(10F))
		}
		
		@Test fun `addition and subtraction with 'Units' preserve 'Internal' and 'Floating' precision`(){
			assertEquals(Internal(5_105_000), Internal(5_055_000) + Units(1))
			assertEquals(Internal(5_005_000), Internal(5_055_000) - Units(1))
			
			assertEquals(Floating(5.105F), Floating(5.055F) + Units(1))
			assertEquals(Floating(5.005F), Floating(5.055F) - Units(1))
		}
		
		@Test fun `scalar multiplication with same representations has correct results`(){
			assertEquals(Internal(5_000_000), Internal(2_500_000) * 2F)
			assertEquals(Internal(1_250_000), Internal(2_500_000) * 0.5F)
			
			assertEquals(Floating(5F), Floating(2.5F) * 2F)
			assertEquals(Floating(1.25F), Floating(2.5F) * 0.5F)
			
			assertEquals(Units(100), Units(50) * 2F)
			assertEquals(Units(25), Units(50) * 0.5F)
		}
	}
}
