package chylex.hee.game.command

import chylex.hee.HEE
import chylex.hee.client.util.MC
import chylex.hee.game.command.client.CommandClientHelp
import chylex.hee.game.command.client.CommandClientScaffolding
import chylex.hee.game.command.client.CommandDebugToggles
import chylex.hee.init.ModCommands
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import net.minecraftforge.client.event.ClientChatEvent

@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID)
object ClientCommandHandler { // UPDATE
	val nonHelpCommands = listOf(
		CommandClientHelp,
		CommandClientScaffolding,
		CommandDebugToggles
	).associateBy { it.name }
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	fun onClientChat(e: ClientChatEvent) {
		val secondPart = e.message.removePrefix("/${ModCommands.ROOT}")
		
		if (secondPart == e.message) {
			return
		}
		
		val source = MC.player!!.commandSource
		val arguments = secondPart.split(' ').filter { it.isNotEmpty() }
		
		val command = when {
			arguments.isEmpty()                    -> CommandClientHelp
			arguments[0] == CommandClientHelp.name -> CommandClientHelp.takeIf { arguments.size < 2 || arguments[1] == "1" } ?: return
			else                                   -> nonHelpCommands[arguments[0]] ?: return
		}
		
		command.executeCommand(source, arguments.drop(1).toTypedArray())
		e.isCanceled = true
	}
}
