package chylex.hee.game.commands.util
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.ISuggestionProvider
import java.util.Locale
import java.util.concurrent.CompletableFuture

class EnumArgument<T : Enum<T>>(private val enumClass: Class<T>) : ArgumentType<T>{
	companion object{
		inline fun <reified T : Enum<T>> enum(): EnumArgument<T>{
			return EnumArgument(T::class.java)
		}
	}
	
	private fun name(element: T): String{
		return element.name.toLowerCase(Locale.ENGLISH)
	}
	
	override fun parse(reader: StringReader): T{
		val query = reader.readUnquotedString()
		return enumClass.enumConstants.first { it.name.equals(query, ignoreCase = true) }
	}
	
	override fun <S : Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions>{
		return ISuggestionProvider.suggest(enumClass.enumConstants.map(::name), builder)
	}
	
	override fun getExamples(): MutableCollection<String>{
		return enumClass.enumConstants.take(3).map(::name).toMutableList()
	}
}
