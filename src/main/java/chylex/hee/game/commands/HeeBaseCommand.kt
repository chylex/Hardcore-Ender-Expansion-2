package chylex.hee.game.commands
import chylex.hee.game.commands.sub.ISubCommand
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import org.apache.commons.lang3.ArrayUtils
import java.util.Collections

internal abstract class HeeBaseCommand : CommandBase(){
	protected abstract val allSubCommands: Map<String, ISubCommand>
	protected abstract val defaultSubCommand: ISubCommand
	
	final override fun getName() = "hee"
	
	final override fun getUsage(sender: ICommandSender) = "commands.hee.usage"
	
	abstract override fun getRequiredPermissionLevel(): Int
	
	final override fun execute(server: MinecraftServer?, sender: ICommandSender, args: Array<out String>){
		val noArgs = args.isEmpty()
		val subCommand = if (noArgs) defaultSubCommand else allSubCommands[args[0]]
		
		if (subCommand == null){
			defaultSubCommand.executeCommand(server, sender, args)
		}
		else{
			subCommand.executeCommand(server, sender, if (noArgs) args else ArrayUtils.remove(args, 0))
		}
	}
	
	final override fun getTabCompletions(server: MinecraftServer?, sender: ICommandSender, args: Array<out String>, targetPos: BlockPos?): MutableList<String>{
		return when(args.size){
			0 -> Collections.emptyList()
			1 -> allSubCommands.mapNotNull { if (it.value !== defaultSubCommand && it.key.startsWith(args[0])) it.key else null }.toMutableList()
			else -> allSubCommands[args[0]]?.getTabCompletions(server, sender, ArrayUtils.remove(args, 0), targetPos) ?: Collections.emptyList()
		}
	}
}
