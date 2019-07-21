package chylex.hee.game.world.feature.obsidiantower
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerLevel_General
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerLevel_Top
import chylex.hee.game.world.structure.IStructureDescription
import chylex.hee.game.world.structure.IStructureDescription.Companion.NULL_LOCATOR
import chylex.hee.game.world.structure.file.PaletteBuilder
import chylex.hee.game.world.structure.file.PaletteMappings
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.game.world.util.Size
import chylex.hee.init.ModBlocks
import net.minecraft.init.Blocks

object ObsidianTowerPieces : IStructureDescription{
	fun calculateStructureSize(floors: Int) = Size(
		PIECES_BASE.map { it.size.x }.max()!!,
		PIECES_BASE.sumBy { it.size.y } + ((floors - 2) * PIECE_LEVEL_MIDDLE.size.y),
		PIECES_BASE.map { it.size.z }.max()!!
	)
	
	// Palette
	
	override val PALETTE = with(PaletteBuilder.Combined()){
		add("air", Blocks.AIR)
		add("obsidian", Blocks.OBSIDIAN)
		add("glowstone", Blocks.GLOWSTONE)
		
		add("obsidian.smooth", ModBlocks.OBSIDIAN_SMOOTH)
		add("obsidian.chiseled", ModBlocks.OBSIDIAN_CHISELED)
		add("obsidian.pillar.*", ModBlocks.OBSIDIAN_PILLAR, PaletteMappings.FACING_AXIS)
		
		add("obsidian.smooth.lit", ModBlocks.OBSIDIAN_SMOOTH_LIT)
		add("obsidian.chiseled.lit", ModBlocks.OBSIDIAN_CHISELED_LIT)
		add("obsidian.pillar.lit.*", ModBlocks.OBSIDIAN_PILLAR_LIT, PaletteMappings.FACING_AXIS)
		
		add("stairs.obsidian.*.*", ModBlocks.OBSIDIAN_STAIRS, listOf(PaletteMappings.STAIR_FLIP, PaletteMappings.FACING_HORIZONTAL))
		
		add("ladder.*", Blocks.LADDER, PaletteMappings.FACING_HORIZONTAL)
		
		build()
	}
	
	// Pieces
	
	val PIECE_LEVEL_BOTTOM = ObsidianTowerLevel_General("level.bottom.nbt")
	val PIECE_LEVEL_MIDDLE = ObsidianTowerLevel_General("level.middle.nbt")
	val PIECE_LEVEL_TOP_BOSS = ObsidianTowerLevel_Top.Boss("level.top.nbt")
	
	fun PIECE_LEVEL_TOP_TOKEN(tokenType: TokenType, territoryType: TerritoryType): ObsidianTowerLevel_Top{
		return ObsidianTowerLevel_Top.Token("level.top.nbt", tokenType, territoryType)
	}
	
	private val PIECES_BASE = arrayOf(
		PIECE_LEVEL_BOTTOM,
		PIECE_LEVEL_MIDDLE,
		PIECE_LEVEL_TOP_BOSS
	)
	
	override val ALL_PIECES
		get() = arrayOf(
			*PIECES_BASE
		)
	
	// Description
	
	private const val DEFAULT_FLOORS = 5
	
	override val STRUCTURE_SIZE = calculateStructureSize(DEFAULT_FLOORS)
	
	override val STRUCTURE_BUILDER = ObsidianTowerBuilder(DEFAULT_FLOORS, PIECE_LEVEL_TOP_BOSS)
	override val STRUCTURE_LOCATOR = NULL_LOCATOR
}
