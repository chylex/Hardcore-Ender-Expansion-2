package chylex.hee.commands

import com.mojang.brigadier.builder.ArgumentBuilder
import net.minecraft.command.CommandSource

interface ICommand {
	val name: String
	
	val permissionLevel: Int
		get() = 2
	
	fun register(builder: ArgumentBuilder<CommandSource, *>)
}
