package chylex.hee.system
import chylex.hee.HEE
import chylex.hee.game.block.BlockEndPortalOverride
import chylex.hee.game.world.provider.WorldProviderEndCustom
import net.minecraft.init.Blocks
import net.minecraft.world.DimensionType

object IntegrityCheck{
	var removedEnderChestRecipe: Boolean = false
	var removedPurpurRecipe: Boolean = false
	var removedEndRodRecipe: Boolean = false
	
	var removedChorusFruitRecipe: Boolean = false
	
	fun verify(){
		warnIfFalse(removedEnderChestRecipe, "could not remove vanilla Ender Chest recipe")
		warnIfFalse(removedPurpurRecipe, "could not remove vanilla Purpur Block recipe")
		warnIfFalse(removedEndRodRecipe, "could not remove vanilla End Rod recipe")
		
		warnIfFalse(removedChorusFruitRecipe, "could not remove vanilla Chorus Fruit smelting recipe")
		
		crashIfFalse(Blocks.END_PORTAL::class.java === BlockEndPortalOverride::class.java, "invalid End Portal block: ${Blocks.END_PORTAL::class.java}")
		
		crashIfFalse(DimensionType.THE_END.clazz.isAssignableFrom(WorldProviderEndCustom::class.java), "invalid End world provider: ${DimensionType.THE_END.clazz}")
	}
	
	// Utilities
	
	private fun warnIfFalse(value: Boolean, message: String){
		if (!value){
			failIntegrityCheck(message, Debug.enabled)
		}
	}
	
	private fun crashIfFalse(value: Boolean, message: String){
		if (!value){
			failIntegrityCheck(message, true)
		}
	}
	
	private fun failIntegrityCheck(message: String, crash: Boolean){
		HEE.log.error("[IntegrityCheck] $message")
		
		if (crash){
			throw IllegalStateException("Integrity check failed: $message")
		}
	}
}
