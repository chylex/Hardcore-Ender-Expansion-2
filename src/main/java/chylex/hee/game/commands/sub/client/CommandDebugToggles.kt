package chylex.hee.game.commands.sub.client
import chylex.hee.game.commands.sub.ISubCommand
import chylex.hee.game.world.WorldProviderEndCustom
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentString

internal object CommandDebugToggles : ISubCommand{
	override val name = "debug"
	override val usage = "commands.hee.debug.usage"
	override val info = "commands.hee.debug.info"
	
	override fun executeCommand(server: MinecraftServer?, sender: ICommandSender, args: Array<out String>){
		val name = args.getOrNull(0) ?: return
		
		if (name == "territory"){
			WorldProviderEndCustom.debugMode = !WorldProviderEndCustom.debugMode
			sender.sendMessage(TextComponentString("Territory debugging ${if (WorldProviderEndCustom.debugMode) "enabled" else "disabled"}."))
		}
		
		// TODO update
	}
}
