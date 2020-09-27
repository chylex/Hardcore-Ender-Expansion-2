package chylex.hee.commands.server
import chylex.hee.commands.CommandExecutionFunction
import chylex.hee.commands.ICommand
import chylex.hee.commands.returning
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource

object CommandDebugTestWorld : ICommand, CommandExecutionFunction{
	override val name = "testworld"
	
	override fun register(builder: ArgumentBuilder<CommandSource, *>){
		builder.executes(this)
	}
	
	override fun run(ctx: CommandContext<CommandSource>) = returning(1){
		val source = ctx.source
		
		with(source.server.commandManager){
			handleCommand(source, "/gamerule keepInventory true")
			handleCommand(source, "/gamerule doDaylightCycle false")
			handleCommand(source, "/gamerule doWeatherCycle false")
			
			handleCommand(source, "/time set 1000")
			handleCommand(source, "/weather clear")
		}
	}
}
