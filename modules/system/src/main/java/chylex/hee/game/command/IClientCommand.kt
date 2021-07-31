package chylex.hee.game.command

import net.minecraft.command.CommandSource

interface IClientCommand {
	val name: String
	val description: String
	fun executeCommand(sender: CommandSource, args: Array<String>)
}
