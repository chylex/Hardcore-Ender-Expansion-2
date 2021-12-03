package chylex.hee.game.command.server

import chylex.hee.game.command.ICommand
import chylex.hee.game.command.argument.EnumArgument.Companion.enum
import chylex.hee.game.command.message
import chylex.hee.game.command.util.CommandExecutionFunctionCtx
import chylex.hee.game.command.util.executes
import chylex.hee.game.command.util.getEnum
import chylex.hee.game.command.util.simpleCommand
import chylex.hee.game.item.ItemPortalToken
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.territory.TerritoryType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands.argument
import net.minecraft.util.text.TranslationTextComponent

object CommandServerPortalToken : ICommand, CommandExecutionFunctionCtx<Boolean> {
	override val name = "token"
	override val description = "generates a Portal Token item"
	
	override val localization
		get() = mapOf(
			"success" to "Created Portal Token leading to %s",
		)
	
	override fun register(builder: ArgumentBuilder<CommandSource, *>) {
		builder.then(
			argument("territory", enum<TerritoryType>()).executes(this, false).then(
				argument("type", enum<TokenType>()).executes(this, true)
			)
		)
	}
	
	override fun invoke(ctx: CommandContext<CommandSource>, hasType: Boolean) = simpleCommand {
		val territory = ctx.getEnum<TerritoryType>("territory")
		val type = if (hasType) ctx.getEnum("type") else TokenType.NORMAL
		
		with(ctx.source) {
			asPlayer().addItemStackToInventory(ItemPortalToken.forTerritory(type, territory))
			sendFeedback(message("success", TranslationTextComponent(territory.translationKey)), true)
		}
	}
}
