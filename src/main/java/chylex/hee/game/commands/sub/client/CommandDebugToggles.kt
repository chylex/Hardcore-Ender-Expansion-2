package chylex.hee.game.commands.sub.client
import chylex.hee.game.commands.sub.ISubCommand
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentString

internal object CommandDebugToggles : ISubCommand{
	override val name = "toggle"
	override val usage = "commands.hee.toggle.usage"
	override val info = "commands.hee.toggle.info"
	
	override fun executeCommand(server: MinecraftServer?, sender: ICommandSender, args: Array<out String>){
		val name = args.getOrNull(0) ?: return
		
		// TODO update
	}
}
