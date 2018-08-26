package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.block.BlockSimple
import chylex.hee.game.block.BlockSlabCustom
import chylex.hee.game.block.BlockStairsCustom
import chylex.hee.game.item.util.Tool.Level.WOOD
import chylex.hee.game.item.util.Tool.Type.PICKAXE
import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemSlab
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = HardcoreEnderExpansion.ID)
object ModBlocks{
	
	// Blocks: Gloomrock
	
	private val buildGloomrock = BlockSimple.Builder(Material.ROCK).apply {
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
	@JvmField val GLOOMROCK_BRICK_SLAB         = BlockSlabCustom.Half(buildGloomrockBricks).apply { setup("gloomrock_brick_slab") }
	@JvmField val GLOOMROCK_BRICK_DOUBLE_SLAB  = BlockSlabCustom.Full(buildGloomrockBricks, GLOOMROCK_BRICK_SLAB).apply { setup("gloomrock_brick_slab_double", "gloomrock_brick_slab") }
	@JvmField val GLOOMROCK_BRICK_STAIRS       = BlockStairsCustom(GLOOMROCK_BRICKS).apply { setup("gloomrock_brick_stairs") }
	@JvmField val GLOOMROCK_SMOOTH             = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth") }
	@JvmField val GLOOMROCK_SMOOTH_SLAB        = BlockSlabCustom.Half(buildGloomrockSmooth).apply { setup("gloomrock_smooth_slab") }
	@JvmField val GLOOMROCK_SMOOTH_DOUBLE_SLAB = BlockSlabCustom.Full(buildGloomrockSmooth, GLOOMROCK_SMOOTH_SLAB).apply { setup("gloomrock_smooth_slab_double", "gloomrock_smooth_slab") }
	@JvmField val GLOOMROCK_SMOOTH_STAIRS      = BlockStairsCustom(GLOOMROCK_SMOOTH).apply { setup("gloomrock_smooth_stairs") }
	@JvmField val GLOOMROCK_SMOOTH_RED         = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_red") }
	@JvmField val GLOOMROCK_SMOOTH_ORANGE      = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_orange") }
	@JvmField val GLOOMROCK_SMOOTH_YELLOW      = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_yellow") }
	@JvmField val GLOOMROCK_SMOOTH_GREEN       = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_green") }
	@JvmField val GLOOMROCK_SMOOTH_CYAN        = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_cyan") }
	@JvmField val GLOOMROCK_SMOOTH_BLUE        = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_blue") }
	@JvmField val GLOOMROCK_SMOOTH_PURPLE      = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_purple") }
	@JvmField val GLOOMROCK_SMOOTH_MAGENTA     = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_magenta") }
	@JvmField val GLOOMROCK_SMOOTH_WHITE       = BlockSimple(buildGloomrockSmooth).apply { setup("gloomrock_smooth_white") }
	
	// Registry
	
	private val basicItemBlock = ::ItemBlock
	
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<Block>){
		with(e.registry){
			register(GLOOMROCK with basicItemBlock)
			register(GLOOMROCK_BRICKS with basicItemBlock)
			register(GLOOMROCK_BRICK_SLAB with ItemSlab(GLOOMROCK_BRICK_SLAB, GLOOMROCK_BRICK_SLAB, GLOOMROCK_BRICK_DOUBLE_SLAB))
			register(GLOOMROCK_BRICK_DOUBLE_SLAB)
			register(GLOOMROCK_BRICK_STAIRS with basicItemBlock)
			register(GLOOMROCK_SMOOTH with basicItemBlock)
			register(GLOOMROCK_SMOOTH_SLAB with ItemSlab(GLOOMROCK_SMOOTH_SLAB, GLOOMROCK_SMOOTH_SLAB, GLOOMROCK_SMOOTH_DOUBLE_SLAB))
			register(GLOOMROCK_SMOOTH_DOUBLE_SLAB)
			register(GLOOMROCK_SMOOTH_STAIRS with basicItemBlock)
			register(GLOOMROCK_SMOOTH_RED with basicItemBlock)
			register(GLOOMROCK_SMOOTH_ORANGE with basicItemBlock)
			register(GLOOMROCK_SMOOTH_YELLOW with basicItemBlock)
			register(GLOOMROCK_SMOOTH_GREEN with basicItemBlock)
			register(GLOOMROCK_SMOOTH_CYAN with basicItemBlock)
			register(GLOOMROCK_SMOOTH_BLUE with basicItemBlock)
			register(GLOOMROCK_SMOOTH_PURPLE with basicItemBlock)
			register(GLOOMROCK_SMOOTH_MAGENTA with basicItemBlock)
			register(GLOOMROCK_SMOOTH_WHITE with basicItemBlock)
		}
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
		temporaryItemBlocks.add(itemBlock.apply { this.registryName = this@with.registryName })
		return this
	}
	
	private infix fun Block.with(itemBlockConstructor: (Block) -> ItemBlock): Block{
		return with(itemBlockConstructor(this))
	}
	
	private inline fun <reified T: TileEntity> tile(registryName: String){
		TileEntity.register("${HardcoreEnderExpansion.ID}:$registryName", T::class.java)
	}
}
