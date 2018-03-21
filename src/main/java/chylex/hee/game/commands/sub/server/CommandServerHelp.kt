package chylex.hee.game.commands.sub.server
import chylex.hee.game.commands.sub.ISubCommand
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString

internal object CommandServerHelp : ISubCommand{
	override val name = "1"
	override val usage = "commands.hee.usage"
	
	override fun executeCommand(server: MinecraftServer, sender: ICommandSender, args: Array<out String>){
		var helpPage = if (args.isEmpty()) 1 else args[0].toIntOrNull()
		
		if (helpPage != null){
			if (!server.isDedicatedServer){
				helpPage = (helpPage - 1).coerceAtLeast(1) // TODO remember this when displaying total page count
			}
			
			sender.sendMessage(TextComponentString("server help $helpPage"))
			// TODO show first page of server-only help, and a message to use /hee [page=1] for more pages
		}
		else{
			throw WrongUsageException(usage)
		}
	}
	
	override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, args: Array<out String>, targetPos: BlockPos?): MutableList<String>?{
		return null
	}
}
