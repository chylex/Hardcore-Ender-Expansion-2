package chylex.hee.game.commands.sub.client
import chylex.hee.game.commands.sub.ISubCommand
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString

internal object CommandClientHelp : ISubCommand{
	override val name = "1"
	override val usage = "commands.hee.usage"
	
	override fun executeCommand(server: MinecraftServer, sender: ICommandSender, args: Array<out String>){
		sender.sendMessage(TextComponentString("help")) // TODO
	}
	
	override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, args: Array<out String>, targetPos: BlockPos?): MutableList<String>?{
		return null
	}
}
