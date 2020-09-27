package chylex.hee.game.world.feature.tombdungeon
import chylex.hee.game.block.BlockGraveDirt
import chylex.hee.game.block.with
import chylex.hee.game.block.withFacing
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonAbstractPiece
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonCorridor_Intersection
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonCorridor_Stairs
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonCorridor_Straight
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonCorridor_StraightCrumbling
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonCorridor_StraightTombs
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonDeadEnd_Tomb
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_End
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Main_BranchingHall
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Main_CrossroadRooms
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Main_Maze
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Main_Pillars
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Main_SplitRooms
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Side_Arches
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Side_Crossroads
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Side_CrossroadsLounge
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Side_Fountain
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Side_Shelves
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Tomb
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Tomb_Mass
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Tomb_MassSpacious
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Tomb_MultiDeep
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Tomb_MultiSpacious
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Tomb_Single
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonSecret
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonSecret_CornerShelf
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonSecret_LootChest
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonSecret_Rubble
import chylex.hee.game.world.generation.IBlockPicker.Weighted.Companion.Weighted
import chylex.hee.game.world.math.Size
import chylex.hee.game.world.structure.IStructureDescription
import chylex.hee.game.world.structure.IStructureDescription.Companion.NULL_LOCATOR
import chylex.hee.game.world.structure.palette.PaletteBuilder
import chylex.hee.game.world.structure.palette.PaletteMappings
import chylex.hee.init.ModBlocks
import chylex.hee.system.collection.WeightedList
import chylex.hee.system.collection.WeightedList.Companion.weightedListOf
import chylex.hee.system.migration.BlockSlab
import chylex.hee.system.migration.BlockStairs
import chylex.hee.system.migration.Blocks
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItem
import chylex.hee.system.random.removeItem
import net.minecraft.state.properties.Half
import net.minecraft.state.properties.SlabType
import java.util.Random
import kotlin.math.max
import kotlin.math.min

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
		878 to ModBlocks.DUSTY_STONE_BRICKS.defaultState,
		100 to ModBlocks.DUSTY_STONE_CRACKED_BRICKS.defaultState,
		 10 to ModBlocks.DUSTY_STONE_BRICK_SLAB.with(BlockSlab.TYPE, SlabType.TOP),
		  3 to ModBlocks.DUSTY_STONE_BRICK_STAIRS.with(BlockStairs.HALF, Half.TOP).withFacing(NORTH),
		  3 to ModBlocks.DUSTY_STONE_BRICK_STAIRS.with(BlockStairs.HALF, Half.TOP).withFacing(SOUTH),
		  3 to ModBlocks.DUSTY_STONE_BRICK_STAIRS.with(BlockStairs.HALF, Half.TOP).withFacing(EAST),
		  3 to ModBlocks.DUSTY_STONE_BRICK_STAIRS.with(BlockStairs.HALF, Half.TOP).withFacing(WEST)
	)
	
	val PALETTE_ENTRY_PLAIN_GRAVE = Weighted(
		74 to ModBlocks.GRAVE_DIRT_PLAIN,
		18 to ModBlocks.GRAVE_DIRT_SPIDERLING,
		 8 to ModBlocks.GRAVE_DIRT_LOOT
	)
	
	val PALETTE_ENTRY_FANCY_GRAVE = Weighted(
		69 to ModBlocks.GRAVE_DIRT_PLAIN,
		27 to ModBlocks.GRAVE_DIRT_LOOT,
		 4 to ModBlocks.GRAVE_DIRT_SPIDERLING
	)
	
	private val PALETTE_ENTRY_PLAIN_DECORATION = Weighted(
		96 to ModBlocks.DUSTY_STONE_BRICKS,
		 3 to ModBlocks.DUSTY_STONE_CRACKED_BRICKS,
		 1 to ModBlocks.DUSTY_STONE_DECORATION
	)
	
	private val PALETTE_ENTRY_FANCY_DECORATION = Weighted(
		24 to ModBlocks.DUSTY_STONE_DECORATION,
		74 to ModBlocks.DUSTY_STONE_BRICKS,
		 4 to ModBlocks.DUSTY_STONE_CRACKED_BRICKS
	)
	
	private fun generateCommonPalette() = PaletteBuilder.Combined().apply {
		add("air", Blocks.AIR)
		add("dustystone", ModBlocks.DUSTY_STONE)
		add("dustystone.bricks", ModBlocks.DUSTY_STONE_BRICKS)
		add("dustystone.decoration", ModBlocks.DUSTY_STONE_DECORATION)
		
		add("slab.dustystonebrick.*", ModBlocks.DUSTY_STONE_BRICK_SLAB, PaletteMappings.SLAB_TYPE)
		add("stairs.dustystonebrick.*.*.*", ModBlocks.DUSTY_STONE_BRICK_STAIRS, PaletteMappings.STAIR_MAPPING_LIST)
		
		add("torch", Blocks.TORCH)
		add("torch.*", Blocks.WALL_TORCH, PaletteMappings.FACING_HORIZONTAL)
		add("redstonetorch", Blocks.REDSTONE_TORCH)
		add("redstonetorch.*", Blocks.REDSTONE_WALL_TORCH, PaletteMappings.FACING_HORIZONTAL)
		
		add("voidportal.frame", ModBlocks.VOID_PORTAL_FRAME)
		
		with(forDevelopment){
			add("dustystone.wall", Blocks.LAPIS_BLOCK)
			add("dustystone.ceiling", Blocks.COAL_BLOCK)
			add("dustystone.decoration.random", Blocks.DIAMOND_BLOCK)
			add("gravedirt", ModBlocks.GRAVE_DIRT_PLAIN.with(BlockGraveDirt.FULL, false))
			add("gravedirt.full", ModBlocks.GRAVE_DIRT_PLAIN.with(BlockGraveDirt.FULL, true))
		}
	}
	
	override val PALETTE = with(generateCommonPalette()){
		with(forGeneration){
			add("dustystone.wall", PALETTE_ENTRY_PLAIN_WALL_CEILING)
			add("dustystone.ceiling", PALETTE_ENTRY_PLAIN_WALL_CEILING)
			add("dustystone.decoration.random", PALETTE_ENTRY_PLAIN_DECORATION)
			add("gravedirt", PALETTE_ENTRY_PLAIN_GRAVE.thenSetting(BlockGraveDirt.FULL, false))
			add("gravedirt.full", PALETTE_ENTRY_PLAIN_GRAVE.thenSetting(BlockGraveDirt.FULL, true))
		}
		
		build()
	}
	
	val PALETTE_FANCY = with(generateCommonPalette()){
		with(forGeneration){
			add("dustystone.wall", PALETTE_ENTRY_FANCY_WALL)
			add("dustystone.ceiling", PALETTE_ENTRY_FANCY_CEILING)
			add("dustystone.decoration.random", PALETTE_ENTRY_FANCY_DECORATION)
			add("gravedirt", PALETTE_ENTRY_FANCY_GRAVE.thenSetting(BlockGraveDirt.FULL, false))
			add("gravedirt.full", PALETTE_ENTRY_FANCY_GRAVE.thenSetting(BlockGraveDirt.FULL, true))
		}
		
		build()
	}
	
	// Pieces
	
	val PIECE_ROOM_END = TombDungeonRoom_End("main.end.nbt")
	
	private val PIECES_STAIRS = arrayOf(
		TombDungeonCorridor_Stairs.Start("corridor.stairs.start.nbt"),
		TombDungeonCorridor_Stairs.Middle("corridor.stairs.middle.nbt"),
		TombDungeonCorridor_Stairs.End("corridor.stairs.end.nbt")
	)
	
	fun PIECES_STAIRCASE(middle: Int): List<TombDungeonCorridor_Stairs>{
		return listOf(PIECES_STAIRS[0], *Array(middle){ PIECES_STAIRS[1] }, PIECES_STAIRS[2])
	}
	
	fun PIECES_MAIN_CORRIDOR(rand: Random, level: TombDungeonLevel, cornerCount: Int, crumblingCorridorCount: Int): MutableList<TombDungeonAbstractPiece>{
		return mutableListOf<TombDungeonAbstractPiece>().apply {
			val isFancy = level.isFancy
			var lengthRemaining = level.getMainCorridorLength(rand)
			
			for(attempt in 0..2){
				val (sideTombSpacing, sideTombGenerator) = level.pickTombGeneratorAndSpacing(rand)
				
				val tombsPerSide = rand.nextInt(6, 7) - attempt - min(2, (sideTombSpacing - 3) / 3)
				val tombConfiguration = TombDungeonCorridor_StraightTombs.Configuration.random(rand)
				
				val corridor = TombDungeonCorridor_StraightTombs(sideTombSpacing, tombConfiguration, tombsPerSide, sideTombGenerator, isFancy)
				val length = corridor.size.z
				
				if (length <= (lengthRemaining * 4) / 9){
					add(corridor)
					lengthRemaining -= length
					break
				}
			}
			
			while(lengthRemaining > 0){
				var nextLength = level.nextMainCorridorSplitLength(rand, lengthRemaining)
				lengthRemaining -= nextLength
				
				val (sideTombSpacing, sideTombGenerator) = level.pickTombGeneratorAndSpacing(rand)
				
				if (nextLength >= sideTombSpacing && rand.nextInt(6) != 0){
					val tombsPerSide = max(1, nextLength / sideTombSpacing)
					val tombConfiguration = TombDungeonCorridor_StraightTombs.Configuration.random(rand)
					
					val corridor = TombDungeonCorridor_StraightTombs(sideTombSpacing, tombConfiguration, tombsPerSide, sideTombGenerator, isFancy)
					
					add(corridor)
					nextLength -= corridor.size.z
				}
				
				if (nextLength > 0){
					add(TombDungeonCorridor_Straight(nextLength, isFancy))
				}
			}
			
			repeat(cornerCount){
				add(TombDungeonCorridor_Intersection(isFancy))
			}
			
			repeat(crumblingCorridorCount){
				if (level === TombDungeonLevel.FIRST){
					add(TombDungeonCorridor_StraightCrumbling(length = rand.nextInt(7, 8), fallHeight = rand.nextInt(4, 6), isFancy = isFancy))
				}
				else{
					add(TombDungeonCorridor_StraightCrumbling(length = rand.nextInt(5, 13), fallHeight = rand.nextInt(14, 21), isFancy = isFancy))
				}
			}
			
			shuffle(rand)
		}
	}
	
	fun PIECES_SIDE_CORRIDOR(rand: Random, level: TombDungeonLevel, crumblingCorridorCount: Int, endRoom: TombDungeonAbstractPiece?): MutableList<TombDungeonAbstractPiece>{
		return mutableListOf<TombDungeonAbstractPiece>().apply {
			val isFancy = level.isFancy
			
			repeat(rand.nextInt(0, rand.nextInt(1, 6))){
				val length = rand.nextInt(1, 6)
				
				if (length >= 5 && rand.nextBoolean()){
					val (sideTombSpacing, sideTombGenerator) = level.pickTombGeneratorAndSpacing(rand)
					add(TombDungeonCorridor_StraightTombs(sideTombSpacing, rand.nextItem(), rand.nextInt(1, 2), sideTombGenerator, isFancy))
				}
				else{
					add(TombDungeonCorridor_Straight(length, isFancy))
				}
			}
			
			repeat(rand.nextInt(0, rand.nextInt(1, 3))){
				add(TombDungeonCorridor_Intersection(isFancy))
			}
			
			repeat(crumblingCorridorCount){
				add(TombDungeonCorridor_StraightCrumbling(length = rand.nextInt(5, 16), fallHeight = rand.nextInt(14, 21), isFancy = isFancy))
			}
			
			shuffle(rand)
			
			if (endRoom != null){
				add(endRoom)
			}
			else{
				add(TombDungeonDeadEnd_Tomb(rand.nextInt(2, 4), level.pickTombGeneratorAndSpacing(rand).second, isFancy))
			}
		}
	}
	
	fun PIECES_SECRET(rand: Random, amount: Int): MutableList<TombDungeonSecret>{
		return mutableListOf<TombDungeonSecret>().apply {
			addAll(PIECES_SECRET_GUARANTEED)
			
			val randomPieces = mutableListOf<TombDungeonSecret>()
			
			while(randomPieces.size < amount){
				randomPieces.addAll(PIECES_SECRET_RANDOM)
			}
			
			while(size < amount){
				add(rand.removeItem(randomPieces))
			}
		}
	}
	
	private val PIECES_SECRET_GUARANTEED = arrayOf(
		TombDungeonSecret_LootChest("secret.loot_chest.nbt")
	)
	
	private val PIECES_SECRET_RANDOM = arrayOf(
		TombDungeonSecret_CornerShelf("secret.corner_shelf.nbt"),
		TombDungeonSecret_Rubble("secret.rubble1.nbt"),
		TombDungeonSecret_Rubble("secret.rubble2.nbt")
	)
	
	val PIECES_MAIN_ROOMS_NONFANCY = arrayOf(
		TombDungeonRoom_Main_Pillars("main.pillars.nbt", isFancy = false),
		TombDungeonRoom_Main_Maze("main.maze.nbt", isFancy = false)
	)
	
	val PIECES_MAIN_ROOMS_FANCY = arrayOf(
		TombDungeonRoom_Main_BranchingHall("main.branching_hall.nbt", isFancy = true),
		TombDungeonRoom_Main_CrossroadRooms("main.crossroad_rooms.nbt", isFancy = true),
		TombDungeonRoom_Main_SplitRooms("main.split_rooms.nbt", isFancy = true)
	)
	
	val PIECES_SIDE_ROOMS_NONFANCY = arrayOf(
		TombDungeonRoom_Side_Shelves("side.shelves.nbt", isFancy = false),
		TombDungeonRoom_Side_Crossroads("side.crossroads.nbt", isFancy = false),
		TombDungeonRoom_Side_Fountain("side.fountain.nbt", isFancy = false)
	)
	
	val PIECES_SIDE_ROOMS_FANCY = arrayOf(
		TombDungeonRoom_Side_CrossroadsLounge("side.crossroads_lounge.nbt", isFancy = true),
		TombDungeonRoom_Side_Arches("side.arches.nbt", isFancy = true)
	)
	
	private fun TOMB(file: String, entranceY: Int): (Boolean) -> TombDungeonAbstractPiece{
		return { TombDungeonRoom_Tomb(file, entranceY, allowSecrets = false, isFancy = it) }
	}
	
	private fun TOMB_MASS(width: Int, depth: Int, border: Boolean, split: Boolean): (Boolean) -> TombDungeonAbstractPiece{
		return { TombDungeonRoom_Tomb_Mass(width, depth, border, split, isFancy = it) }
	}
	
	private fun TOMB_MASS_SPACIOUS(file: String): (Boolean) -> TombDungeonAbstractPiece{
		return { TombDungeonRoom_Tomb_MassSpacious(file, entranceY = 1, allowSecrets = false, isFancy = it) }
	}
	
	private fun TOMB_MULTI_DEEP(file: String, tombsPerColumn: Int): (Boolean) -> TombDungeonAbstractPiece{
		return { TombDungeonRoom_Tomb_MultiDeep(file, tombsPerColumn, entranceY = 2, allowSecrets = false, isFancy = it) }
	}
	
	private fun TOMB_MULTI_SPACIOUS(file: String, tombsPerColumn: Int): (Boolean) -> TombDungeonAbstractPiece{
		return { TombDungeonRoom_Tomb_MultiSpacious(file, tombsPerColumn, entranceY = 2, allowSecrets = false, isFancy = it) }
	}
	
	private fun TOMB_SINGLE(file: String): (Boolean) -> TombDungeonAbstractPiece{
		return { TombDungeonRoom_Tomb_Single(file, entranceY = 2, allowSecrets = false, isFancy = it) }
	}
	
	fun TOMB_RANDOM(rand: Random, options: WeightedList<(Boolean) -> TombDungeonAbstractPiece>): (Boolean) -> TombDungeonAbstractPiece{
		return { options.generateItem(rand)(it) }
	}
	
	fun TOMB_RANDOM(rand: Random, vararg options: WeightedList<(Boolean) -> TombDungeonAbstractPiece>): (Boolean) -> TombDungeonAbstractPiece{
		return { rand.nextItem(options).generateItem(rand)(it) }
	}
	
	val PIECE_TOMB_RANDOM_MASS_5X_BASIC = weightedListOf(
		2 to TOMB_MASS(width = 5, depth = 4, border = false, split = false),
		3 to TOMB_MASS(width = 5, depth = 5, border = false, split = false),
		2 to TOMB_MASS(width = 5, depth = 6, border = false, split = false)
	)
	
	val PIECE_TOMB_RANDOM_MASS_5X_BORDER = weightedListOf(
		5 to TOMB_MASS(width = 5, depth = 5, border = true, split = false),
		4 to TOMB_MASS(width = 5, depth = 6, border = true, split = false),
		3 to TOMB_MASS(width = 5, depth = 7, border = true, split = false)
	)
	
	val PIECE_TOMB_RANDOM_MASS_5X_SPLIT = weightedListOf(
		3 to TOMB_MASS(width = 5, depth = 4, border = false, split = true),
		3 to TOMB_MASS(width = 5, depth = 5, border = false, split = true),
		2 to TOMB_MASS(width = 5, depth = 6, border = false, split = true)
	)
	
	val PIECE_TOMB_RANDOM_MASS_7X_BASIC = weightedListOf(
		2 to TOMB_MASS(width = 7, depth = 5, border = false, split = false),
		2 to TOMB_MASS(width = 7, depth = 6, border = false, split = false),
		3 to TOMB_MASS(width = 7, depth = 7, border = false, split = false)
	)
	
	val PIECE_TOMB_RANDOM_MASS_7X_BORDER = weightedListOf(
		3 to TOMB_MASS(width = 7, depth = 6, border = true, split = false),
		3 to TOMB_MASS(width = 7, depth = 7, border = true, split = false)
	)
	
	val PIECE_TOMB_RANDOM_MASS_7X_SPLIT = weightedListOf(
		2 to TOMB_MASS(width = 7, depth = 5, border = false, split = true),
		3 to TOMB_MASS(width = 7, depth = 6, border = false, split = true),
		2 to TOMB_MASS(width = 7, depth = 7, border = false, split = true)
	)
	
	val PIECE_TOMB_RANDOM_MASS_7X_BORDER_SPLIT = weightedListOf(
		3 to TOMB_MASS(width = 7, depth = 6, border = true, split = true),
		3 to TOMB_MASS(width = 7, depth = 7, border = true, split = true)
	)
	
	val PIECE_TOMB_RANDOM_MASS_SPACIOUS = weightedListOf(
		5 to TOMB_MASS_SPACIOUS("tomb.mass.spacious_2x6x3.nbt"),
		5 to TOMB_MASS_SPACIOUS("tomb.mass.spacious_2x7x3.nbt"),
		5 to TOMB_MASS_SPACIOUS("tomb.mass.spacious_2x8x3.nbt"),
		2 to TOMB_MASS_SPACIOUS("tomb.mass.spacious_4x3x3.nbt"),
		2 to TOMB_MASS_SPACIOUS("tomb.mass.spacious_6x2x2.nbt")
	)
	
	val PIECE_TOMB_RANDOM_MULTI_NARROW = weightedListOf(
		1 to TOMB("tomb.multi.narrow_4x2.nbt", entranceY = 1),
		3 to TOMB("tomb.multi.narrow_5x2.nbt", entranceY = 1),
		3 to TOMB("tomb.multi.narrow_6x2.nbt", entranceY = 1),
		2 to TOMB("tomb.multi.narrow_7x2.nbt", entranceY = 1)
	)
	
	val PIECE_TOMB_RANDOM_MULTI_DEEP_SHORT = weightedListOf(
		2 to TOMB_MULTI_DEEP("tomb.multi.deep_4x2.nbt", tombsPerColumn = 4),
		2 to TOMB_MULTI_DEEP("tomb.multi.deep_5x2.nbt", tombsPerColumn = 5),
		3 to TOMB_MULTI_DEEP("tomb.multi.deep_6x2.nbt", tombsPerColumn = 6)
	)
	
	val PIECE_TOMB_RANDOM_MULTI_DEEP_LONG = weightedListOf(
		3 to TOMB_MULTI_DEEP("tomb.multi.deep_7x2.nbt", tombsPerColumn = 7),
		2 to TOMB_MULTI_DEEP("tomb.multi.deep_8x2.nbt", tombsPerColumn = 8),
		2 to TOMB_MULTI_DEEP("tomb.multi.deep_9x2.nbt", tombsPerColumn = 9)
	)
	
	val PIECE_TOMB_RANDOM_MULTI_SPACIOUS = weightedListOf(
		1 to TOMB_MULTI_SPACIOUS("tomb.multi.spacious_4x2.nbt", tombsPerColumn = 4),
		3 to TOMB_MULTI_SPACIOUS("tomb.multi.spacious_5x2.nbt", tombsPerColumn = 5),
		3 to TOMB_MULTI_SPACIOUS("tomb.multi.spacious_6x2.nbt", tombsPerColumn = 6),
		2 to TOMB_MULTI_SPACIOUS("tomb.multi.spacious_7x2.nbt", tombsPerColumn = 7)
	)
	
	val PIECE_TOMB_SINGLE_NARROW   = TOMB_SINGLE("tomb.single.narrow.nbt")
	val PIECE_TOMB_SINGLE_SPACIOUS = TOMB_SINGLE("tomb.single.spacious.nbt")
	
	override val ALL_PIECES
		get() = arrayOf(
			TombDungeonCorridor_Straight(length = 5, isFancy = false),
			TombDungeonCorridor_StraightCrumbling(length = 5, fallHeight = 5, isFancy = false),
			TombDungeonCorridor_Intersection(isFancy = false),
			
			*PIECES_STAIRS,
			
			*PIECE_TOMB_RANDOM_MASS_5X_BASIC.values.map        { it(false) }.toTypedArray(),
			*PIECE_TOMB_RANDOM_MASS_5X_BORDER.values.map       { it(false) }.toTypedArray(),
			*PIECE_TOMB_RANDOM_MASS_5X_SPLIT.values.map        { it(false) }.toTypedArray(),
			*PIECE_TOMB_RANDOM_MASS_7X_BASIC.values.map        { it(false) }.toTypedArray(),
			*PIECE_TOMB_RANDOM_MASS_7X_BORDER.values.map       { it(false) }.toTypedArray(),
			*PIECE_TOMB_RANDOM_MASS_7X_SPLIT.values.map        { it(false) }.toTypedArray(),
			*PIECE_TOMB_RANDOM_MASS_7X_BORDER_SPLIT.values.map { it(false) }.toTypedArray(),
			*PIECE_TOMB_RANDOM_MASS_SPACIOUS.values.map        { it(false) }.toTypedArray(),
			
			*PIECE_TOMB_RANDOM_MULTI_NARROW.values.map     { it(false) }.toTypedArray(),
			*PIECE_TOMB_RANDOM_MULTI_DEEP_SHORT.values.map { it(true) }.toTypedArray(),
			*PIECE_TOMB_RANDOM_MULTI_DEEP_LONG.values.map  { it(true) }.toTypedArray(),
			*PIECE_TOMB_RANDOM_MULTI_SPACIOUS.values.map   { it(true) }.toTypedArray(),
			
			PIECE_TOMB_SINGLE_NARROW(true),
			PIECE_TOMB_SINGLE_SPACIOUS(true),
			
			*PIECES_SECRET_GUARANTEED,
			*PIECES_SECRET_RANDOM,
			
			*PIECES_SIDE_ROOMS_NONFANCY,
			*PIECES_MAIN_ROOMS_NONFANCY,
			*PIECES_SIDE_ROOMS_FANCY,
			*PIECES_MAIN_ROOMS_FANCY,
			
			PIECE_ROOM_END
		)
}
