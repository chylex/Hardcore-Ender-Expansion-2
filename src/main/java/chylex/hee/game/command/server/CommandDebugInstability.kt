package chylex.hee.game.command.server

import chylex.hee.game.command.ICommand
import chylex.hee.game.command.util.executes
import chylex.hee.game.command.util.getInt
import chylex.hee.game.mechanics.instability.Instability
import chylex.hee.game.mechanics.instability.dimension.DimensionInstabilityNull
import chylex.hee.game.mechanics.instability.dimension.IDimensionInstability
import chylex.hee.util.math.Pos
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands.argument
import net.minecraft.command.Commands.literal
import net.minecraft.util.text.StringTextComponent

object CommandDebugInstability : ICommand {
	override val name = "instability"
	
	override fun register(builder: ArgumentBuilder<CommandSource, *>) {
		val execModify = this::executeModify
		val instabilityAmountArg = integer(UShort.MIN_VALUE.toInt(), UShort.MAX_VALUE.toInt())
		
		builder.then(
			literal("check").executes(this::executeCheck)
		)
		
		builder.then(
			literal("add").then(argument("amount", instabilityAmountArg).executes(execModify, true))
		)
		
		builder.then(
			literal("subtract").then(argument("amount", instabilityAmountArg).executes(execModify, true))
		)
	}
	
	private fun executeCheck(ctx: CommandContext<CommandSource>): Int {
		val instability = getInstability(ctx) ?: return 0
		
		with(ctx.source) {
			sendFeedback(StringTextComponent("Instability level: " + instability.getLevel(Pos(pos))), false)
			return 1
		}
	}
	
	private fun executeModify(ctx: CommandContext<CommandSource>, add: Boolean): Int {
		val instability = getInstability(ctx) ?: return 0
		val amount = ctx.getInt("amount")
		
		with(ctx.source) {
			val pos = Pos(pos)
			
			if (add) {
				instability.resetActionMultiplier(pos)
				instability.triggerAction(amount.toUShort(), pos)
			}
			else {
				instability.triggerRelief(amount.toUShort(), pos)
			}
			
			sendFeedback(StringTextComponent("Instability level updated."), false)
			return 1
		}
	}
	
	private fun getInstability(ctx: CommandContext<CommandSource>): IDimensionInstability? {
		with(ctx.source) {
			val instability = Instability.get(world)
			
			if (instability === DimensionInstabilityNull) {
				sendFeedback(StringTextComponent("Invalid dimension."), false)
				return null
			}
			
			return instability
		}
	}
}
