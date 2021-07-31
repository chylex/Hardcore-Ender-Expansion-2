package chylex.hee.game.command.client

import chylex.hee.game.command.IClientCommand
import chylex.hee.game.command.server.CommandDebugStructure
import net.minecraft.command.CommandSource
import net.minecraft.util.text.StringTextComponent
import java.util.prefs.Preferences

object CommandClientScaffolding : IClientCommand {
	override val name = "scaffolding"
	override val description = "sets current structure palette"
	
	private val data
		get() = Preferences.userRoot().node("chylex-hee-scaffolding")
	
	val currentPalette
		get() = data.get("Structure", null)?.let(CommandDebugStructure.structureDescriptions::get)?.PALETTE
	
	val currentFile
		get() = data.get("File", "")!!.ifBlank { "structure.nbt" }
	
	override fun executeCommand(sender: CommandSource, args: Array<String>) {
		val structure = args.getOrNull(0) ?: return
		
		if (!CommandDebugStructure.structureDescriptions.containsKey(structure)) {
			sender.sendFeedback(StringTextComponent("Unknown structure."), false)
			return
		}
		
		with(data) {
			put("Structure", structure)
			put("File", args.getOrElse(1) { "" })
		}
		
		sender.sendFeedback(StringTextComponent("Structure set."), false)
	}
}
