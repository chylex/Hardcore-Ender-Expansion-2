package chylex.hee.commands.client
import chylex.hee.commands.ClientCommandHandler
import chylex.hee.commands.IClientCommand
import chylex.hee.commands.server.CommandServerHelp
import net.minecraft.command.CommandSource

object CommandClientHelp : IClientCommand{
	override val name = "help"
	
	override fun executeCommand(sender: CommandSource, args: Array<String>){
		CommandServerHelp.sendCommandListPage(sender, ClientCommandHandler.nonHelpCommands.keys, emptyMap(), "commands.hee.help.header.client", 1, null)
	}
}
