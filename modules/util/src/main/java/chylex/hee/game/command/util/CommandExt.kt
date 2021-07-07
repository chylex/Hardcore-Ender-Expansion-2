package chylex.hee.game.command.util

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.arguments.LocationInput
import net.minecraft.util.math.BlockPos

typealias CommandExecutionFunction = Command<CommandSource>
typealias CommandExecutionFunctionCtx<C> = (CommandContext<CommandSource>, C) -> Int

fun <C, T : ArgumentBuilder<CommandSource, T>> ArgumentBuilder<CommandSource, T>.executes(function: (CommandContext<CommandSource>, C) -> Int, extra: C): T {
	return this.executes { function(it, extra) }
}

inline fun simpleCommand(code: () -> Unit): Int {
	code();
	return 1;
}

inline fun <reified T : Enum<T>> CommandContext<CommandSource>.getEnum(name: String): T {
	return this.getArgument(name, T::class.java)
}

fun CommandContext<CommandSource>.getString(name: String): String {
	return this.getArgument(name, String::class.java)
}

fun CommandContext<CommandSource>.getInt(name: String): Int {
	return this.getArgument(name, Int::class.java)
}

fun CommandContext<CommandSource>.getLong(name: String): Long {
	return this.getArgument(name, Long::class.java)
}

fun CommandContext<CommandSource>.getPos(name: String): BlockPos {
	return this.getArgument(name, LocationInput::class.java).getBlockPos(this.source)
}
