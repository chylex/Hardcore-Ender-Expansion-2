package chylex.hee.init.factory
import chylex.hee.HEE
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.fml.client.IModGuiFactory
import net.minecraftforge.fml.client.config.GuiConfig

@Suppress("unused")
class ConfigGuiFactory : IModGuiFactory{
	override fun hasConfigGui() = true
	
	override fun createConfigGui(parentScreen: GuiScreen) = GuiConfig(parentScreen, HEE.config.configElements, HEE.ID, false, false, GuiConfig.getAbridgedConfigPath(HEE.config.filePath))
	
	override fun runtimeGuiCategories() = mutableSetOf<IModGuiFactory.RuntimeOptionCategoryElement>()
	
	override fun initialize(mc: Minecraft){}
}
