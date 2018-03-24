package chylex.hee.init
import chylex.hee.HardcoreEnderExpansion
import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = HardcoreEnderExpansion.ID)
object ModBlocks{
	/* TODO val testBlock = Block(Material.GRASS).apply {
		setName("testblock", "test")
	}*/
	
	// Registry
	
	@JvmStatic
	@SubscribeEvent
	fun onRegister(e: RegistryEvent.Register<Block>){
		with(e.registry){
			// TODO
		}
	}
	
	// Utilities
	
	private fun Block.setName(registryName: String, unlocalizedName: String){
		this.setRegistryName(HardcoreEnderExpansion.ID, registryName)
		this.unlocalizedName = "tile.hee.$unlocalizedName"
	}
	
	private inline fun <reified T: TileEntity> tile(registryName: String){
		TileEntity.register("${HardcoreEnderExpansion.ID}:$registryName", T::class.java)
	}
}
