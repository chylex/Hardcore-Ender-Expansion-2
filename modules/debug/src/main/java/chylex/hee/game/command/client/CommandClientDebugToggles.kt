package chylex.hee.game.command.client

import chylex.hee.client.render.TerritoryRenderer
import chylex.hee.game.block.BlockScaffolding
import chylex.hee.game.command.IClientCommand
import chylex.hee.game.territory.TerritoryVoid
import net.minecraft.command.CommandSource
import net.minecraft.util.text.StringTextComponent

object CommandClientDebugToggles : IClientCommand {
	override val name = "debug"
	override val description = "access to debug toggles"
	
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
			BlockScaffolding.enableShape = !BlockScaffolding.enableShape
			sender.sendFeedback(StringTextComponent("Scaffolding shape ${if (BlockScaffolding.enableShape) "enabled" else "disabled"}."), false)
		}
	}
}
