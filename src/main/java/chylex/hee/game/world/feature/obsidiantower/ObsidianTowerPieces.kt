package chylex.hee.game.world.feature.obsidiantower
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerSpawnerLevel.LEVEL_1
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerSpawnerLevel.LEVEL_2
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerSpawnerLevel.LEVEL_3
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerSpawnerLevel.LEVEL_4
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerDebugRoomPiece
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerLevel_General
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerLevel_Top
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Chest_MarketStalls
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Chest_ObsidianCeiling
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Chest_SpiralStaircase
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_PreBoss_GloomrockPillars
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Rare_FenceBlockage
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Rare_PillarMaze
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Rare_SpawnerCircle
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Rare_SpawnerCover
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Rare_SpawnerDoor
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Regular_BloodyDroppers
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Regular_CeilingSpawners
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Regular_CenterPillar
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Regular_GloomrockTables
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Regular_GooTrap
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Regular_GuardingHeads
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Regular_IronSupports
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Regular_LibraryCorners
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Regular_LibraryShelves
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Regular_SideFenceSpawners
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Regular_SideWorkTables
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Regular_StaringHeads
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_Regular_SurroundFenceSpawners
import chylex.hee.game.world.structure.IBlockPicker.Weighted.Companion.Weighted
import chylex.hee.game.world.structure.IStructureDescription
import chylex.hee.game.world.structure.IStructureDescription.Companion.NULL_LOCATOR
import chylex.hee.game.world.structure.file.PaletteBuilder
import chylex.hee.game.world.structure.file.PaletteMappings
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.game.world.util.Size
import chylex.hee.init.ModBlocks
import chylex.hee.system.Resource
import chylex.hee.system.collection.WeightedList.Companion.weightedListOf
import chylex.hee.system.util.with
import net.minecraft.block.BlockTorch
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing.UP
import net.minecraftforge.fluids.BlockFluidBase

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
		add("bookshelf", Blocks.BOOKSHELF)
		add("ironbars", Blocks.IRON_BARS)
		
		add("obsidian.smooth", ModBlocks.OBSIDIAN_SMOOTH)
		add("obsidian.chiseled", ModBlocks.OBSIDIAN_CHISELED)
		add("obsidian.pillar.*", ModBlocks.OBSIDIAN_PILLAR, PaletteMappings.FACING_AXIS)
		
		add("obsidian.smooth.lit", ModBlocks.OBSIDIAN_SMOOTH_LIT)
		add("obsidian.chiseled.lit", ModBlocks.OBSIDIAN_CHISELED_LIT)
		add("obsidian.pillar.lit.*", ModBlocks.OBSIDIAN_PILLAR_LIT, PaletteMappings.FACING_AXIS)
		
		add("gloomrock.bricks", ModBlocks.GLOOMROCK_BRICKS)
		add("gloomrock.smooth", ModBlocks.GLOOMROCK_SMOOTH)
		
		add("slab.gloomrock.bricks.*", ModBlocks.GLOOMROCK_BRICK_SLAB, PaletteMappings.SLAB_HALF)
		add("slab.gloomrock.bricks.double", ModBlocks.GLOOMROCK_BRICK_DOUBLE_SLAB)
		add("slab.gloomrock.smooth.*", ModBlocks.GLOOMROCK_SMOOTH_SLAB, PaletteMappings.SLAB_HALF)
		add("slab.gloomrock.smooth.double", ModBlocks.GLOOMROCK_SMOOTH_DOUBLE_SLAB)
		
		add("stairs.obsidian.*.*", ModBlocks.OBSIDIAN_STAIRS, PaletteMappings.STAIR_MAPPING_LIST)
		add("stairs.gloomrock.bricks.*.*", ModBlocks.GLOOMROCK_BRICK_STAIRS, PaletteMappings.STAIR_MAPPING_LIST)
		add("stairs.gloomrock.smooth.*.*", ModBlocks.GLOOMROCK_SMOOTH_STAIRS, PaletteMappings.STAIR_MAPPING_LIST)
		
		add("ladder.*", Blocks.LADDER, PaletteMappings.FACING_HORIZONTAL)
		add("trapdoor.*.*.*", Blocks.TRAPDOOR, PaletteMappings.TRAPDOOR_MAPPING_LIST)
		add("fence.darkoak", Blocks.DARK_OAK_FENCE)
		
		add("torch.up", Blocks.TORCH.with(BlockTorch.FACING, UP))
		add("chest.*", Blocks.CHEST, PaletteMappings.FACING_HORIZONTAL)
		add("furnace.*", Blocks.FURNACE, PaletteMappings.FACING_HORIZONTAL)
		add("dropper.*", Blocks.DROPPER, PaletteMappings.FACING_ALL)
		
		add("endergoo", ModBlocks.ENDER_GOO.with(BlockFluidBase.LEVEL, 0))
		
		with(forGeneration){
			add("redstone.random", Weighted(
				7 to Blocks.AIR,
				1 to Blocks.REDSTONE_WIRE
			))
		}
		
		with(forDevelopment){
			add("redstone.random", Blocks.REDSTONE_WIRE)
		}
		
		build()
	}
	
	// Loot
	
	val LOOT_GENERAL = Resource.Custom("chests/obsidiantower_general")
	val LOOT_SPECIAL = Resource.Custom("chests/obsidiantower_special")
	val LOOT_FUEL = Resource.Custom("chests/obsidiantower_fuel")
	
	// Pieces
	
	val PIECE_LEVEL_BOTTOM = ObsidianTowerLevel_General("level.bottom.nbt")
	val PIECE_LEVEL_MIDDLE = ObsidianTowerLevel_General("level.middle.nbt")
	private val PIECE_LEVEL_TOP_BOSS = ObsidianTowerLevel_Top.Boss("level.top.nbt")
	
	private fun PIECE_LEVEL_TOP_TOKEN(tokenType: TokenType, territoryType: TerritoryType): ObsidianTowerLevel_Top{
		return ObsidianTowerLevel_Top.Token("level.top.nbt", tokenType, territoryType)
	}
	
	private val PIECES_BASE = arrayOf(
		PIECE_LEVEL_BOTTOM,
		PIECE_LEVEL_MIDDLE,
		PIECE_LEVEL_TOP_BOSS
	)
	
	private val PIECES_ROOMS_REGULAR = arrayOf(
		ObsidianTowerRoom_Regular_BloodyDroppers("regular.bloody_droppers.nbt"),
		ObsidianTowerRoom_Regular_CeilingSpawners("regular.ceiling_spawners.nbt"),
		ObsidianTowerRoom_Regular_CenterPillar("regular.center_pillar.nbt"),
		ObsidianTowerRoom_Regular_GloomrockTables("regular.gloomrock_tables.nbt"),
		ObsidianTowerRoom_Regular_GooTrap("regular.goo_trap.nbt"),
		ObsidianTowerRoom_Regular_GuardingHeads("regular.guarding_heads.nbt"),
		ObsidianTowerRoom_Regular_IronSupports("regular.iron_supports.nbt"),
		ObsidianTowerRoom_Regular_LibraryCorners("regular.library_corners.nbt"),
		ObsidianTowerRoom_Regular_LibraryShelves("regular.library_shelves.nbt"),
		ObsidianTowerRoom_Regular_SideFenceSpawners("regular.side_fence_spawners.nbt"),
		ObsidianTowerRoom_Regular_SideWorkTables("regular.side_work_tables.nbt"),
		ObsidianTowerRoom_Regular_StaringHeads("regular.staring_heads.nbt"),
		ObsidianTowerRoom_Regular_SurroundFenceSpawners("regular.surround_fence_spawners.nbt")
	)
	
	private val PIECES_ROOMS_RARE = arrayOf(
		ObsidianTowerRoom_Rare_FenceBlockage("rare.fence_blockage.nbt"),
		ObsidianTowerRoom_Rare_PillarMaze("rare.pillar_maze.nbt"),
		ObsidianTowerRoom_Rare_SpawnerCircle("rare.spawner_circle.nbt"),
		ObsidianTowerRoom_Rare_SpawnerCover("rare.spawner_cover.nbt"),
		ObsidianTowerRoom_Rare_SpawnerDoor("rare.spawner_door.nbt")
	)
	
	private val PIECES_ROOMS_CHEST = arrayOf(
		ObsidianTowerRoom_Chest_MarketStalls("chest.market_stalls.nbt"),
		ObsidianTowerRoom_Chest_ObsidianCeiling("chest.obsidian_ceiling.nbt"),
		ObsidianTowerRoom_Chest_SpiralStaircase("chest.spiral_staircase.nbt")
	)
	
	private val PIECES_ROOMS_PREBOSS = arrayOf(
		ObsidianTowerRoom_PreBoss_GloomrockPillars("preboss.gloomrock_pillars.nbt")
	)
	
	private val ALL_ROOM_PIECES
		get() = arrayOf(
			*PIECES_ROOMS_REGULAR,
			*PIECES_ROOMS_RARE,
			*PIECES_ROOMS_CHEST,
			*PIECES_ROOMS_PREBOSS
		)
	
	override val ALL_PIECES
		get() = arrayOf(
			*PIECES_BASE,
			*ALL_ROOM_PIECES,
			*(ALL_ROOM_PIECES.map { ObsidianTowerDebugRoomPiece(PIECE_LEVEL_BOTTOM, it) }.toTypedArray())
		)
	
	// Arrangements
	
	fun ARRANGEMENTS_REGULAR(tokenType: TokenType, territoryType: TerritoryType) = PIECE_LEVEL_TOP_TOKEN(tokenType, territoryType).let {
		topPiece -> weightedListOf(
			10 to ObsidianTowerRoomArrangement(arrayOf(
				PIECES_ROOMS_REGULAR to LEVEL_1,
				PIECES_ROOMS_REGULAR to LEVEL_2,
				PIECES_ROOMS_CHEST to LEVEL_2
			), topPiece),
			
			7 to ObsidianTowerRoomArrangement(arrayOf(
				PIECES_ROOMS_REGULAR to LEVEL_1,
				PIECES_ROOMS_CHEST to LEVEL_2,
				PIECES_ROOMS_REGULAR to LEVEL_2
			), topPiece),
		
			5 to ObsidianTowerRoomArrangement(arrayOf(
				PIECES_ROOMS_REGULAR to LEVEL_1,
				PIECES_ROOMS_REGULAR to LEVEL_2,
				PIECES_ROOMS_RARE to LEVEL_2
			), topPiece)
		)
	}
	
	val ARRANGEMENTS_BOSS = weightedListOf(
		10 to ObsidianTowerRoomArrangement(arrayOf(
			PIECES_ROOMS_REGULAR to LEVEL_1,
			PIECES_ROOMS_REGULAR to LEVEL_2,
			PIECES_ROOMS_REGULAR to LEVEL_3,
			PIECES_ROOMS_RARE    to LEVEL_3,
			PIECES_ROOMS_PREBOSS to LEVEL_4
		), PIECE_LEVEL_TOP_BOSS),
		
		8 to ObsidianTowerRoomArrangement(arrayOf(
			PIECES_ROOMS_REGULAR to LEVEL_1,
			PIECES_ROOMS_REGULAR to LEVEL_2,
			PIECES_ROOMS_RARE    to LEVEL_3,
			PIECES_ROOMS_REGULAR to LEVEL_3,
			PIECES_ROOMS_PREBOSS to LEVEL_4
		), PIECE_LEVEL_TOP_BOSS),
		
		7 to ObsidianTowerRoomArrangement(arrayOf(
			PIECES_ROOMS_REGULAR to LEVEL_1,
			PIECES_ROOMS_REGULAR to LEVEL_2,
			PIECES_ROOMS_CHEST   to LEVEL_3,
			PIECES_ROOMS_REGULAR to LEVEL_3,
			PIECES_ROOMS_PREBOSS to LEVEL_4
		), PIECE_LEVEL_TOP_BOSS)
	)
	
	private val ARRANGEMENT_DEBUG = ObsidianTowerRoomArrangement(arrayOf(
		PIECES_ROOMS_REGULAR to LEVEL_1,
		PIECES_ROOMS_REGULAR to LEVEL_1,
		PIECES_ROOMS_CHEST   to LEVEL_1,
		PIECES_ROOMS_REGULAR to LEVEL_2,
		PIECES_ROOMS_REGULAR to LEVEL_2,
		PIECES_ROOMS_RARE    to LEVEL_2,
		PIECES_ROOMS_REGULAR to LEVEL_3,
		PIECES_ROOMS_REGULAR to LEVEL_3,
		PIECES_ROOMS_RARE    to LEVEL_3,
		PIECES_ROOMS_REGULAR to LEVEL_4,
		PIECES_ROOMS_PREBOSS to LEVEL_4
	), PIECE_LEVEL_TOP_BOSS)
	
	// Description
	
	private const val DEFAULT_FLOORS = 5
	
	override val STRUCTURE_SIZE = calculateStructureSize(DEFAULT_FLOORS)
	
	override val STRUCTURE_BUILDER = ObsidianTowerBuilder(ARRANGEMENT_DEBUG)
	override val STRUCTURE_LOCATOR = NULL_LOCATOR
}
