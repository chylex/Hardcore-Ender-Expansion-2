package chylex.hee.game.command.client

import chylex.hee.client.render.TerritoryRenderer
import chylex.hee.game.command.IClientCommand
import chylex.hee.game.territory.TerritoryVoid
import chylex.hee.init.ModBlocks
import net.minecraft.command.CommandSource
import net.minecraft.util.text.StringTextComponent

object CommandDebugToggles : IClientCommand {
	override val name = "debug"
	
	private var debug = false
	
	override fun executeCommand(sender: CommandSource, args: Array<String>) {
		val name = args.getOrNull(0) ?: return
		
		if (name == "territory") {
			debug = !debug
			TerritoryRenderer.debug = debug
			TerritoryVoid.debug = debug
			sender.sendFeedback(StringTextComponent("Territory debugging ${if (debug) "enabled" else "disabled"}."), false)
		}
		else if (name == "scaffolding") {
			ModBlocks.SCAFFOLDING.enableShape = !ModBlocks.SCAFFOLDING.enableShape
			sender.sendFeedback(StringTextComponent("Scaffolding shape ${if (ModBlocks.SCAFFOLDING.enableShape) "enabled" else "disabled"}."), false)
		}
	}
}
