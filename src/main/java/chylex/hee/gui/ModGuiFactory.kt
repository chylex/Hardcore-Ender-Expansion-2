package chylex.hee.gui
import chylex.hee.HardcoreEnderExpansion
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.fml.client.IModGuiFactory
import net.minecraftforge.fml.client.config.GuiConfig

@Suppress("unused")
class ModGuiFactory : IModGuiFactory{
	override fun hasConfigGui(): Boolean = true
	
	override fun createConfigGui(parentScreen: GuiScreen?): GuiScreen = GuiConfig(parentScreen, HardcoreEnderExpansion.config.configElements, HardcoreEnderExpansion.ID, false, false, GuiConfig.getAbridgedConfigPath(HardcoreEnderExpansion.config.filePath))
	
	override fun runtimeGuiCategories(): MutableSet<IModGuiFactory.RuntimeOptionCategoryElement> = mutableSetOf()
	
	override fun initialize(minecraftInstance: Minecraft?){}
}
