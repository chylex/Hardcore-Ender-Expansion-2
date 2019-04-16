package chylex.hee.game.world.territory
import chylex.hee.game.world.territory.descriptions.Territory_TheHub
import kotlin.math.abs

enum class TerritoryType(
	val title: String,
	val desc: ITerritoryDescription,
	val chunks: Int,
	val height: IntRange
){
	THE_HUB(
		title  = "the_hub",
		desc   = Territory_TheHub,
		chunks = 24,
		height = 0 until 128
	);
	
	companion object{
		const val FALLBACK_TRANSLATION_KEY = "hee.territory.fallback.name"
		const val CHUNK_MARGIN = 64 // must be a multiple of 2
		
		val ALL = values()
		val CHUNK_X_OFFSET = -(ALL[0].chunks / 2)
		
		fun fromX(x: Int): TerritoryType?{
			val adjustedX = if (x < 0) abs(x + 1) else x
			var testX = adjustedX + (ALL[0].chunks + CHUNK_MARGIN) * 8
			
			for(territory in ALL){
				val totalBlocks = (territory.chunks + CHUNK_MARGIN) * 16
				
				if (testX < totalBlocks){
					return territory
				}
				
				testX -= totalBlocks
			}
			
			return null
		}
		
		fun fromTitle(title: String): TerritoryType?{
			return ALL.find { it.title == title }
		}
	}
	
	val translationKey
		get() = "hee.territory.$title.name"
	
	val canGenerate
		get() = ordinal > 0
}
