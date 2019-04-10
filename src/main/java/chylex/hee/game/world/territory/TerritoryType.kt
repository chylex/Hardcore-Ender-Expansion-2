package chylex.hee.game.world.territory
import kotlin.math.abs

enum class TerritoryType(
	val title: String,
	val desc: ITerritoryDescription,
	val chunks: Int,
	val height: IntRange
){
	// TODO
	TEST1(key = "test1", chunks = 24, height = 0 until 128),
	TEST2(key = "test2", chunks = 1, height = 0 until 128),
	TEST3(key = "test3", chunks = 5, height = 0 until 128),
	TEST4(key = "test4", chunks = 6, height = 0 until 128);
	;
	
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
	}
	
	val translationKey
		get() = "hee.territory.$title.name"
	
	val canGenerate
		get() = ordinal > 0
}
