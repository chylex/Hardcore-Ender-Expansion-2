package chylex.hee.init

import chylex.hee.HEE
import chylex.hee.game.command.argument.EnumArgument
import chylex.hee.game.command.argument.ValidatedStringArgument
import chylex.hee.game.command.server.CommandDebugInstability
import chylex.hee.game.command.server.CommandDebugStructure
import chylex.hee.game.command.server.CommandDebugTerritory
import chylex.hee.game.command.server.CommandDebugTestWorld
import chylex.hee.game.command.server.CommandServerCausatum
import chylex.hee.game.command.server.CommandServerHelp
import chylex.hee.game.command.server.CommandServerInfusions
import chylex.hee.game.command.server.CommandServerLootChest
import chylex.hee.game.command.server.CommandServerPortalToken
import chylex.hee.game.command.util.executes
import chylex.hee.system.Debug
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import net.minecraft.command.Commands.literal
import net.minecraft.command.arguments.ArgumentTypes
import net.minecraftforge.event.RegisterCommandsEvent

@SubscribeAllEvents(modid = HEE.ID)
object ModCommands {
	const val ROOT = HEE.ID
	
	val admin = listOf(
		CommandServerHelp,
		CommandServerCausatum,
		CommandServerInfusions,
		CommandServerLootChest,
		CommandServerPortalToken
	)
	
	val debug = if (Debug.enabled) listOf(
		CommandDebugInstability,
		CommandDebugStructure,
		CommandDebugTerritory,
		CommandDebugTestWorld
	)
	else emptyList()
	
	init {
		ArgumentTypes.register("hee:enum", EnumArgument::class.java, EnumArgument.Serializer)
		ArgumentTypes.register("hee:validated_string", ValidatedStringArgument::class.java, ValidatedStringArgument.Serializer)
	}
	
	@SubscribeEvent
	fun onServerStart(e: RegisterCommandsEvent) {
		val baseCommand = literal(ROOT).executes(CommandServerHelp, false)
		
		for (command in admin + debug) {
			baseCommand.then(literal(command.name).requires { it.hasPermissionLevel(command.permissionLevel) }.apply(command::register))
		}
		
		e.dispatcher.register(baseCommand)
	}
}
