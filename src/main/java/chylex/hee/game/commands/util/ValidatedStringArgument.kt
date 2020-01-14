package chylex.hee.game.commands.util
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.ISuggestionProvider
import java.util.concurrent.CompletableFuture

class ValidatedStringArgument(private val strings: Set<String>) : ArgumentType<String>{
	companion object{
		 fun validatedString(strings: Collection<String>): ValidatedStringArgument{
			return ValidatedStringArgument(strings.toSet())
		}
	}
	
	init{
		if (strings.any { it.contains(' ') }){
			throw IllegalArgumentException("[StringFromSetArgument] strings must not contain any spaces")
		}
	}
	
	override fun parse(reader: StringReader): String{
		return reader.readUnquotedString().takeIf(strings::contains) ?: throw IllegalArgumentException()
	}
	
	override fun <S : Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions>{
		return ISuggestionProvider.suggest(strings, builder)
	}
	
	override fun getExamples(): MutableCollection<String>{
		return strings.sorted().take(3).toMutableList()
	}
}
