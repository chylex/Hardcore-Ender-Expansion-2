package chylex.hee
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

@Mod(modid = "hee", useMetadata = true, modLanguageAdapter = "chylex.hee.system.core.KotlinAdapter")
object HardcoreEnderExpansion {
    @EventHandler
	fun onPreInit(e: FMLPreInitializationEvent) {
	}
}
