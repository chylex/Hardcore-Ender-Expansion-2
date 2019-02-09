package chylex.hee.game.world.feature.stronghold
import chylex.hee.game.block.util.FutureBlocks
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
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Relic_Dungeon
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Relic_Fountains
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Relic_Hell
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Trap_CornerHoles
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Trap_Prison
import chylex.hee.game.world.feature.stronghold.piece.StrongholdRoom_Trap_TallIntersection
import chylex.hee.game.world.structure.IBlockPicker.Weighted.Companion.Weighted
import chylex.hee.game.world.structure.file.PaletteBuilder
import chylex.hee.game.world.structure.file.PaletteMappings
import chylex.hee.game.world.util.Size
import chylex.hee.init.ModBlocks
import chylex.hee.system.Resource
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST

object StrongholdPieces{
	val STRUCTURE_SIZE = Size(256, 32, 256)
	
	val PALETTE_ENTRY_STONE_BRICK = Weighted(
		610 to FutureBlocks.STONE_BRICKS,
		175 to FutureBlocks.MOSSY_STONE_BRICKS,
		175 to FutureBlocks.CRACKED_STONE_BRICKS,
		 20 to FutureBlocks.INFESTED_STONE_BRICKS,
		 12 to FutureBlocks.INFESTED_MOSSY_STONE_BRICKS,
		  8 to FutureBlocks.INFESTED_CRACKED_STONE_BRICKS
	)
	
	val PALETTE = with(PaletteBuilder()){
		add("air", Blocks.AIR)
		add("bookshelf", Blocks.BOOKSHELF)
		add("obsidian", Blocks.OBSIDIAN)
		add("ethereallantern", ModBlocks.ETHEREAL_LANTERN)
		add("workbench", Blocks.CRAFTING_TABLE)
		add("ironbars", Blocks.IRON_BARS)
		add("redstone", Blocks.REDSTONE_WIRE)
		add("water", Blocks.WATER)
		add("lava", Blocks.LAVA)
		
		add("stonebrick", PALETTE_ENTRY_STONE_BRICK)
		add("stonebrick.plain", FutureBlocks.STONE_BRICKS)
		add("stonebrick.chiseled", FutureBlocks.CHISELED_STONE_BRICKS)
		
		add("stonebrick.chiseled.random", Weighted(
			2 to FutureBlocks.STONE_BRICKS,
			1 to FutureBlocks.CHISELED_STONE_BRICKS
		))
		
		add("wall.stonebrick", ModBlocks.STONE_BRICK_WALL)
		
		add("slab.stone.*", FutureBlocks.STONE_SLAB, PaletteMappings.SLAB_HALF)
		add("slab.stonebrick.*", FutureBlocks.STONE_BRICK_SLAB, PaletteMappings.SLAB_HALF)
		add("slab.spruce.*", FutureBlocks.SPRUCE_SLAB, PaletteMappings.SLAB_HALF)
		add("slab.stone.double", FutureBlocks.DOUBLE_STONE_SLAB)
		
		add("stairs.stonebrick.*.*", Blocks.STONE_BRICK_STAIRS, listOf(PaletteMappings.STAIR_FLIP, PaletteMappings.FACING_HORIZONTAL))
		add("stairs.spruce.*.*", Blocks.SPRUCE_STAIRS, listOf(PaletteMappings.STAIR_FLIP, PaletteMappings.FACING_HORIZONTAL))
		add("stairs.darkoak.*.*", Blocks.DARK_OAK_STAIRS, listOf(PaletteMappings.STAIR_FLIP, PaletteMappings.FACING_HORIZONTAL))
		
		add("log.spruce.*", FutureBlocks.SPRUCE_LOG, PaletteMappings.FACING_AXIS_LOGS)
		add("log.darkoak.*", FutureBlocks.DARK_OAK_LOG, PaletteMappings.FACING_AXIS_LOGS)
		
		add("planks.oak", FutureBlocks.OAK_PLANKS)
		add("planks.spruce", FutureBlocks.SPRUCE_PLANKS)
		add("planks.darkoak", FutureBlocks.DARK_OAK_PLANKS)
		
		add("fence.oak", Blocks.OAK_FENCE)
		
		add("door.oak.*.*", Blocks.OAK_DOOR, listOf(PaletteMappings.DOOR_HALF, PaletteMappings.FACING_HORIZONTAL))
		add("door.iron.*.*", Blocks.IRON_DOOR, listOf(PaletteMappings.DOOR_HALF, PaletteMappings.FACING_HORIZONTAL))
		
		add("torch.*", Blocks.TORCH, PaletteMappings.FACING_TORCH)
		add("chest.*", Blocks.CHEST, PaletteMappings.FACING_HORIZONTAL)
		add("furnace.*", Blocks.FURNACE, PaletteMappings.FACING_HORIZONTAL)
		add("stone_button.*", Blocks.STONE_BUTTON, PaletteMappings.FACING_ALL)
		
		add("bookshelf.random", Weighted(
			8 to Blocks.BOOKSHELF.defaultState,
			1 to FutureBlocks.OAK_PLANKS
		))
		
		add("endportal.inner", ModBlocks.END_PORTAL_INNER)
		add("endportal.frame", ModBlocks.END_PORTAL_FRAME)
		add("endportal.acceptor", ModBlocks.END_PORTAL_ACCEPTOR)
		
		for((suffix, state) in PaletteMappings.VINE_WALLS(ModBlocks.DRY_VINES)){
			add("dry_vines.$suffix", state)
		}
		
		build()
	}
	
	fun isStoneBrick(block: Block): Boolean{
		return block === Blocks.STONEBRICK || block === Blocks.MONSTER_EGG
	}
	
	val LOOT_GENERIC = Resource.Custom("chests/stronghold_generic")
	val LOOT_LIBRARY_MAIN = Resource.Custom("chests/stronghold_library_main")
	val LOOT_LIBRARY_SECOND = Resource.Custom("chests/stronghold_library_second")
	
	val CORRIDORS_PATH = arrayOf(
		StrongholdCorridor_Straight(2),
		StrongholdCorridor_Straight(3),
		StrongholdCorridor_Straight(5)
	)
	
	val CORRIDORS_INTERSECTION = arrayOf(
		StrongholdCorridor_Intersection(SOUTH, WEST),
		StrongholdCorridor_Intersection(SOUTH, WEST, EAST),
		StrongholdCorridor_Intersection(SOUTH, WEST, EAST, NORTH)
	)
	
	val CORRIDORS_CHEST = arrayOf(
		StrongholdCorridor_Chest_Single("corridor.one_chest.1.nbt"),
		StrongholdCorridor_Chest_Single("corridor.one_chest.2.nbt"),
		StrongholdCorridor_Chest_Single("corridor.one_chest.3.nbt"),
		StrongholdCorridor_Chest_Single("corridor.one_chest.4.nbt"),
		StrongholdCorridor_Chest_Single("corridor.one_chest.5.nbt"),
		StrongholdCorridor_Chest_Double("corridor.two_chests.1.nbt"),
		StrongholdCorridor_Chest_Double("corridor.two_chests.2.nbt")
	)
	
	val CORRIDORS_STAIRS = arrayOf(
		StrongholdCorridor_Stairs_Straight("corridor.stairs.straight.nbt"),
		StrongholdCorridor_Stairs_Vertical(SOUTH, NORTH, 1),
		StrongholdCorridor_Stairs_Vertical(SOUTH, SOUTH, 1),
		StrongholdCorridor_Stairs_Vertical(SOUTH, EAST, 1),
		StrongholdCorridor_Stairs_Vertical(SOUTH, WEST, 1),
		StrongholdCorridor_Stairs_Vertical(SOUTH, NORTH, 2),
		StrongholdCorridor_Stairs_Vertical(SOUTH, SOUTH, 2),
		StrongholdCorridor_Stairs_Vertical(SOUTH, EAST, 2),
		StrongholdCorridor_Stairs_Vertical(SOUTH, WEST, 2)
	)
	
	val DOORS = arrayOf(
		StrongholdDoor_Generic("door.grates.nbt"),
		StrongholdDoor_Generic("door.small.nbt"),
		StrongholdDoor_Generic("door.torches.nbt"),
		StrongholdDoor_Generic("door.wooden.nbt")
	)
	
	val DEAD_ENDS = arrayOf(
		StrongholdRoom_DeadEnd_Shelves("deadend.shelves.nbt"),
		StrongholdRoom_DeadEnd_Waterfalls("deadend.waterfalls.nbt")
	)
	
	val ROOMS_START = arrayOf(
		StrongholdRoom_Main_Portal("main.portal.nbt")
	)
	
	val ROOMS_MAIN = arrayOf(
		StrongholdRoom_Main_Library("main.library.nbt"),
		StrongholdRoom_Main_Scriptorium("main.scriptorium.nbt"),
		StrongholdRoom_Main_Workshop("main.workshop.nbt")
	)
	
	val ROOMS_RELIC = arrayOf(
		StrongholdRoom_Relic_Dungeon("relic.dungeon.nbt", ItemStack(Items.STICK)),
		StrongholdRoom_Relic_Fountains("relic.fountains.nbt", ItemStack(Items.STICK)),
		StrongholdRoom_Relic_Hell("relic.hell.nbt", ItemStack(Items.STICK))
	)
	
	val ROOMS_CLUSTER = arrayOf(
		StrongholdRoom_Cluster_Floating("cluster.floating.nbt"),
		StrongholdRoom_Cluster_Pillar("cluster.pillar.nbt"),
		StrongholdRoom_Cluster_TwoFloorIntersection("cluster.two_floor_intersection.nbt"),
		StrongholdRoom_Cluster_Waterfalls("cluster.waterfalls.nbt")
	)
	
	val ROOMS_CHEST = arrayOf(
		StrongholdRoom_Chest_DecoratedCorners("chest.decorated_corners.nbt"),
		StrongholdRoom_Chest_Pool_DrainedOrEmbedded("chest.pool.drained.nbt"),
		StrongholdRoom_Chest_Pool_DrainedOrEmbedded("chest.pool.embedded.nbt"),
		StrongholdRoom_Chest_Pool_Raised("chest.pool.raised.nbt"),
		StrongholdRoom_Chest_TwoFloorCorridor("chest.two_floor_corridor.nbt"),
		StrongholdRoom_Chest_WoodenSupports("chest.wooden_supports.nbt")
	)
	
	val ROOMS_TRAP = arrayOf(
		StrongholdRoom_Trap_CornerHoles("trap.corner_holes.nbt"),
		StrongholdRoom_Trap_Prison("trap.prison.nbt"),
		StrongholdRoom_Trap_TallIntersection("trap.tall_intersection.nbt")
	)
	
	val ROOMS_DECOR = arrayOf(
		StrongholdRoom_Decor_Generic("decor.arches_and_poles.nbt"),
		StrongholdRoom_Decor_Generic("decor.ceiling_fountain.nbt"),
		StrongholdRoom_Decor_Generic("decor.decorated_corners.nbt"),
		StrongholdRoom_Decor_GlassCorners("decor.glass_corners.nbt", EnumDyeColor.WHITE),
		StrongholdRoom_Decor_GlassCorners("decor.glass_corners.nbt", EnumDyeColor.SILVER),
		StrongholdRoom_Decor_GlassCorners("decor.glass_corners.nbt", EnumDyeColor.BLACK),
		StrongholdRoom_Decor_GlassCorners("decor.glass_corners.nbt", EnumDyeColor.PURPLE),
		StrongholdRoom_Decor_GlassCorners("decor.glass_corners.nbt", EnumDyeColor.MAGENTA),
		StrongholdRoom_Decor_Generic("decor.large_fountain.nbt"),
		StrongholdRoom_Decor_Generic("decor.lowered_corners.nbt"),
		StrongholdRoom_Decor_Generic("decor.pole_torches.nbt"),
		StrongholdRoom_Decor_StairSnake("decor.stair_snake.nbt"),
		StrongholdRoom_Decor_Generic("decor.tall_intersection.nbt"),
		StrongholdRoom_Decor_Generic("decor.totem_torches.nbt"),
		StrongholdRoom_Decor_TwoFloorIntersection("decor.two_floor_intersection.nbt")
	)
	
	val ALL_PIECES = arrayOf(
		*CORRIDORS_PATH,
		*CORRIDORS_INTERSECTION,
		*CORRIDORS_CHEST,
		*CORRIDORS_STAIRS,
		*DOORS,
		*DEAD_ENDS,
		*ROOMS_START,
		*ROOMS_MAIN,
		*ROOMS_RELIC,
		*ROOMS_CLUSTER,
		*ROOMS_CHEST,
		*ROOMS_TRAP,
		*ROOMS_DECOR
	)
}
