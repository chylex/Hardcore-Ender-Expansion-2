package chylex.hee.system.core
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions

@Name("Hardcore Ender Expansion Coremod")
@TransformerExclusions("chylex.hee.system.core", "kotlin.")
class CoremodPlugin : IFMLLoadingPlugin{
	override fun getASMTransformerClass() = emptyArray<String>()
	
	override fun getAccessTransformerClass() = null
	override fun getSetupClass(): String? = null
	override fun getModContainerClass() = null
	override fun injectData(data: MutableMap<String, Any>?){}
	
}
