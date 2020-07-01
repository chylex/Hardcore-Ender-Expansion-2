package chylex.hee.game.commands.server
import chylex.hee.game.commands.ICommand
import chylex.hee.game.commands.util.CommandExecutionFunctionCtx
import chylex.hee.game.commands.util.EnumArgument.Companion.enum
import chylex.hee.game.commands.util.executes
import chylex.hee.game.commands.util.getEnum
import chylex.hee.game.commands.util.message
import chylex.hee.game.commands.util.returning
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.init.ModItems
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands.argument

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
			sendFeedback(message("success", TextComponentTranslation(territory.translationKey)), true)
		}
	}
}
