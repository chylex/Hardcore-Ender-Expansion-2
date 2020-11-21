package chylex.hee.init
import chylex.hee.HEE
import chylex.hee.commands.arguments.EnumArgument
import chylex.hee.commands.arguments.ValidatedStringArgument
import chylex.hee.commands.executes
import chylex.hee.commands.server.CommandDebugStructure
import chylex.hee.commands.server.CommandDebugTerritory
import chylex.hee.commands.server.CommandDebugTestWorld
import chylex.hee.commands.server.CommandServerCausatum
import chylex.hee.commands.server.CommandServerHelp
import chylex.hee.commands.server.CommandServerInfusions
import chylex.hee.commands.server.CommandServerLootChest
import chylex.hee.commands.server.CommandServerPortalToken
import chylex.hee.system.Debug
import chylex.hee.system.forge.SubscribeAllEvents
import chylex.hee.system.forge.SubscribeEvent
import net.minecraft.command.Commands.literal
import net.minecraft.command.arguments.ArgumentTypes
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
		CommandDebugTerritory,
		CommandDebugTestWorld
	) else emptyList()
	
	init{
		ArgumentTypes.register("hee:enum", EnumArgument::class.java, EnumArgument.Serializer)
		ArgumentTypes.register("hee:validated_string", ValidatedStringArgument::class.java, ValidatedStringArgument.Serializer)
	}
	
	@SubscribeEvent
	fun onServerStart(e: FMLServerStartingEvent){
		val baseCommand = literal(ROOT).executes(CommandServerHelp, false)
		
		for(command in admin + debug){
			baseCommand.then(literal(command.name).requires { it.hasPermissionLevel(command.permissionLevel) }.apply(command::register))
		}
		
		e.commandDispatcher.register(baseCommand)
	}
}
