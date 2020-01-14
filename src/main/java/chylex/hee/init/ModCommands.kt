package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.commands.ICommand
import chylex.hee.system.Debug
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import net.minecraft.command.Commands.literal
import net.minecraftforge.fml.event.server.FMLServerStartingEvent

@SubscribeAllEvents(modid = HEE.ID)
object ModCommands{
	const val ROOT = "hee"
	
	val admin = listOf<ICommand>(
	)
	
	val debug = if (Debug.enabled) listOf<ICommand>(
	) else emptyList()
	
	@SubscribeEvent
	fun onServerStart(e: FMLServerStartingEvent){
		val baseCommand = literal(ROOT)
		
		for(command in admin + debug){
			baseCommand.then(literal(command.name).requires { it.hasPermissionLevel(command.permissionLevel) }.apply(command::register))
		}
		
		e.commandDispatcher.register(baseCommand)
	}
}
