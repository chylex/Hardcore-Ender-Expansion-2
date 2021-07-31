package chylex.hee.game.command

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.command.CommandSource
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent

interface ICommand {
	val name: String
	val description: String
	
	val permissionLevel: Int
		get() = 2
	
	val localization: Map<String, String>
		get() = emptyMap()
	
	fun register(builder: ArgumentBuilder<CommandSource, *>)
}

fun ICommand.message(name: String, vararg params: Any): ITextComponent {
	return TranslationTextComponent("commands.hee.${this.name}.$name", *params)
}

fun ICommand.exception(name: String): SimpleCommandExceptionType {
	return SimpleCommandExceptionType(TranslationTextComponent("commands.hee.${this.name}.$name"))
}
