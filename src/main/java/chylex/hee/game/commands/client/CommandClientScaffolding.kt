package chylex.hee.game.commands.client
import chylex.hee.game.commands.ClientCommandHandler.IClientCommand
import chylex.hee.game.commands.server.CommandDebugStructure
import chylex.hee.system.migration.vanilla.TextComponentString
import net.minecraft.command.CommandSource
import java.util.prefs.Preferences

object CommandClientScaffolding : IClientCommand{
	override val name = "scaffolding"
	
	private val data
		get() = Preferences.userRoot().node("chylex-hee-scaffolding")
	
	val currentPalette
		get() = data.get("Structure", null)?.let(CommandDebugStructure.structureDescriptions::get)?.PALETTE
	
	val currentFile
		get() = data.get("File", "")!!.ifBlank { "structure.nbt" }
	
	override fun executeCommand(sender: CommandSource, args: Array<String>){
		val structure = args.getOrNull(0) ?: return
		
		if (!CommandDebugStructure.structureDescriptions.containsKey(structure)){
			sender.sendFeedback(TextComponentString("Unknown structure."), false)
			return
		}
		
		with(data){
			put("Structure", structure)
			put("File", args.getOrElse(1){ "" })
		}
		
		sender.sendFeedback(TextComponentString("Structure set."), false)
	}
}
