package chylex.hee.game.world.territory
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.generation.TerritoryGenerationInfo
import chylex.hee.game.world.territory.descriptions.Territory_ArcaneConjunctions
import chylex.hee.game.world.territory.descriptions.Territory_ForgottenTombs
import chylex.hee.game.world.territory.descriptions.Territory_ObsidianTowers
import chylex.hee.game.world.territory.descriptions.Territory_TheHub
import chylex.hee.game.world.territory.generators.Generator_ArcaneConjunctions
import chylex.hee.game.world.territory.generators.Generator_ForgottenTombs
import chylex.hee.game.world.territory.generators.Generator_ObsidianTowers
import chylex.hee.game.world.territory.generators.Generator_TheHub
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.properties.TerritoryEnvironment
import chylex.hee.game.world.util.Size
import chylex.hee.system.util.color.IntColor.Companion.RGB
import java.util.Random
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
	),
	
	OBSIDIAN_TOWERS(
		title  = "obsidian_towers",
		desc   = Territory_ObsidianTowers,
		gen    = Generator_ObsidianTowers,
		chunks = 14,
		height = 100 until 180
	),
	
	ARCANE_CONJUNCTIONS(
		title  = "arcane_conjunctions",
		desc   = Territory_ArcaneConjunctions,
		gen    = Generator_ArcaneConjunctions,
		chunks = 30,
		height = 64 until 256
	),
	
	LOST_GARDEN(
		title  = "lost_garden",
		desc   = TerritoryDummy,
		gen    = GeneratorDummy,
		chunks = 45,
		height = 128 until 256
	),
	
	ENDER_CITY(
		title  = "ender_city",
		desc   = TerritoryDummy,
		gen    = GeneratorDummy,
		chunks = 52,
		height = 128 until 256
	),
	
	HOSTILE_PASS(
		title  = "hostile_pass",
		desc   = TerritoryDummy,
		gen    = GeneratorDummy,
		chunks = 38,
		height = 128 until 256
	),
	
	WARDED_MINES(
		title  = "warded_mines",
		desc   = TerritoryDummy,
		gen    = GeneratorDummy,
		chunks = 27,
		height = 0 until 256
	),
	
	ETERNAL_MISTS(
		title  = "eternal_mists",
		desc   = TerritoryDummy,
		gen    = GeneratorDummy,
		chunks = 92,
		height = 128 until 256
	),
	
	CURSED_LIBRARY(
		title  = "cursed_library",
		desc   = TerritoryDummy,
		gen    = GeneratorDummy,
		chunks = 11,
		height = 64 until 256
	),
	
	DRAGON_LAIR(
		title  = "dragon_lair",
		desc   = TerritoryDummy,
		gen    = GeneratorDummy,
		chunks = 34,
		height = 128 until 256
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
		
		// TODO remove once no longer necessary
		
		private object TerritoryDummy : ITerritoryDescription{
			override val colors = object : TerritoryColors(){
				override val tokenTop    = RGB(255u)
				override val tokenBottom = RGB(255u)
				
				override val portalSeed = 0L
				
				override fun nextPortalColor(rand: Random, color: FloatArray){
					color.fill(1F)
				}
			}
			
			override val environment = object : TerritoryEnvironment(){
				override val fogColor = RGB(0u).asVec
				override val fogDensity = 0F
				
				override val voidRadiusMpXZ = 1F
				override val voidRadiusMpY = 1F
			}
		}
		
		private object GeneratorDummy : ITerritoryGenerator{
			override val segmentSize = Size(1)
			
			override fun provide(world: SegmentedWorld): TerritoryGenerationInfo{
				return TerritoryGenerationInfo(world.worldSize.centerPos)
			}
		}
	}
	
	val size
		get() = Size(16 * chunks, 1 + height.last - height.first, 16 * chunks)
	
	val translationKey
		get() = "hee.territory.$title.name"
	
	val isSpawn
		get() = ordinal == 0
}
