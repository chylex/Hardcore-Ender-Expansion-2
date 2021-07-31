package chylex.hee.game.command.client

import chylex.hee.game.command.ClientCommandHandler
import chylex.hee.game.command.IClientCommand
import chylex.hee.game.command.server.CommandServerHelp
import net.minecraft.command.CommandSource

object CommandClientHelp : IClientCommand {
	override val name = CommandServerHelp.name
	override val description = CommandServerHelp.description
	
	override fun executeCommand(sender: CommandSource, args: Array<String>) {
		CommandServerHelp.sendCommandListPage(sender, ClientCommandHandler.nonHelpCommands.keys, emptyMap(), "commands.hee.help.header.client", 1, null)
	}
}
