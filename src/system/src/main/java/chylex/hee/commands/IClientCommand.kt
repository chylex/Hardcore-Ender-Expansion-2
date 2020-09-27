package chylex.hee.commands
import net.minecraft.command.CommandSource

interface IClientCommand{
	val name: String
	fun executeCommand(sender: CommandSource, args: Array<String>)
}
