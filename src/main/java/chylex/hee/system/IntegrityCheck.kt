package chylex.hee.system
import chylex.hee.HEE
import chylex.hee.game.block.BlockBrewingStandCustom
import chylex.hee.game.block.BlockEndPortalOverride
import chylex.hee.game.world.WorldProviderEndCustom
import chylex.hee.system.migration.vanilla.Blocks
import net.minecraft.world.dimension.DimensionType

object IntegrityCheck{
	var removedEnderChestRecipe: Boolean = false
	var removedPurpurRecipe: Boolean = false
	var removedEndRodRecipe: Boolean = false
	var removedEyeOfEnderRecipe: Boolean = false
	
	var removedChorusFruitRecipe: Boolean = false
	
	fun verify(){
		warnIfFalse(removedEnderChestRecipe, "could not remove vanilla Ender Chest recipe")
		warnIfFalse(removedPurpurRecipe, "could not remove vanilla Purpur Block recipe")
		warnIfFalse(removedEndRodRecipe, "could not remove vanilla End Rod recipe")
		warnIfFalse(removedEyeOfEnderRecipe, "could not remove vanilla Eye of Ender recipe")
		
		warnIfFalse(removedChorusFruitRecipe, "could not remove vanilla Chorus Fruit smelting recipe")
		
		crashIfFalse(Blocks.END_PORTAL::class.java === BlockEndPortalOverride::class.java, "invalid End Portal block: ${Blocks.END_PORTAL::class.java}")
		crashIfFalse(Blocks.BREWING_STAND::class.java === BlockBrewingStandCustom.Override::class.java, "invalid Brewing Stand block: ${Blocks.BREWING_STAND::class.java}")
		
		crashIfFalse(DimensionType.THE_END.directory == WorldProviderEndCustom.SAVE_FOLDER, "invalid End dimension save directory: ${DimensionType.THE_END.directory}")
		crashIfFalse(DimensionType.THE_END.factory === WorldProviderEndCustom.CONSTRUCTOR, "invalid End dimension factory: ${DimensionType.THE_END.factory}")
		crashIfFalse(DimensionType.THE_END.hasSkyLight, "invalid End dimension property: hasSkyLight != true")
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
		// UPDATE check(!crash){ "Integrity check failed: $message" }
	}
}
