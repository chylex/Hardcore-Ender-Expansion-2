package chylex.hee.commands
import chylex.hee.HEE
import chylex.hee.client.MC
import chylex.hee.commands.client.CommandClientHelp
import chylex.hee.commands.client.CommandClientScaffolding
import chylex.hee.commands.client.CommandDebugToggles
import chylex.hee.init.ModCommands
import chylex.hee.system.forge.EventPriority
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.ClientChatEvent

@SubscribeAllEvents(Dist.CLIENT, modid = HEE.ID)
object ClientCommandHandler{ // UPDATE
	val nonHelpCommands = listOf(
		CommandClientHelp,
		CommandClientScaffolding,
		CommandDebugToggles
	).associateBy { it.name }
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	fun onClientChat(e: ClientChatEvent){
		val secondPart = e.message.removePrefix("/${ModCommands.ROOT}")
		
		if (secondPart == e.message){
			return
		}
		
		val source = MC.player!!.commandSource
		val arguments = secondPart.split(' ').filter { it.isNotEmpty() }
		
		val command = when{
			arguments.isEmpty()                    -> CommandClientHelp
			arguments[0] == CommandClientHelp.name -> CommandClientHelp.takeIf { arguments.size < 2 || arguments[1] == "1" } ?: return
			else                                   -> nonHelpCommands[arguments[0]] ?: return
		}
		
		command.executeCommand(source, arguments.drop(1).toTypedArray())
		e.isCanceled = true
	}
}
