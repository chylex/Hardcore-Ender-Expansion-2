package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.block.BlockAncientCobweb
import chylex.hee.game.block.BlockEndPowderOre
import chylex.hee.game.block.BlockEndium
import chylex.hee.game.block.BlockEnergyCluster
import chylex.hee.game.block.BlockPillarCustom
import chylex.hee.game.block.BlockSimple
import chylex.hee.game.block.BlockSlabCustom
import chylex.hee.game.block.BlockStairsCustom
import chylex.hee.game.block.BlockWallCustom
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.block.material.Materials
import chylex.hee.game.item.ItemAncientCobweb
import chylex.hee.game.item.util.Tool.Level.DIAMOND
import chylex.hee.game.item.util.Tool.Level.IRON
import chylex.hee.game.item.util.Tool.Level.STONE
import chylex.hee.game.item.util.Tool.Level.WOOD
import chylex.hee.game.item.util.Tool.Type.PICKAXE
import chylex.hee.init.ModCreativeTabs.OrderedCreativeTab
import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.MapColor
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemSlab
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.GameRegistry

@EventBusSubscriber(modid = HardcoreEnderExpansion.ID)
object ModBlocks{
	
	// Blocks: Building (Uncategorized)
	
	private val buildEndiumBlock = BlockSimple.Builder(Materials.SOLID_WITH_TOOL).apply {
		harvestTool = Pair(IRON, PICKAXE)
		harvestHardness = 6.2F
		explosionResistance = 20.0F
		
		soundType = SoundType.METAL
		mapColor = MapColor.BLUE
	}
	
	@JvmField val STONE_BRICK_WALL = BlockWallCustom(Blocks.STONEBRICK).apply { setup("stone_brick_wall") }
	@JvmField val ENDIUM_BLOCK     = BlockEndium(buildEndiumBlock).apply { setup("endium_block") }
	
	// Blocks: Building (Gloomrock)
	
	private val buildGloomrock = BlockSimple.Builder(Materials.SOLID_WITH_TOOL).apply {
		harvestTool = Pair(WOOD, PICKAXE)
		harvestHardness = 1.6F
		explosionResistance = 7.0F
		
		soundType = SoundType.STONE
		mapColor = MapColor.BLACK
	}
	
	private val buildGloomrockBricks = buildGloomrock.clone {
		harvestHardness = 2.8F
		explosionResistance = 10.0F
	}
	
	private val buildGloomrockSmooth = buildGloomrock.clone {
		harvestHardness = 2.0F
		explosionResistance = 8.0F
	}
	
	@JvmField val GLOOMROCK                    = BlockSimple(buildGloomrock).apply { setup("gloomrock") }
	@JvmField val GLOOMROCK_BRICKS             = BlockSimple(buildGloomrockBricks).apply { setup("gloomrock_bricks") }
	@JvmField val GLOOMROCK_BRICK_STAIRS       = BlockStairsCustom(GLOOMROCK_BRICKS).apply { setup("gloomrock_brick_stairs") }
	@JvmField val GLOOMROCK_BRICK_SLAB         = BlockSlabCustom.Half(buildGloomrockBricks).apply { setup("gloomrock_brick_slab") }
	@JvmField val GLOOMROCK_BRICK_DOUBLE_SLAB  = BlockSlabCustom.Full(buildGloomrockBricks, GLOOMROCK_BRICK_SLAB).apply { setup("gloomrock_brick_slab_double", "gloomrock_brick_slab") }
	@JvmField val GLOOMROCK_SMOOTH             = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth") }
	@JvmField val GLOOMROCK_SMOOTH_STAIRS      = BlockStairsCustom(GLOOMROCK_SMOOTH).apply { setup("gloomrock_smooth_stairs") }
	@JvmField val GLOOMROCK_SMOOTH_SLAB        = BlockSlabCustom.Half(buildGloomrockSmooth).apply { setup("gloomrock_smooth_slab") }
	@JvmField val GLOOMROCK_SMOOTH_DOUBLE_SLAB = BlockSlabCustom.Full(buildGloomrockSmooth, GLOOMROCK_SMOOTH_SLAB).apply { setup("gloomrock_smooth_slab_double", "gloomrock_smooth_slab") }
	@JvmField val GLOOMROCK_SMOOTH_RED         = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_red") }
	@JvmField val GLOOMROCK_SMOOTH_ORANGE      = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_orange") }
	@JvmField val GLOOMROCK_SMOOTH_YELLOW      = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_yellow") }
	@JvmField val GLOOMROCK_SMOOTH_GREEN       = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_green") }
	@JvmField val GLOOMROCK_SMOOTH_CYAN        = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_cyan") }
	@JvmField val GLOOMROCK_SMOOTH_BLUE        = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_blue") }
	@JvmField val GLOOMROCK_SMOOTH_PURPLE      = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_purple") }
	@JvmField val GLOOMROCK_SMOOTH_MAGENTA     = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_magenta") }
	@JvmField val GLOOMROCK_SMOOTH_WHITE       = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_white") }
	
	// Blocks: Building (Obsidian)
	
	private val buildObsidianCustom = BlockSimple.Builder(Materials.SOLID_WITH_TOOL).apply {
		harvestTool = Pair(DIAMOND, PICKAXE)
		harvestHardness = 20F
		explosionResistance = 500F
		
		soundType = SoundType.STONE
		mapColor = MapColor.BLACK
	}
	
	private val buildObsidianCustomLit = buildObsidianCustom.clone {
		lightLevel = 15
	}
	
	@JvmField val OBSIDIAN_STAIRS       = BlockStairsCustom(Blocks.OBSIDIAN).apply { setup("obsidian_stairs") }
	@JvmField val OBSIDIAN_SMOOTH       = BlockSimple(buildObsidianCustom).apply { setup("obsidian_smooth") }
	@JvmField val OBSIDIAN_CHISELED     = BlockSimple(buildObsidianCustom).apply { setup("obsidian_chiseled") }
	@JvmField val OBSIDIAN_PILLAR       = BlockPillarCustom(buildObsidianCustom).apply { setup("obsidian_pillar") }
	@JvmField val OBSIDIAN_SMOOTH_LIT   = BlockSimple(buildObsidianCustomLit).apply { setup("obsidian_smooth_lit") }
	@JvmField val OBSIDIAN_CHISELED_LIT = BlockSimple(buildObsidianCustomLit).apply { setup("obsidian_chiseled_lit") }
	@JvmField val OBSIDIAN_PILLAR_LIT   = BlockPillarCustom(buildObsidianCustomLit).apply { setup("obsidian_pillar_lit") }
	
	// Blocks: Ores
	
	private val buildEndOre = BlockSimple.Builder(Materials.SOLID_WITH_TOOL).apply {
		soundType = SoundType.STONE
		mapColor = MapColor.SAND
	}
	
	private val buildEndPowderOre = buildEndOre.clone {
		harvestTool = Pair(STONE, PICKAXE)
		harvestHardness = 2.0F
		explosionResistance = 9.0F
	}
	
	private val buildEndiumOre = buildEndOre.clone {
		harvestTool = Pair(IRON, PICKAXE)
		harvestHardness = 5.0F
		explosionResistance = 16.5F
	}
	
	@JvmField val END_POWDER_ORE = BlockEndPowderOre(buildEndPowderOre).apply { setup("end_powder_ore") }
	@JvmField val ENDIUM_ORE     = BlockEndium(buildEndiumOre).apply { setup("endium_ore") }
	
	// Blocks: Decorative (Uncategorized)
	
	@JvmField val ANCIENT_COBWEB = BlockAncientCobweb().apply { setup("ancient_cobweb") }
	
	// Blocks: Energy
	
	@JvmField val ENERGY_CLUSTER = BlockEnergyCluster().apply { setup("energy_cluster") }
	
	// Registry
	
	private val basicItemBlock = ::ItemBlock
	
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<Block>){
		with(e.registry){
			register(STONE_BRICK_WALL with basicItemBlock)
			register(ENDIUM_BLOCK with basicItemBlock)
			
			register(GLOOMROCK with basicItemBlock)
			register(GLOOMROCK_BRICKS with basicItemBlock)
			register(GLOOMROCK_BRICK_STAIRS with basicItemBlock)
			register(GLOOMROCK_BRICK_SLAB with ItemSlab(GLOOMROCK_BRICK_SLAB, GLOOMROCK_BRICK_SLAB, GLOOMROCK_BRICK_DOUBLE_SLAB).apply { hasSubtypes = false })
			register(GLOOMROCK_BRICK_DOUBLE_SLAB)
			register(GLOOMROCK_SMOOTH with basicItemBlock)
			register(GLOOMROCK_SMOOTH_STAIRS with basicItemBlock)
			register(GLOOMROCK_SMOOTH_SLAB with ItemSlab(GLOOMROCK_SMOOTH_SLAB, GLOOMROCK_SMOOTH_SLAB, GLOOMROCK_SMOOTH_DOUBLE_SLAB).apply { hasSubtypes = false })
			register(GLOOMROCK_SMOOTH_DOUBLE_SLAB)
			register(GLOOMROCK_SMOOTH_RED with basicItemBlock)
			register(GLOOMROCK_SMOOTH_ORANGE with basicItemBlock)
			register(GLOOMROCK_SMOOTH_YELLOW with basicItemBlock)
			register(GLOOMROCK_SMOOTH_GREEN with basicItemBlock)
			register(GLOOMROCK_SMOOTH_CYAN with basicItemBlock)
			register(GLOOMROCK_SMOOTH_BLUE with basicItemBlock)
			register(GLOOMROCK_SMOOTH_PURPLE with basicItemBlock)
			register(GLOOMROCK_SMOOTH_MAGENTA with basicItemBlock)
			register(GLOOMROCK_SMOOTH_WHITE with basicItemBlock)
			
			register(OBSIDIAN_STAIRS with basicItemBlock)
			register(OBSIDIAN_SMOOTH with basicItemBlock)
			register(OBSIDIAN_CHISELED with basicItemBlock)
			register(OBSIDIAN_PILLAR with basicItemBlock)
			register(OBSIDIAN_SMOOTH_LIT with basicItemBlock)
			register(OBSIDIAN_CHISELED_LIT with basicItemBlock)
			register(OBSIDIAN_PILLAR_LIT with basicItemBlock)
			
			register(END_POWDER_ORE with basicItemBlock)
			register(ENDIUM_ORE with basicItemBlock)
			
			register(ANCIENT_COBWEB with ItemAncientCobweb(ANCIENT_COBWEB))
			
			register(ENERGY_CLUSTER with basicItemBlock)
		}
		
		tile<TileEntityEnergyCluster>("energy_cluster")
		
		// vanilla modifications
		
		Blocks.END_BRICKS.setHardness(1.0F).setResistance(4.0F)
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onRegisterItemBlocks(e: RegistryEvent.Register<Item>){
		temporaryItemBlocks.forEach(e.registry::register)
		temporaryItemBlocks.clear()
	}
	
	// Utilities
	
	private val temporaryItemBlocks = mutableListOf<ItemBlock>()
	
	private fun Block.setup(registryName: String, unlocalizedName: String = "", inCreativeTab: Boolean = true){
		this.setRegistryName(HardcoreEnderExpansion.ID, registryName)
		this.unlocalizedName = "hee.${if (unlocalizedName.isEmpty()) registryName else unlocalizedName}"
		
		if (inCreativeTab){
			this.setCreativeTab(ModCreativeTabs.main)
		}
	}
	
	private infix fun Block.with(itemBlock: ItemBlock): Block{
		temporaryItemBlocks.add(itemBlock.also { it.registryName = this.registryName })
		(itemBlock.creativeTab as OrderedCreativeTab?)?.registerOrder(itemBlock)
		return this
	}
	
	private infix fun Block.with(itemBlockConstructor: (Block) -> ItemBlock): Block{
		return with(itemBlockConstructor(this))
	}
	
	private inline fun <reified T: TileEntity> tile(registryName: String){
		GameRegistry.registerTileEntity(T::class.java, "${HardcoreEnderExpansion.ID}:$registryName")
	}
}
