package chylex.hee.game.world.feature.stronghold
import chylex.hee.game.block.util.FutureBlocks
import chylex.hee.game.world.structure.IBlockPicker.Weighted.Companion.Weighted
import chylex.hee.game.world.structure.file.PaletteBuilder
import chylex.hee.game.world.structure.file.PaletteMappings
import chylex.hee.init.ModBlocks
import chylex.hee.system.Resource
import net.minecraft.init.Blocks

object StrongholdPieces{
	val PALETTE_ENTRY_STONE_BRICK = Weighted(
		555 to FutureBlocks.STONE_BRICKS,
		150 to FutureBlocks.MOSSY_STONE_BRICKS,
		150 to FutureBlocks.CRACKED_STONE_BRICKS,
		 70 to FutureBlocks.INFESTED_STONE_BRICKS,
		 45 to FutureBlocks.INFESTED_MOSSY_STONE_BRICKS,
		 30 to FutureBlocks.INFESTED_CRACKED_STONE_BRICKS
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
	
	val LOOT_GENERIC = Resource.Custom("chests/stronghold_generic")
	val LOOT_LIBRARY_MAIN = Resource.Custom("chests/stronghold_library_main")
	val LOOT_LIBRARY_SECOND = Resource.Custom("chests/stronghold_library_second")
}
