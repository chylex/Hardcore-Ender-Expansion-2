package chylex.hee.game.command.server

import chylex.hee.game.command.ICommand
import chylex.hee.game.command.util.CommandExecutionFunction
import chylex.hee.game.command.util.simpleCommand
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource

object CommandDebugTestWorld : ICommand, CommandExecutionFunction {
	override val name = "testworld"
	override val description = "converts overworld into a test world"
	
	override fun register(builder: ArgumentBuilder<CommandSource, *>) {
		builder.executes(this)
	}
	
	override fun run(ctx: CommandContext<CommandSource>) = simpleCommand {
		val source = ctx.source
		
		with(source.server.commandManager) {
			handleCommand(source, "/gamerule keepInventory true")
			handleCommand(source, "/gamerule doDaylightCycle false")
			handleCommand(source, "/gamerule doWeatherCycle false")
			handleCommand(source, "/gamerule doInsomnia false")
			handleCommand(source, "/gamerule doPatrolSpawning false")
			handleCommand(source, "/gamerule doTraderSpawning false")
			
			handleCommand(source, "/time set 1000")
			handleCommand(source, "/weather clear")
		}
	}
}
