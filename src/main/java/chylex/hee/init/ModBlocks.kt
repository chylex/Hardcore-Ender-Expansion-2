package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = HardcoreEnderExpansion.ID)
object ModBlocks{
	
	// Registry
	
	private val basicItemBlock = ::ItemBlock
	
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<Block>){
		with(e.registry){
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
