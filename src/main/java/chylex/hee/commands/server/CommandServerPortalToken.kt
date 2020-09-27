package chylex.hee.commands.server
import chylex.hee.commands.CommandExecutionFunctionCtx
import chylex.hee.commands.ICommand
import chylex.hee.commands.arguments.EnumArgument.Companion.enum
import chylex.hee.commands.executes
import chylex.hee.commands.getEnum
import chylex.hee.commands.message
import chylex.hee.commands.returning
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.init.ModItems
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands.argument
import net.minecraft.util.text.TranslationTextComponent

object CommandServerPortalToken : ICommand, CommandExecutionFunctionCtx<Boolean>{
	override val name = "token"
	
	override fun register(builder: ArgumentBuilder<CommandSource, *>){
		builder.then(
			argument("territory", enum<TerritoryType>()).executes(this, false).then(
				argument("type", enum<TokenType>()).executes(this, true)
			)
		)
	}
	
	override fun invoke(ctx: CommandContext<CommandSource>, hasType: Boolean) = returning(1){
		val territory = ctx.getEnum<TerritoryType>("territory")
		val type = if (hasType) ctx.getEnum("type") else TokenType.NORMAL
		
		with(ctx.source){
			asPlayer().addItemStackToInventory(ModItems.PORTAL_TOKEN.forTerritory(type, territory))
			sendFeedback(message("success", TranslationTextComponent(territory.translationKey)), true)
		}
	}
}
