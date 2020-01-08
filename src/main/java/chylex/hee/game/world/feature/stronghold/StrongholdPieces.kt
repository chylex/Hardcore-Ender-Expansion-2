package chylex.hee.game.world.feature.stronghold
import chylex.hee.game.item.ItemEnergyOracle
import chylex.hee.game.world.feature.stronghold.piece.StrongholdAbstractPiece
import chylex.hee.game.world.feature.stronghold.piece.StrongholdCorridor_Chest_Double
import chylex.hee.game.world.feature.stronghold.piece.StrongholdCorridor_Chest_Single
import chylex.hee.game.world.feature.stronghold.piece.StrongholdCorridor_Intersection
import chylex.hee.game.world.feature.stronghold.piece.StrongholdCorridor_Stairs_Straight
import chylex.hee.game.world.feature.stronghold.piece.StrongholdCorridor_Stairs_Vertical
import chylex.hee.game.world.feature.stronghold.piece.StrongholdCorridor_Straight
import chylex.hee.game.world.feature.stronghold.piece.StrongholdDoor_Generic
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Chest_DecoratedCorners
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Chest_Pool_DrainedOrEmbedded
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Chest_Pool_Raised
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Chest_TwoFloorCorridor
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Chest_WoodenSupports
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Cluster_Floating
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Cluster_Pillar
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Cluster_TwoFloorIntersection
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Cluster_Waterfalls
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_DeadEnd_Shelves
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_DeadEnd_Waterfalls
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Decor_Generic
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Decor_GlassCorners
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Decor_StairSnake
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Decor_TwoFloorIntersection
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Main_Library
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Main_Portal
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Main_Scriptorium
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Main_Workshop
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Relic
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Relic_Dungeon
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Relic_Fountains
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Relic_Hell
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Trap_CornerHoles
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Trap_Prison
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Trap_TallIntersection
import chylex.hee.game.world.structure.IBlockPicker.Weighted.Companion.Weighted
import chylex.hee.game.world.structure.IStructureDescription
import chylex.hee.game.world.structure.file.PaletteBuilder
import chylex.hee.game.world.structure.file.PaletteMappings
import chylex.hee.game.world.util.Size
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.system.collection.MutableWeightedList
import chylex.hee.system.collection.WeightedList.Companion.weightedListOf
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.migration.vanilla.BlockSilverfish
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.removeItem
import net.minecraft.block.Block
import net.minecraft.item.DyeColor
import net.minecraft.item.ItemStack
import net.minecraft.tags.BlockTags
import java.util.Random

object StrongholdPieces : IStructureDescription{
	override val STRUCTURE_SIZE = Size(224, 32, 224)
	
	override val STRUCTURE_BUILDER = StrongholdBuilder
	override val STRUCTURE_LOCATOR = StrongholdGenerator::findNearest
	
	// Palette
	
	val PALETTE_ENTRY_STONE_BRICK = Weighted(
		610 to Blocks.STONE_BRICKS,
		175 to Blocks.MOSSY_STONE_BRICKS,
		175 to Blocks.CRACKED_STONE_BRICKS,
		 20 to Blocks.INFESTED_STONE_BRICKS,
		 12 to Blocks.INFESTED_MOSSY_STONE_BRICKS,
		  8 to Blocks.INFESTED_CRACKED_STONE_BRICKS
	)
	
	override val PALETTE = with(PaletteBuilder.Combined()){
		add("air", Blocks.AIR)
		add("bookshelf", Blocks.BOOKSHELF)
		add("obsidian", Blocks.OBSIDIAN)
		add("ethereallantern", ModBlocks.ETHEREAL_LANTERN)
		add("workbench", Blocks.CRAFTING_TABLE)
		add("ironbars", Blocks.IRON_BARS)
		add("redstone", Blocks.REDSTONE_WIRE)
		add("water", Blocks.WATER)
		add("lava", Blocks.LAVA)
		
		add("wall.stonebrick", ModBlocks.STONE_BRICK_WALL)
		
		add("slab.stone.*", Blocks.STONE_SLAB, PaletteMappings.SLAB_TYPE)
		add("slab.stonebrick.*", Blocks.STONE_BRICK_SLAB, PaletteMappings.SLAB_TYPE)
		add("slab.spruce.*", Blocks.SPRUCE_SLAB, PaletteMappings.SLAB_TYPE)
		
		add("stairs.stonebrick.*.*", Blocks.STONE_BRICK_STAIRS, PaletteMappings.STAIR_MAPPING_LIST)
		add("stairs.spruce.*.*", Blocks.SPRUCE_STAIRS, PaletteMappings.STAIR_MAPPING_LIST)
		add("stairs.darkoak.*.*", Blocks.DARK_OAK_STAIRS, PaletteMappings.STAIR_MAPPING_LIST)
		
		add("log.spruce.*", Blocks.SPRUCE_LOG, PaletteMappings.FACING_AXIS)
		add("log.darkoak.*", Blocks.DARK_OAK_LOG, PaletteMappings.FACING_AXIS)
		
		add("planks.oak", Blocks.OAK_PLANKS)
		add("planks.spruce", Blocks.SPRUCE_PLANKS)
		add("planks.darkoak", Blocks.DARK_OAK_PLANKS)
		
		add("fence.oak", Blocks.OAK_FENCE)
		
		add("door.oak.*.*", Blocks.OAK_DOOR, listOf(PaletteMappings.DOOR_HALF, PaletteMappings.FACING_HORIZONTAL))
		add("door.iron.*.*", Blocks.IRON_DOOR, listOf(PaletteMappings.DOOR_HALF, PaletteMappings.FACING_HORIZONTAL))
		
		add("torch", Blocks.TORCH)
		add("torch.*", Blocks.WALL_TORCH, PaletteMappings.FACING_WALL_TORCH)
		add("chest.*", Blocks.CHEST, PaletteMappings.FACING_HORIZONTAL)
		add("furnace.*", Blocks.FURNACE, PaletteMappings.FACING_HORIZONTAL)
		add("stone_button.*", Blocks.STONE_BUTTON, PaletteMappings.FACING_ALL)
		
		add("endportal.inner", ModBlocks.END_PORTAL_INNER)
		add("endportal.frame", ModBlocks.END_PORTAL_FRAME)
		add("endportal.acceptor", ModBlocks.END_PORTAL_ACCEPTOR)
		
		for((suffix, state) in PaletteMappings.VINE_WALLS(ModBlocks.DRY_VINES)){
			add("dry_vines.$suffix", state)
		}
		
		with(forGeneration){
			add("stonebrick", PALETTE_ENTRY_STONE_BRICK)
			add("stonebrick.plain", Blocks.STONE_BRICKS)
			add("stonebrick.chiseled", Blocks.CHISELED_STONE_BRICKS)
			
			add("stonebrick.chiseled.random", Weighted(
				2 to Blocks.STONE_BRICKS,
				1 to Blocks.CHISELED_STONE_BRICKS
			))
			
			add("bookshelf.random", Weighted(
				8 to Blocks.BOOKSHELF,
				1 to Blocks.OAK_PLANKS
			))
		}
		
		with(forDevelopment){
			add("stonebrick", Blocks.STONE_BRICKS)
			add("stonebrick.plain", Blocks.DIAMOND_BLOCK)
			add("stonebrick.chiseled", Blocks.IRON_BLOCK)
			add("stonebrick.chiseled.random", Blocks.EMERALD_BLOCK)
			add("bookshelf.random", Blocks.GOLD_BLOCK)
		}
		
		build()
	}
	
	fun isStoneBrick(block: Block): Boolean{
		return block.isIn(BlockTags.STONE_BRICKS) || (block is BlockSilverfish && block.mimickedBlock.isIn(BlockTags.STONE_BRICKS))
	}
	
	// Loot
	
	val LOOT_GENERIC = Resource.Custom("chests/stronghold_generic")
	val LOOT_LIBRARY_MAIN = Resource.Custom("chests/stronghold_library_main")
	val LOOT_LIBRARY_SECOND = Resource.Custom("chests/stronghold_library_second")
	
	// Pieces (Decorative)
	
	private val PIECES_ROOMS_DECOR_SMALL_GENERAL = arrayOf(
		StrongholdRoom_Decor_Generic("decor.arches_and_poles.nbt"),
		StrongholdRoom_Decor_Generic("decor.ceiling_fountain.nbt"),
		StrongholdRoom_Decor_Generic("decor.decorated_corners.nbt"),
		StrongholdRoom_Decor_Generic("decor.large_fountain.nbt"),
		StrongholdRoom_Decor_Generic("decor.lowered_corners.nbt"),
		StrongholdRoom_Decor_Generic("decor.pole_torches.nbt"),
		StrongholdRoom_Decor_StairSnake("decor.stair_snake.nbt"),
		StrongholdRoom_Decor_Generic("decor.totem_torches.nbt")
	)
	
	private val PIECES_ROOMS_DECOR_SMALL_GLASS = arrayOf(
		StrongholdRoom_Decor_GlassCorners("decor.glass_corners.nbt", DyeColor.WHITE),
		StrongholdRoom_Decor_GlassCorners("decor.glass_corners.nbt", DyeColor.LIGHT_GRAY),
		StrongholdRoom_Decor_GlassCorners("decor.glass_corners.nbt", DyeColor.BLACK),
		StrongholdRoom_Decor_GlassCorners("decor.glass_corners.nbt", DyeColor.PURPLE),
		StrongholdRoom_Decor_GlassCorners("decor.glass_corners.nbt", DyeColor.MAGENTA)
	)
	
	private val PIECES_ROOMS_DECOR_LARGE = arrayOf(
		StrongholdRoom_Decor_Generic("decor.tall_intersection.nbt"),
		StrongholdRoom_Decor_TwoFloorIntersection("decor.two_floor_intersection.nbt")
	)
	
	// Pieces (Chests & Clusters)
	
	private val PIECES_ROOMS_CHESTS_SMALL_GENERAL = arrayOf(
		StrongholdRoom_Chest_DecoratedCorners("chest.decorated_corners.nbt"),
		StrongholdRoom_Chest_WoodenSupports("chest.wooden_supports.nbt")
	)
	
	private val PIECES_ROOMS_CHESTS_SMALL_POOLS = arrayOf(
		StrongholdRoom_Chest_Pool_DrainedOrEmbedded("chest.pool.drained.nbt"),
		StrongholdRoom_Chest_Pool_DrainedOrEmbedded("chest.pool.embedded.nbt"),
		StrongholdRoom_Chest_Pool_Raised("chest.pool.raised.nbt")
	)
	
	private val PIECES_ROOMS_CHESTS_LARGE = arrayOf(
		StrongholdRoom_Chest_TwoFloorCorridor("chest.two_floor_corridor.nbt")
	)
	
	private val PIECES_ROOMS_CLUSTERS_SMALL = arrayOf(
		StrongholdRoom_Cluster_Floating("cluster.floating.nbt"),
		StrongholdRoom_Cluster_Pillar("cluster.pillar.nbt"),
		StrongholdRoom_Cluster_Waterfalls("cluster.waterfalls.nbt")
	)
	
	private val PIECES_ROOMS_CLUSTERS_LARGE = arrayOf(
		StrongholdRoom_Cluster_TwoFloorIntersection("cluster.two_floor_intersection.nbt")
	)
	
	private fun PIECES_CHESTS_CLUSTERS(rand: Random): List<StrongholdAbstractPiece>{
		val chestsPools = PIECES_ROOMS_CHESTS_SMALL_POOLS.toMutableList()
		
		return mutableListOf(
			*PIECES_ROOMS_CHESTS_SMALL_GENERAL,
			*PIECES_ROOMS_CHESTS_SMALL_GENERAL,
			*PIECES_ROOMS_CHESTS_LARGE,
			rand.removeItem(chestsPools),
			
			*PIECES_ROOMS_CLUSTERS_SMALL,
			*PIECES_ROOMS_CLUSTERS_LARGE,
			rand.nextItem(PIECES_ROOMS_CLUSTERS_SMALL),
			
			if (rand.nextBoolean())
				rand.nextItem(chestsPools)
			else
				rand.nextItem(PIECES_ROOMS_CLUSTERS_SMALL)
		)
	}
	
	// Pieces (Traps)
	
	private val PIECES_ROOMS_TRAPS_MAIN = arrayOf(
		StrongholdRoom_Trap_Prison("trap.prison.nbt")
	)
	
	private val PIECES_ROOMS_TRAPS_PICK = arrayOf(
		StrongholdRoom_Trap_CornerHoles("trap.corner_holes.nbt"),
		StrongholdRoom_Trap_TallIntersection("trap.tall_intersection.nbt")
	)
	
	// Pieces (Main)
	
	val PIECES_START = arrayOf(
		StrongholdRoom_Main_Portal("main.portal.nbt")
	)
	
	private val PIECES_ROOMS_MAIN_LIBRARY = arrayOf(
		StrongholdRoom_Main_Library("main.library.nbt")
	)
	
	private val PIECES_ROOMS_MAIN_GENERAL = arrayOf(
		StrongholdRoom_Main_Scriptorium("main.scriptorium.nbt"),
		StrongholdRoom_Main_Workshop("main.workshop.nbt")
	)
	
	fun PIECES_ROOMS(rand: Random): MutableList<StrongholdAbstractPiece>{
		val totalRoomsExceptLibrary = rand.nextInt(26, 27)
		
		val decorSmall = mutableListOf(*PIECES_ROOMS_DECOR_SMALL_GENERAL, *PIECES_ROOMS_DECOR_SMALL_GENERAL)
		val decorGlass = PIECES_ROOMS_DECOR_SMALL_GLASS.toMutableList()
		val decorLarge = PIECES_ROOMS_DECOR_LARGE.toMutableList()
		
		return mutableListOf<StrongholdAbstractPiece>().apply {
			addAll(PIECES_ROOMS_MAIN_GENERAL)
			addAll(PIECES_CHESTS_CLUSTERS(rand))
			
			addAll(PIECES_ROOMS_TRAPS_MAIN)
			add(rand.nextItem(PIECES_ROOMS_TRAPS_PICK))
			
			repeat(rand.nextInt(1, 2)){
				add(rand.removeItem(decorLarge))
			}
			
			repeat(rand.nextInt(1, 3)){
				add(rand.removeItem(decorGlass))
			}
			
			while(size < totalRoomsExceptLibrary){
				add(rand.removeItem(decorSmall))
			}
			
			shuffle(rand)
			add(rand.nextInt(5, 7), rand.nextItem(PIECES_ROOMS_MAIN_LIBRARY))
		}
	}
	
	// Pieces (Corridors)
	
	val PIECES_DOORS = weightedListOf(
		15 to StrongholdDoor_Generic("door.small.nbt"),
		20 to StrongholdDoor_Generic("door.wooden.nbt"),
		25 to StrongholdDoor_Generic("door.grates.nbt"),
		50 to StrongholdDoor_Generic("door.torches.nbt")
	)
	
	private val PIECES_CORRIDOR_MAIN = weightedListOf(
		25 to StrongholdCorridor_Straight(5),
		
		40 to StrongholdCorridor_Straight(3),
		10 to StrongholdCorridor_Straight(3),
		
		50 to StrongholdCorridor_Straight(2),
		15 to StrongholdCorridor_Straight(2),
		10 to StrongholdCorridor_Straight(2),
		
		20 to StrongholdCorridor_Intersection.FOURWAY,
		20 to StrongholdCorridor_Intersection.THREEWAY,
		15 to StrongholdCorridor_Intersection.CORNER,
		15 to StrongholdCorridor_Intersection.CORNER
	)
	
	private val PIECES_CORRIDOR_STAIRS = weightedListOf(
		150 to StrongholdCorridor_Stairs_Straight("corridor.stairs.straight.nbt"),
		 30 to StrongholdCorridor_Stairs_Vertical(SOUTH, NORTH, 1),
		 30 to StrongholdCorridor_Stairs_Vertical(SOUTH, SOUTH, 1),
		 30 to StrongholdCorridor_Stairs_Vertical(SOUTH, EAST, 1),
		 30 to StrongholdCorridor_Stairs_Vertical(SOUTH, WEST, 1),
		 12 to StrongholdCorridor_Stairs_Vertical(SOUTH, NORTH, 2),
		 12 to StrongholdCorridor_Stairs_Vertical(SOUTH, SOUTH, 2),
		 12 to StrongholdCorridor_Stairs_Vertical(SOUTH, EAST, 2),
		 12 to StrongholdCorridor_Stairs_Vertical(SOUTH, WEST, 2)
	)
	
	private val PIECES_CORRIDOR_CHESTS = weightedListOf(
		30 to StrongholdCorridor_Chest_Single("corridor.one_chest.1.nbt"),
		30 to StrongholdCorridor_Chest_Single("corridor.one_chest.2.nbt"),
		30 to StrongholdCorridor_Chest_Single("corridor.one_chest.3.nbt"),
		30 to StrongholdCorridor_Chest_Single("corridor.one_chest.4.nbt"),
		30 to StrongholdCorridor_Chest_Single("corridor.one_chest.5.nbt"),
		10 to StrongholdCorridor_Chest_Double("corridor.two_chests.1.nbt"),
		10 to StrongholdCorridor_Chest_Double("corridor.two_chests.2.nbt")
	)
	
	fun PIECES_CORRIDORS(rand: Random, distanceToPortal: Int): MutableWeightedList<StrongholdAbstractPiece>{
		return PIECES_CORRIDOR_MAIN.mutableCopy().also {
			if (distanceToPortal > 0 && rand.nextBoolean()){
				it.addItem(55, PIECES_CORRIDOR_STAIRS.generateItem(rand))
			}
			
			if (distanceToPortal > 1){
				it.addItem(10, PIECES_CORRIDOR_CHESTS.generateItem(rand))
			}
		}
	}
	
	// Pieces (Relics)
	
	val PIECES_CORRIDOR_RELIC = weightedListOf(
		50 to StrongholdCorridor_Straight(5),
		
		40 to StrongholdCorridor_Straight(3),
		20 to StrongholdCorridor_Straight(3),
		10 to StrongholdCorridor_Straight(3),
		
		40 to StrongholdCorridor_Straight(2),
		15 to StrongholdCorridor_Straight(2),
		
		30 to StrongholdCorridor_Intersection.CORNER,
		30 to StrongholdCorridor_Intersection.CORNER,
		20 to StrongholdCorridor_Intersection.CORNER,
		10 to StrongholdCorridor_Intersection.CORNER,
		
		120 to StrongholdCorridor_Stairs_Straight("corridor.stairs.straight.nbt")
	)
	
	fun PIECES_RELICS(rand: Random): MutableList<StrongholdRoom_Relic>{
		val availableRooms = mutableListOf<(ItemStack) -> StrongholdRoom_Relic>(
			{ stack -> StrongholdRoom_Relic_Dungeon("relic.dungeon.nbt", stack) },
			{ stack -> StrongholdRoom_Relic_Fountains("relic.fountains.nbt", stack) },
			{ stack -> StrongholdRoom_Relic_Hell("relic.hell.nbt", stack) }
		)
		
		val pickedRooms = mutableListOf<StrongholdRoom_Relic>()
		
		for(relicStack in arrayOf(
			ItemStack(ModItems.ENERGY_ORACLE).apply(ItemEnergyOracle.Companion::setupRecipeNBT),
			ItemStack(ModItems.AMULET_OF_RECOVERY)
		)){
			pickedRooms.add(rand.removeItem(availableRooms)(relicStack))
		}
		
		return pickedRooms
	}
	
	// Pieces (Dead ends)
	
	val PIECES_CORRIDOR_DEAD_END = weightedListOf(
		40 to StrongholdCorridor_Straight(6),
		10 to StrongholdCorridor_Straight(5),
		
		40 to StrongholdCorridor_Straight(4),
		15 to StrongholdCorridor_Straight(3),
		15 to StrongholdCorridor_Straight(2),
		
		50 to StrongholdCorridor_Intersection.CORNER,
		40 to StrongholdCorridor_Intersection.CORNER,
		30 to StrongholdCorridor_Intersection.CORNER
	)
	
	fun PIECES_DEAD_ENDS(rand: Random): MutableList<StrongholdAbstractPiece>{
		val shelves = StrongholdRoom_DeadEnd_Shelves("deadend.shelves.nbt")
		val waterfalls = StrongholdRoom_DeadEnd_Waterfalls("deadend.waterfalls.nbt")
		
		return if (rand.nextInt(3) == 0)
			mutableListOf(shelves, shelves, shelves, waterfalls)
		else
			mutableListOf(shelves, shelves, waterfalls)
	}
	
	// Pieces (All)
	
	override val ALL_PIECES
		get() = arrayOf(
			StrongholdCorridor_Straight(2),
			StrongholdCorridor_Straight(3),
			StrongholdCorridor_Straight(5),
			
			StrongholdCorridor_Intersection.CORNER,
			StrongholdCorridor_Intersection.THREEWAY,
			StrongholdCorridor_Intersection.FOURWAY,
			
			*PIECES_CORRIDOR_CHESTS.values.toTypedArray(),
			*PIECES_CORRIDOR_STAIRS.values.toTypedArray(),
			
			*PIECES_DOORS.values.toTypedArray(),
			
			StrongholdRoom_DeadEnd_Shelves("deadend.shelves.nbt"),
			StrongholdRoom_DeadEnd_Waterfalls("deadend.waterfalls.nbt"),
			
			*PIECES_START,
			*PIECES_ROOMS_MAIN_GENERAL,
			*PIECES_ROOMS_MAIN_LIBRARY,
			
			StrongholdRoom_Relic_Dungeon("relic.dungeon.nbt", ItemStack(Items.STICK)),
			StrongholdRoom_Relic_Fountains("relic.fountains.nbt", ItemStack(Items.STICK)),
			StrongholdRoom_Relic_Hell("relic.hell.nbt", ItemStack(Items.STICK)),
			
			*PIECES_ROOMS_CLUSTERS_SMALL,
			*PIECES_ROOMS_CLUSTERS_LARGE,
			
			*PIECES_ROOMS_CHESTS_SMALL_GENERAL,
			*PIECES_ROOMS_CHESTS_SMALL_POOLS,
			*PIECES_ROOMS_CHESTS_LARGE,
			
			*PIECES_ROOMS_TRAPS_MAIN,
			*PIECES_ROOMS_TRAPS_PICK,
			
			*PIECES_ROOMS_DECOR_SMALL_GENERAL,
			*PIECES_ROOMS_DECOR_SMALL_GLASS,
			*PIECES_ROOMS_DECOR_LARGE
		)
}
