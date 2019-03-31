package chylex.hee.game.world.feature.energyshrine
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineCorridor_Corner
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineCorridor_Staircase_180
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineCorridor_Staircase_90
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineCorridor_Straight
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineCorridor_StraightLit
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineRoom_Main_Final
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineRoom_Main_Start
import chylex.hee.game.world.structure.IStructureDescription
import chylex.hee.game.world.structure.file.PaletteBuilder
import chylex.hee.game.world.structure.file.PaletteMappings
import chylex.hee.game.world.util.Size
import chylex.hee.init.ModBlocks
import chylex.hee.system.Resource
import net.minecraft.init.Blocks
import java.util.Random

object EnergyShrinePieces : IStructureDescription{
	override val STRUCTURE_SIZE = Size(128, 40, 128)
	
	override val STRUCTURE_BUILDER get() = TODO("not implemented")
	override val STRUCTURE_LOCATOR get() = TODO("not implemented")
	
	// Palette
	
	override val PALETTE
		get() = with(PaletteBuilder.Combined()){
			add("air", Blocks.AIR)
			add("bedrock", Blocks.BEDROCK)
			
			add("gloomrock", ModBlocks.GLOOMROCK)
			add("gloomrock.bricks", ModBlocks.GLOOMROCK_BRICKS)
			add("gloomrock.smooth", ModBlocks.GLOOMROCK_SMOOTH)
			add("gloomrock.white", ModBlocks.GLOOMROCK_SMOOTH_WHITE)
			
			add("slab.gloomrock.bricks.*", ModBlocks.GLOOMROCK_BRICK_SLAB, PaletteMappings.SLAB_HALF)
			add("slab.gloomrock.bricks.double", ModBlocks.GLOOMROCK_BRICK_DOUBLE_SLAB)
			add("slab.gloomrock.smooth.*", ModBlocks.GLOOMROCK_SMOOTH_SLAB, PaletteMappings.SLAB_HALF)
			add("slab.gloomrock.smooth.double", ModBlocks.GLOOMROCK_SMOOTH_DOUBLE_SLAB)
			
			add("stairs.gloomrock.bricks.*.*", ModBlocks.GLOOMROCK_BRICK_STAIRS, listOf(PaletteMappings.STAIR_FLIP, PaletteMappings.FACING_HORIZONTAL))
			add("stairs.gloomrock.smooth.*.*", ModBlocks.GLOOMROCK_SMOOTH_STAIRS, listOf(PaletteMappings.STAIR_FLIP, PaletteMappings.FACING_HORIZONTAL))
			
			add("gloomtorch.*", ModBlocks.GLOOMTORCH, PaletteMappings.FACING_ALL)
			
			build()
		}
	
	// Loot
	
	val LOOT_GENERAL = Resource.Custom("chests/energyshrine_general")
	val LOOT_BUILDING_MATERIALS = Resource.Custom("chests/energyshrine_building_materials")
	
	fun LOOT_PICK(rand: Random) =
		if (rand.nextInt(100) < 65)
			LOOT_GENERAL
		else
			LOOT_BUILDING_MATERIALS
	
	// Pieces (Corridors)
	
	private val PIECES_CORRIDOR_CORNER = weightedListOf(
		2 to EnergyShrineCorridor_Corner(lit = false),
		1 to EnergyShrineCorridor_Corner(lit = true)
	)
	
	private val PIECES_CORRIDOR_STAIRS = arrayOf(
		EnergyShrineCorridor_Staircase_90("corridor.staircase90.nbt"),
		EnergyShrineCorridor_Staircase_180("corridor.staircase180.nbt")
	)
	
	// Pieces (Rooms)
	
	val PIECES_START = arrayOf(
		EnergyShrineRoom_Main_Start("main.start.nbt")
	)
	
	val PIECES_END = arrayOf(
		EnergyShrineRoom_Main_Final("main.end.nbt")
	)
	
	// Pieces (All)
	
	override val ALL_PIECES
		get() = arrayOf(
			EnergyShrineCorridor_Straight(5),
			EnergyShrineCorridor_StraightLit(5),
			
			*PIECES_CORRIDOR_CORNER.values.toTypedArray(),
			*PIECES_CORRIDOR_STAIRS,
			
			*PIECES_START,
			*PIECES_END,
		)
}
