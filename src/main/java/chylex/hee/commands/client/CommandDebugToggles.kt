package chylex.hee.commands.client

import chylex.hee.commands.IClientCommand
import chylex.hee.game.world.WorldProviderEndCustom
import chylex.hee.init.ModBlocks
import net.minecraft.command.CommandSource
import net.minecraft.util.text.StringTextComponent

object CommandDebugToggles : IClientCommand {
	override val name = "debug"
	
	override fun executeCommand(sender: CommandSource, args: Array<String>) {
		val name = args.getOrNull(0) ?: return
		
		if (name == "territory") {
			WorldProviderEndCustom.debugMode = !WorldProviderEndCustom.debugMode
			sender.sendFeedback(StringTextComponent("Territory debugging ${if (WorldProviderEndCustom.debugMode) "enabled" else "disabled"}."), false)
		}
		else if (name == "scaffolding") {
			ModBlocks.SCAFFOLDING.enableShape = !ModBlocks.SCAFFOLDING.enableShape
			sender.sendFeedback(StringTextComponent("Scaffolding shape ${if (ModBlocks.SCAFFOLDING.enableShape) "enabled" else "disabled"}."), false)
		}
	}
}
