package chylex.hee.game.world.feature.energyshrine
import chylex.hee.game.world.feature.energyshrine.EnergyShrineBanners.BannerColors
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineAbstractPiece
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineCorridor_Corner
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineCorridor_Staircase_180_Bottom
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineCorridor_Staircase_180_Top
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineCorridor_Staircase_90
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineCorridor_Straight
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineCorridor_StraightLit
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineRoom_Main_Final
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineRoom_Main_Start
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineRoom_Primary_DisplayCase
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineRoom_Primary_Secretariat
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineRoom_Primary_SplitT
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineRoom_Primary_TwoFloorFork
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineRoom_Primary_TwoFloorOverhang
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineRoom_Primary_TwoFloorSecret
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineRoom_Primary_TwoFloorT
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineRoom_Secondary_Dormitory
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineRoom_Secondary_Portal
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineRoom_Secondary_Storage
import chylex.hee.game.world.structure.IStructureDescription
import chylex.hee.game.world.structure.file.PaletteBuilder
import chylex.hee.game.world.structure.file.PaletteMappings
import chylex.hee.game.world.util.Size
import chylex.hee.init.ModBlocks
import chylex.hee.system.collection.WeightedList.Companion.weightedListOf
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.removeItem
import java.util.Random

object EnergyShrinePieces : IStructureDescription{
	override val STRUCTURE_SIZE = Size(128, 40, 128)
	
	override val STRUCTURE_BUILDER = EnergyShrineBuilder
	override val STRUCTURE_LOCATOR = EnergyShrineGenerator::findNearest
	
	// Palette
	
	override val PALETTE
		get() = with(PaletteBuilder.Combined()){
			add("air", Blocks.AIR)
			add("bedrock", Blocks.BEDROCK)
			
			add("gloomrock", ModBlocks.GLOOMROCK)
			add("gloomrock.bricks", ModBlocks.GLOOMROCK_BRICKS)
			add("gloomrock.smooth", ModBlocks.GLOOMROCK_SMOOTH)
			add("gloomrock.white", ModBlocks.GLOOMROCK_SMOOTH_WHITE)
			
			add("slab.gloomrock.bricks.*", ModBlocks.GLOOMROCK_BRICK_SLAB, PaletteMappings.SLAB_TYPE)
			add("slab.gloomrock.smooth.*", ModBlocks.GLOOMROCK_SMOOTH_SLAB, PaletteMappings.SLAB_TYPE)
			
			add("stairs.gloomrock.bricks.*.*.*", ModBlocks.GLOOMROCK_BRICK_STAIRS, PaletteMappings.STAIR_MAPPING_LIST)
			add("stairs.gloomrock.smooth.*.*.*", ModBlocks.GLOOMROCK_SMOOTH_STAIRS, PaletteMappings.STAIR_MAPPING_LIST)
			
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
		arrayOf(EnergyShrineCorridor_Staircase_90("corridor.staircase90.nbt")),
		arrayOf(EnergyShrineCorridor_Staircase_180_Top("corridor.staircase180top.nbt"),
			    EnergyShrineCorridor_Staircase_180_Bottom("corridor.staircase180bottom.nbt"))
	)
	
	private fun PIECES_CORRIDOR_PART_STRAIGHT(rand: Random) = when{
		rand.nextBoolean() -> arrayOf(EnergyShrineCorridor_StraightLit(1 + 2 * rand.nextInt(0, 3)))
		rand.nextInt(4) != 0 -> arrayOf(EnergyShrineCorridor_Straight(rand.nextInt(1, 7)))
		else -> emptyArray()
	}
	
	private fun PIECES_CORRIDOR_PART_CORNER(rand: Random): Array<EnergyShrineAbstractPiece>{
		fun pickShortCorridor() = when{
			rand.nextInt(3) == 0 -> EnergyShrineCorridor_StraightLit(1 + 2 * rand.nextInt(0, 2))
			rand.nextInt(5) != 0 -> EnergyShrineCorridor_Straight(rand.nextInt(1, 5))
			else -> null
		}
		
		return listOfNotNull(
			pickShortCorridor(),
			PIECES_CORRIDOR_CORNER.generateItem(rand),
			pickShortCorridor()
		).toTypedArray()
	}
	
	private fun PIECES_CORRIDORS_GENERIC(rand: Random, targetAmount: Int) = mutableListOf<Array<out EnergyShrineAbstractPiece>>().apply {
		repeat(rand.nextInt(3, 4)){
			add(PIECES_CORRIDOR_PART_CORNER(rand))
		}
		
		while(size < targetAmount){
			add(PIECES_CORRIDOR_PART_STRAIGHT(rand))
		}
	}
	
	// Pieces (Rooms)
	
	val PIECES_START = arrayOf(
		EnergyShrineRoom_Main_Start("main.start.nbt")
	)
	
	val PIECES_END = arrayOf(
		EnergyShrineRoom_Main_Final("main.end.nbt")
	)
	
	private val PIECES_ROOMS_PRIMARY = arrayOf(
		EnergyShrineRoom_Primary_DisplayCase("primary.display_case.nbt"),
		EnergyShrineRoom_Primary_Secretariat("primary.secretariat.nbt"),
		EnergyShrineRoom_Primary_SplitT("primary.split_t.nbt"),
		EnergyShrineRoom_Primary_TwoFloorFork("primary.two_floor_fork.nbt"),
		EnergyShrineRoom_Primary_TwoFloorSecret("primary.two_floor_secret.nbt")
	)
	
	private val PIECES_ROOMS_PRIMARY_LARGE_INTERSECTIONS = arrayOf(
		EnergyShrineRoom_Primary_TwoFloorOverhang("primary.two_floor_overhang.nbt"),
		EnergyShrineRoom_Primary_TwoFloorT("primary.two_floor_t.nbt")
	)
	
	private val PIECES_ROOMS_SECONDARY = arrayOf(
		EnergyShrineRoom_Secondary_Dormitory("secondary.dormitory.nbt"),
		EnergyShrineRoom_Secondary_Portal("secondary.portal.nbt"),
		EnergyShrineRoom_Secondary_Storage("secondary.storage.nbt")
	)
	
	// Pieces (Configuration)
	
	fun generateRoomConfiguration(rand: Random, targetMainPathRoomAmount: Int): RoomConfiguration{
		val bannerColors = BannerColors.random(rand)
		
		val cornerColors = mutableListOf(
			ModBlocks.GLOOMROCK_SMOOTH_RED,
			ModBlocks.GLOOMROCK_SMOOTH_ORANGE,
			ModBlocks.GLOOMROCK_SMOOTH_YELLOW,
			ModBlocks.GLOOMROCK_SMOOTH_GREEN,
			ModBlocks.GLOOMROCK_SMOOTH_CYAN,
			ModBlocks.GLOOMROCK_SMOOTH_BLUE,
			ModBlocks.GLOOMROCK_SMOOTH_PURPLE,
			ModBlocks.GLOOMROCK_SMOOTH_MAGENTA
		).apply {
			shuffle(rand)
		}
		
		val targetOffPathRoomAmount = cornerColors.size - targetMainPathRoomAmount
		
		val mainPathRooms = mutableListOf<Pair<EnergyShrineAbstractPiece, EnergyShrineRoomData>>()
		val offPathRooms = mutableListOf<Pair<EnergyShrineAbstractPiece, EnergyShrineRoomData>>()
		
		// add guaranteed rooms
		
		mainPathRooms.add(rand.nextItem(PIECES_ROOMS_PRIMARY_LARGE_INTERSECTIONS) to EnergyShrineRoomData(cornerColors.removeAt(0), bannerColors))
		
		for(secondaryRoom in PIECES_ROOMS_SECONDARY){
			offPathRooms.add(secondaryRoom to EnergyShrineRoomData(cornerColors.removeAt(0), bannerColors))
		}
		
		// add random rooms to fill up the quota
		
		val remainingPrimaryRooms = PIECES_ROOMS_PRIMARY.toMutableList()
		
		while(mainPathRooms.size < targetMainPathRoomAmount){
			mainPathRooms.add(rand.removeItem(remainingPrimaryRooms) to EnergyShrineRoomData(cornerColors.removeAt(0), bannerColors))
		}
		
		while(offPathRooms.size < targetOffPathRoomAmount){
			offPathRooms.add(rand.removeItem(remainingPrimaryRooms) to EnergyShrineRoomData(cornerColors.removeAt(0), bannerColors))
		}
		
		// finalize
		
		return RoomConfiguration(mainPathRooms, offPathRooms)
	}
	
	fun generateCorridorConfiguration(rand: Random, roomConfiguration: RoomConfiguration): CorridorConfiguration{
		val mainRoomCount = roomConfiguration.mainPath.size
		val offRoomCount = roomConfiguration.offPath.size
		
		val mainPathCorridors = mutableListOf<Array<out EnergyShrineAbstractPiece>>()
		val offPathCorridors = mutableListOf<Array<out EnergyShrineAbstractPiece>>()
		
		val remainingGenericCorridors = PIECES_CORRIDORS_GENERIC(rand, mainRoomCount + offRoomCount /* + 1 for stairs */)
		
		mainPathCorridors.add(rand.nextItem(PIECES_CORRIDOR_STAIRS))
		
		while(mainPathCorridors.size < mainRoomCount + 1){
			mainPathCorridors.add(rand.removeItem(remainingGenericCorridors))
		}
		
		while(offPathCorridors.size < offRoomCount){
			offPathCorridors.add(rand.removeItem(remainingGenericCorridors))
		}
		
		return CorridorConfiguration(mainPathCorridors, offPathCorridors)
	}
	
	class RoomConfiguration(
		val mainPath: MutableList<Pair<EnergyShrineAbstractPiece, EnergyShrineRoomData>>,
		val offPath: MutableList<Pair<EnergyShrineAbstractPiece, EnergyShrineRoomData>>
	)
	
	class CorridorConfiguration(
		val mainPath: MutableList<Array<out EnergyShrineAbstractPiece>>,
		val offPath: MutableList<Array<out EnergyShrineAbstractPiece>>
	)
	
	// Pieces (All)
	
	override val ALL_PIECES
		get() = arrayOf(
			EnergyShrineCorridor_Straight(5),
			EnergyShrineCorridor_StraightLit(5),
			
			*PIECES_CORRIDOR_CORNER.values.toTypedArray(),
			*PIECES_CORRIDOR_STAIRS.flatten().toTypedArray(),
			
			*PIECES_START,
			*PIECES_END,
			
			*PIECES_ROOMS_PRIMARY_LARGE_INTERSECTIONS,
			*PIECES_ROOMS_PRIMARY,
			*PIECES_ROOMS_SECONDARY
		)
}
