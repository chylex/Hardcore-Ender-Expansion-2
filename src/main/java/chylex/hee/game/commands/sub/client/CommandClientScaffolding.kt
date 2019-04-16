package chylex.hee.game.commands.sub.client
import chylex.hee.game.commands.sub.ISubCommand
import chylex.hee.game.commands.sub.server.CommandDebugStructure
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentString
import java.util.prefs.Preferences

internal object CommandClientScaffolding : ISubCommand{
	override val name = "scaffolding"
	override val usage = "commands.hee.scaffolding.usage"
	override val info = "commands.hee.scaffolding.info"
	
	private val data
		get() = Preferences.userRoot().node("chylex-hee-scaffolding")
	
	val currentPalette
		get() = data.get("Structure", null)?.let(CommandDebugStructure.structureDescriptions::get)?.PALETTE
	
	val currentFile
		get() = data.get("File", "").ifBlank { "structure.nbt" }
	
	override fun executeCommand(server: MinecraftServer?, sender: ICommandSender, args: Array<out String>){
		val structure = args.getOrNull(0) ?: return
		
		if (!CommandDebugStructure.structureDescriptions.containsKey(structure)){
			sender.sendMessage(TextComponentString("Unknown structure."))
			return
		}
		
		with(data){
			put("Structure", structure)
			put("File", args.getOrElse(1){ "" })
		}
		
		sender.sendMessage(TextComponentString("Structure set."))
	}
}
