package chylex.hee.game.commands.sub.client
import chylex.hee.game.commands.HeeClientCommand
import chylex.hee.game.commands.sub.ISubCommand
import chylex.hee.game.commands.sub.server.CommandServerHelp
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer

internal object CommandClientHelp : ISubCommand{
	override val name = "1"
	override val usage = "commands.hee.usage"
	override val info = "commands.hee.info"
	
	override fun executeCommand(server: MinecraftServer?, sender: ICommandSender, args: Array<out String>){
		CommandServerHelp.sendCommandListPage(sender, HeeClientCommand.allSubCommands.values, "commands.hee.help.header.client", 1, null)
	}
}
