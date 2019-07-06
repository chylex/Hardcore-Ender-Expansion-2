package chylex.hee.game.world.territory
import chylex.hee.game.world.territory.descriptions.Territory_ForgottenTombs
import chylex.hee.game.world.territory.descriptions.Territory_TheHub
import chylex.hee.game.world.territory.generators.Generator_ForgottenTombs
import chylex.hee.game.world.territory.generators.Generator_TheHub
import chylex.hee.game.world.util.Size
import kotlin.math.abs

enum class TerritoryType(
	val title: String,
	val desc: ITerritoryDescription,
	val gen: ITerritoryGenerator,
	val chunks: Int,
	val height: IntRange
){
	THE_HUB(
		title  = "the_hub",
		desc   = Territory_TheHub,
		gen    = Generator_TheHub,
		chunks = 24,
		height = 64 until 192
	),
	
	FORGOTTEN_TOMBS(
		title  = "forgotten_tombs",
		desc   = Territory_ForgottenTombs,
		gen    = Generator_ForgottenTombs,
		chunks = 28,
		height = 64 until 256
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
	
	val size
		get() = Size(16 * chunks, 1 + height.last - height.first, 16 * chunks)
	
	val translationKey
		get() = "hee.territory.$title.name"
	
	val isSpawn
		get() = ordinal == 0
}
