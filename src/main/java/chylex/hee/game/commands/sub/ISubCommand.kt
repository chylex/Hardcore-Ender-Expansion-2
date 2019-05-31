package chylex.hee.game.commands.sub
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos

internal interface ISubCommand{
	val name: String
	val usage: String
	val info: String
	
	fun executeCommand(server: MinecraftServer?, sender: ICommandSender, args: Array<out String>)
	
	fun getTabCompletions(server: MinecraftServer?, sender: ICommandSender, args: Array<out String>, targetPos: BlockPos?): MutableList<String>?{
		return null
	}
	
	companion object{
		fun subCommandMapOf(vararg commands: ISubCommand): Map<String, ISubCommand>{
			return commands.associateBy(ISubCommand::name){ it }
		}
	}
}
