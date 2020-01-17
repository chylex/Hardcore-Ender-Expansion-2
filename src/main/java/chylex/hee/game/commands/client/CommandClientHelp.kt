package chylex.hee.game.commands.client
import chylex.hee.game.commands.ClientCommandHandler
import chylex.hee.game.commands.ClientCommandHandler.IClientCommand
import chylex.hee.game.commands.server.CommandServerHelp
import net.minecraft.command.CommandSource

object CommandClientHelp : IClientCommand{
	override val name = "help"
	
	override fun executeCommand(sender: CommandSource, args: Array<String>){
		CommandServerHelp.sendCommandListPage(sender, ClientCommandHandler.nonHelpCommands.keys, emptyMap(), "commands.hee.help.header.client", 1, null)
	}
}
