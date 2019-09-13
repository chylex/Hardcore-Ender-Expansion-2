package chylex.hee.game.world.feature.tombdungeon
import chylex.hee.game.block.BlockGraveDirt
import chylex.hee.game.world.structure.IBlockPicker.Weighted.Companion.Weighted
import chylex.hee.game.world.structure.IStructureDescription
import chylex.hee.game.world.structure.IStructureDescription.Companion.NULL_LOCATOR
import chylex.hee.game.world.structure.file.PaletteBuilder
import chylex.hee.game.world.structure.file.PaletteMappings
import chylex.hee.game.world.structure.piece.StructurePiece
import chylex.hee.game.world.util.Size
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.with
import chylex.hee.system.util.withFacing
import net.minecraft.block.BlockSlab
import net.minecraft.block.BlockStairs
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST

object TombDungeonPieces : IStructureDescription{
	override val STRUCTURE_SIZE = Size(300, 110, 300)
	
	override val STRUCTURE_BUILDER = TombDungeonBuilder
	override val STRUCTURE_LOCATOR = NULL_LOCATOR
	
	// Palette
	
	val PALETTE_ENTRY_PLAIN_WALL_CEILING = Weighted(
		840 to ModBlocks.DUSTY_STONE_BRICKS,
		125 to ModBlocks.DUSTY_STONE,
		 35 to ModBlocks.DUSTY_STONE_CRACKED_BRICKS
	)
	
	val PALETTE_ENTRY_FANCY_WALL = Weighted(
		94 to ModBlocks.DUSTY_STONE_BRICKS,
		 6 to ModBlocks.DUSTY_STONE_CRACKED_BRICKS
	)
	
	val PALETTE_ENTRY_FANCY_CEILING = Weighted(
		860 to ModBlocks.DUSTY_STONE_BRICKS.defaultState,
		 82 to ModBlocks.DUSTY_STONE_CRACKED_BRICKS.defaultState,
		 30 to ModBlocks.DUSTY_STONE_BRICK_SLAB.with(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP),
		  7 to ModBlocks.DUSTY_STONE_BRICK_STAIRS.with(BlockStairs.HALF, BlockStairs.EnumHalf.TOP).withFacing(NORTH),
		  7 to ModBlocks.DUSTY_STONE_BRICK_STAIRS.with(BlockStairs.HALF, BlockStairs.EnumHalf.TOP).withFacing(SOUTH),
		  7 to ModBlocks.DUSTY_STONE_BRICK_STAIRS.with(BlockStairs.HALF, BlockStairs.EnumHalf.TOP).withFacing(EAST),
		  7 to ModBlocks.DUSTY_STONE_BRICK_STAIRS.with(BlockStairs.HALF, BlockStairs.EnumHalf.TOP).withFacing(WEST)
	)
	
	private val PALETTE_ENTRY_PLAIN_GRAVE = Weighted(ModBlocks.GRAVE_DIRT, BlockGraveDirt.TYPE, listOf(
		74 to BlockGraveDirt.Type.PLAIN,
		18 to BlockGraveDirt.Type.SPIDERLING,
		 8 to BlockGraveDirt.Type.LOOT
	))
	
	private val PALETTE_ENTRY_FANCY_GRAVE = Weighted(ModBlocks.GRAVE_DIRT, BlockGraveDirt.TYPE, listOf(
		69 to BlockGraveDirt.Type.PLAIN,
		27 to BlockGraveDirt.Type.LOOT,
		 4 to BlockGraveDirt.Type.SPIDERLING
	))
	
	private fun generateCommonPalette() = PaletteBuilder.Combined().apply {
		add("air", Blocks.AIR)
		add("dustystone", ModBlocks.DUSTY_STONE)
		add("dustystone.bricks", ModBlocks.DUSTY_STONE_BRICKS)
		
		add("slab.dustystonebrick.*", ModBlocks.DUSTY_STONE_BRICK_SLAB, PaletteMappings.SLAB_HALF)
		add("stairs.dustystonebrick.*.*", ModBlocks.DUSTY_STONE_BRICK_STAIRS, PaletteMappings.STAIR_MAPPING_LIST)
		
		with(forDevelopment){
			add("dustystone.wall", Blocks.LAPIS_BLOCK)
			add("dustystone.ceiling", Blocks.COAL_BLOCK)
			add("gravedirt", ModBlocks.GRAVE_DIRT)
		}
	}
	
	override val PALETTE = with(generateCommonPalette()){
		with(forGeneration){
			add("dustystone.wall", PALETTE_ENTRY_PLAIN_WALL_CEILING)
			add("dustystone.ceiling", PALETTE_ENTRY_PLAIN_WALL_CEILING)
			add("gravedirt", PALETTE_ENTRY_PLAIN_GRAVE)
		}
		
		build()
	}
	
	val PALETTE_FANCY = with(generateCommonPalette()){
		with(forGeneration){
			add("dustystone.wall", PALETTE_ENTRY_FANCY_WALL)
			add("dustystone.ceiling", PALETTE_ENTRY_FANCY_CEILING)
			add("gravedirt", PALETTE_ENTRY_FANCY_GRAVE)
		}
		
		build()
	}
	
	// Pieces
	
	override val ALL_PIECES
		get() = arrayOf<StructurePiece<*>>()
}
