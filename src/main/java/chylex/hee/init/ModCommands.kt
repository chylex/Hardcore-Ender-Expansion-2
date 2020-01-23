package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.game.commands.server.CommandDebugStructure
import chylex.hee.game.commands.server.CommandDebugTestWorld
import chylex.hee.game.commands.server.CommandServerCausatum
import chylex.hee.game.commands.server.CommandServerHelp
import chylex.hee.game.commands.server.CommandServerInfusions
import chylex.hee.game.commands.server.CommandServerLootChest
import chylex.hee.game.commands.server.CommandServerPortalToken
import chylex.hee.system.Debug
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import net.minecraft.command.Commands.literal
import net.minecraftforge.fml.event.server.FMLServerStartingEvent

@SubscribeAllEvents(modid = HEE.ID)
object ModCommands{
	const val ROOT = "hee"
	
	val admin = listOf(
		CommandServerHelp,
		CommandServerCausatum,
		CommandServerInfusions,
		CommandServerLootChest,
		CommandServerPortalToken
	)
	
	val debug = if (Debug.enabled) listOf(
		CommandDebugStructure,
		CommandDebugTestWorld
	) else emptyList()
	
	@SubscribeEvent
	fun onServerStart(e: FMLServerStartingEvent){
		val baseCommand = literal(ROOT).executes(CommandServerHelp::execute)
		
		for(command in admin + debug){
			baseCommand.then(literal(command.name).requires { it.hasPermissionLevel(command.permissionLevel) }.apply(command::register))
		}
		
		e.commandDispatcher.register(baseCommand)
	}
}
