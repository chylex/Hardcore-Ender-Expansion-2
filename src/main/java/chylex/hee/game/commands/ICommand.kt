package chylex.hee.game.commands
import com.mojang.brigadier.builder.ArgumentBuilder
import net.minecraft.command.CommandSource

interface ICommand{
	val name: String
	
	@JvmDefault
	val permissionLevel: Int
		get() = 2
	
	fun register(builder: ArgumentBuilder<CommandSource, *>)
}
