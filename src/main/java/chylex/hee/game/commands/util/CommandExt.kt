package chylex.hee.game.commands.util
import chylex.hee.game.commands.ICommand
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.command.CommandSource
import net.minecraft.command.arguments.LocationInput
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent

fun <C, T : ArgumentBuilder<CommandSource, T>> ArgumentBuilder<CommandSource, T>.executes(function: (CommandContext<CommandSource>, C) -> Int, extra: C): T{
	return this.executes { function(it, extra) }
}

inline fun returning(result: Int, code: () -> Unit): Int{
	code()
	return result
}

fun ICommand.message(name: String, vararg params: Any): ITextComponent{
	return TextComponentTranslation("commands.hee.${this.name}.$name", *params)
}

fun ICommand.exception(name: String): SimpleCommandExceptionType{
	return SimpleCommandExceptionType(TextComponentTranslation("commands.hee.${this.name}.$name"))
}

inline fun <reified T : Enum<T>> CommandContext<CommandSource>.getEnum(name: String): T{
	return this.getArgument(name, T::class.java)
}

fun CommandContext<CommandSource>.getString(name: String): String{
	return this.getArgument(name, String::class.java)
}

fun CommandContext<CommandSource>.getInt(name: String): Int{
	return this.getArgument(name, Int::class.java)
}

fun CommandContext<CommandSource>.getLong(name: String): Long{
	return this.getArgument(name, Long::class.java)
}

fun CommandContext<CommandSource>.getPos(name: String): BlockPos{
	return this.getArgument(name, LocationInput::class.java).getBlockPos(this.source)
}
