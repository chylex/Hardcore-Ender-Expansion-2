package chylex.hee.game.commands.sub.server
import chylex.hee.game.commands.sub.ISubCommand
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer

internal object CommandDebugTestWorld : ISubCommand{
	override val name = "testworld"
	override val usage = "commands.hee.testworld.usage"
	override val info = "commands.hee.testworld.info"
	
	override fun executeCommand(server: MinecraftServer?, sender: ICommandSender, args: Array<out String>){
		with(server!!.commandManager){
			executeCommand(sender, "/gamerule keepInventory true")
			executeCommand(sender, "/gamerule doDaylightCycle false")
			executeCommand(sender, "/gamerule doWeatherCycle false")
			
			executeCommand(sender, "/time set 1000")
			executeCommand(sender, "/weather clear")
		}
	}
}
