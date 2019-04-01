package chylex.hee.game.world.feature.energyshrine
import chylex.hee.game.world.feature.energyshrine.EnergyShrineBanners.BannerColors
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineAbstractPiece
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineCorridor_Corner
import chylex.hee.game.world.feature.energyshrine.piece.EnergyShrineCorridor_Staircase_180
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
import chylex.hee.system.Resource
import chylex.hee.system.collection.WeightedList.Companion.weightedListOf
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.removeItem
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import java.util.Random

object EnergyShrinePieces : IStructureDescription{
	override val STRUCTURE_SIZE = Size(128, 40, 128)
	
	override val STRUCTURE_BUILDER = EnergyShrineBuilder
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
	
	private fun PIECES_CORRIDORS_GENERIC(rand: Random, targetAmount: Int): MutableList<Array<out EnergyShrineAbstractPiece>>{
		fun newStraightCorridor() = when{
			rand.nextBoolean() -> arrayOf(EnergyShrineCorridor_StraightLit(1 + 2 * rand.nextInt(0, 3)))
			rand.nextInt(4) != 0 -> arrayOf(EnergyShrineCorridor_Straight(rand.nextInt(1, 7)))
			else -> emptyArray()
		}
		
		fun newCornerCorridor(): Array<EnergyShrineAbstractPiece>{
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
		
		return mutableListOf<Array<out EnergyShrineAbstractPiece>>().apply {
			repeat(rand.nextInt(3, 4)){
				add(newCornerCorridor())
			}
			
			while(size < targetAmount){
				add(newStraightCorridor())
			}
		}
	}
	
	// Pieces (Rooms)
	
	val PIECES_START = arrayOf(
		EnergyShrineRoom_Main_Start("main.start.nbt")
	)
	
	val PIECES_END = arrayOf(
		EnergyShrineRoom_Main_Final("main.end.nbt")
	)
	
	private val PIECES_ROOMS_PRIMARY = arrayOf<(Block, BannerColors) -> EnergyShrineAbstractPiece>(
		{ block, bannerColors -> EnergyShrineRoom_Primary_DisplayCase("primary.display_case.nbt", block, bannerColors) },
		{ block, bannerColors -> EnergyShrineRoom_Primary_Secretariat("primary.secretariat.nbt", block, bannerColors) },
		{ block, bannerColors -> EnergyShrineRoom_Primary_SplitT("primary.split_t.nbt", block, bannerColors) },
		{ block, bannerColors -> EnergyShrineRoom_Primary_TwoFloorFork("primary.two_floor_fork.nbt", block, bannerColors) },
		{ block, bannerColors -> EnergyShrineRoom_Primary_TwoFloorSecret("primary.two_floor_secret.nbt", block, bannerColors) }
	)
	
	private val PIECES_ROOMS_PRIMARY_LARGE_INTERSECTIONS = arrayOf<(Block, BannerColors) -> EnergyShrineAbstractPiece>(
		{ block, bannerColors -> EnergyShrineRoom_Primary_TwoFloorOverhang("primary.two_floor_overhang.nbt", block, bannerColors) },
		{ block, bannerColors -> EnergyShrineRoom_Primary_TwoFloorT("primary.two_floor_t.nbt", block, bannerColors) }
	)
	
	private val PIECES_ROOMS_SECONDARY = arrayOf<(Block, BannerColors) -> EnergyShrineAbstractPiece>(
		{ block, bannerColors -> EnergyShrineRoom_Secondary_Dormitory("secondary.dormitory.nbt", block, bannerColors) },
		{ block, bannerColors -> EnergyShrineRoom_Secondary_Portal("secondary.portal.nbt", block, bannerColors) },
		{ block, bannerColors -> EnergyShrineRoom_Secondary_Storage("secondary.storage.nbt", block, bannerColors) }
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
		
		val mainPathRooms = mutableListOf<EnergyShrineAbstractPiece>()
		val offPathRooms = mutableListOf<EnergyShrineAbstractPiece>()
		
		// add guaranteed rooms
		
		mainPathRooms.add(rand.nextItem(PIECES_ROOMS_PRIMARY_LARGE_INTERSECTIONS)(cornerColors.removeAt(0), bannerColors))
		
		for(secondaryRoom in PIECES_ROOMS_SECONDARY){
			offPathRooms.add(secondaryRoom(cornerColors.removeAt(0), bannerColors))
		}
		
		// add random rooms to fill up the quota
		
		val remainingPrimaryRooms = PIECES_ROOMS_PRIMARY.toMutableList()
		
		while(mainPathRooms.size < targetMainPathRoomAmount){
			mainPathRooms.add(rand.removeItem(remainingPrimaryRooms)(cornerColors.removeAt(0), bannerColors))
		}
		
		while(offPathRooms.size < targetOffPathRoomAmount){
			offPathRooms.add(rand.removeItem(remainingPrimaryRooms)(cornerColors.removeAt(0), bannerColors))
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
		
		mainPathCorridors.add(arrayOf(rand.nextItem(PIECES_CORRIDOR_STAIRS)))
		
		while(mainPathCorridors.size < mainRoomCount + 1){
			mainPathCorridors.add(rand.removeItem(remainingGenericCorridors))
		}
		
		while(offPathCorridors.size < offRoomCount){
			offPathCorridors.add(rand.removeItem(remainingGenericCorridors))
		}
		
		return CorridorConfiguration(mainPathCorridors, offPathCorridors)
	}
	
	class RoomConfiguration(
		val mainPath: MutableList<EnergyShrineAbstractPiece>,
		val offPath: MutableList<EnergyShrineAbstractPiece>
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
			*PIECES_CORRIDOR_STAIRS,
			
			*PIECES_START,
			*PIECES_END,
			
			*PIECES_ROOMS_PRIMARY_LARGE_INTERSECTIONS.map { it(ModBlocks.GLOOMROCK_SMOOTH_WHITE, BannerColors.DEFAULT) }.toTypedArray(),
			*PIECES_ROOMS_PRIMARY.map { it(ModBlocks.GLOOMROCK_SMOOTH_WHITE, BannerColors.DEFAULT) }.toTypedArray(),
			*PIECES_ROOMS_SECONDARY.map { it(ModBlocks.GLOOMROCK_SMOOTH_WHITE, BannerColors.DEFAULT) }.toTypedArray()
		)
}
