package chylex.hee.game.commands
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString

object HeeServerCommand : CommandBase(){
	override fun getName(): String = "hee"
	
	override fun getUsage(sender: ICommandSender): String = "commands.hee.usage"
	
	override fun getRequiredPermissionLevel(): Int = 2
	
	override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>){
		var helpPage = if (args.isEmpty()) 1 else args[0].toIntOrNull()
		
		if (helpPage != null){
			if (!server.isDedicatedServer){
				helpPage = (helpPage - 1).coerceAtLeast(1) // TODO remember this when displaying total page count
			}
			
			sender.sendMessage(TextComponentString("server help $helpPage"))
			// TODO show first page of server-only help, and a message to use /hee [page=1] for more pages
		}
		else{
			sender.sendMessage(TextComponentString("server command"))
			// TODO
		}
	}
	
	override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, args: Array<out String>, targetPos: BlockPos?): MutableList<String>{
		return super.getTabCompletions(server, sender, args, targetPos) // TODO
	}
}
