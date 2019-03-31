package chylex.hee.game.world.feature.energyshrine
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
	
	
	// Pieces (All)
	
	override val ALL_PIECES
		get() = arrayOf(
		)
}
