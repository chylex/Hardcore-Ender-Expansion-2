package chylex.hee.game.commands.server
import chylex.hee.game.commands.ICommand
import com.mojang.brigadier.builder.ArgumentBuilder
import net.minecraft.command.CommandSource

object CommandDebugTestWorld : ICommand{
	override val name = "testworld"
	
	override fun register(builder: ArgumentBuilder<CommandSource, *>){
		builder.executes {
			val source = it.source
			
			with(source.server.commandManager){
				handleCommand(source, "/gamerule keepInventory true")
				handleCommand(source, "/gamerule doDaylightCycle false")
				handleCommand(source, "/gamerule doWeatherCycle false")
				
				handleCommand(source, "/time set 1000")
				handleCommand(source, "/weather clear")
			}
		}
	}
}
