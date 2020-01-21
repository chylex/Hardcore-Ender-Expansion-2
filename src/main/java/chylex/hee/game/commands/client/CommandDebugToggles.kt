package chylex.hee.game.commands.client
import chylex.hee.game.commands.ClientCommandHandler.IClientCommand
import chylex.hee.game.world.WorldProviderEndCustom
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.vanilla.TextComponentString
import net.minecraft.command.CommandSource

object CommandDebugToggles : IClientCommand{
	override val name = "debug"
	
	override fun executeCommand(sender: CommandSource, args: Array<String>){
		val name = args.getOrNull(0) ?: return
		
		if (name == "territory"){
			WorldProviderEndCustom.debugMode = !WorldProviderEndCustom.debugMode
			sender.sendFeedback(TextComponentString("Territory debugging ${if (WorldProviderEndCustom.debugMode) "enabled" else "disabled"}."), false)
		}
		else if (name == "scaffolding"){
			ModBlocks.SCAFFOLDING.enableShape = !ModBlocks.SCAFFOLDING.enableShape
			sender.sendFeedback(TextComponentString("Scaffolding shape ${if (ModBlocks.SCAFFOLDING.enableShape) "enabled" else "disabled"}."), false)
		}
		
		// UPDATE
	}
}
