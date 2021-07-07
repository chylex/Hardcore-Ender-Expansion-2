package chylex.hee.game.command.argument

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException.BUILT_IN_EXCEPTIONS
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.ISuggestionProvider
import net.minecraft.command.arguments.IArgumentSerializer
import net.minecraft.network.PacketBuffer
import java.util.concurrent.CompletableFuture

class ValidatedStringArgument(private val strings: Set<String>) : ArgumentType<String> {
	companion object {
		private const val MAX_LENGTH = 64
		
		fun validatedString(strings: Collection<String>): ValidatedStringArgument {
			return ValidatedStringArgument(strings.toSet())
		}
	}
	
	object Serializer : IArgumentSerializer<ValidatedStringArgument> {
		override fun write(argument: ValidatedStringArgument, buffer: PacketBuffer) {
			buffer.writeVarInt(argument.strings.size)
			argument.strings.forEach { buffer.writeString(it, MAX_LENGTH) }
		}
		
		override fun write(argument: ValidatedStringArgument, json: JsonObject) {
			json.add("strings", JsonArray().apply {
				argument.strings.forEach(this::add)
			})
		}
		
		override fun read(buffer: PacketBuffer): ValidatedStringArgument {
			return ValidatedStringArgument(Array(buffer.readVarInt()) { buffer.readString(MAX_LENGTH) }.toSet())
		}
	}
	
	init {
		require(strings.none { it.contains(' ') }) { "[ValidatedStringArgument] strings must not contain any spaces" }
		require(strings.none { it.length > MAX_LENGTH }) { "[ValidatedStringArgument] strings must be at most $MAX_LENGTH characters" }
	}
	
	override fun parse(reader: StringReader): String {
		return reader.readUnquotedString().takeIf(strings::contains) ?: throw BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create()
	}
	
	override fun <S : Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
		return ISuggestionProvider.suggest(strings, builder)
	}
	
	override fun getExamples(): MutableCollection<String> {
		return strings.sorted().take(3).toMutableList()
	}
}
