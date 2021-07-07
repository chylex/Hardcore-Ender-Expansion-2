package chylex.hee.game.command.server

import chylex.hee.game.command.ICommand
import chylex.hee.game.command.argument.EnumArgument.Companion.enum
import chylex.hee.game.command.message
import chylex.hee.game.command.util.executes
import chylex.hee.game.command.util.getEnum
import chylex.hee.game.command.util.simpleCommand
import chylex.hee.game.mechanics.causatum.CausatumStage
import chylex.hee.game.mechanics.causatum.EnderCausatum
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands.argument
import net.minecraft.command.Commands.literal
import net.minecraft.command.arguments.EntityArgument
import net.minecraft.command.arguments.EntityArgument.player
import net.minecraft.command.arguments.EntityArgument.players
import net.minecraft.util.text.StringTextComponent
import java.util.Locale

object CommandServerCausatum : ICommand {
	override val name = "causatum"
	
	override fun register(builder: ArgumentBuilder<CommandSource, *>) {
		val execCheck = this::executeCheck
		val execSet = this::executeSet
		
		builder.then(
			literal("list").executes(this::executeList)
		)
		
		builder.then(
			literal("check").executes(execCheck, false).then(
				argument("player", player()).executes(execCheck, true)
			)
		)
		
		builder.then(
			literal("set").then(
				argument("stage", enum<CausatumStage>()).executes(execSet, false).then(
					argument("players", players()).executes(execSet, true)
				)
			)
		)
	}
	
	private fun executeList(ctx: CommandContext<CommandSource>) = simpleCommand {
		with(ctx.source) {
			sendFeedback(message("list"), false)
			
			for (stage in CausatumStage.values()) {
				sendFeedback(StringTextComponent(stage.name.lowercase(Locale.ENGLISH)), false)
			}
		}
	}
	
	private fun executeCheck(ctx: CommandContext<CommandSource>, hasPlayerParameter: Boolean): Int {
		with(ctx.source) {
			val player = if (hasPlayerParameter) EntityArgument.getPlayer(ctx, "player") else asPlayer()
			val stage = EnderCausatum.getStage(player)
			
			sendFeedback(message("check", stage.name.lowercase(Locale.ENGLISH)), false)
			return stage.ordinal
		}
	}
	
	private fun executeSet(ctx: CommandContext<CommandSource>, hasPlayerParameter: Boolean): Int {
		val newStage = ctx.getEnum<CausatumStage>("stage")
		
		with(ctx.source) {
			val players = if (hasPlayerParameter) EntityArgument.getPlayers(ctx, "players") else listOf(asPlayer())
			
			for (player in players) {
				EnderCausatum.triggerStage(player, newStage, force = true)
			}
			
			sendFeedback(message("set", players.size), true)
			return players.size
		}
	}
}
