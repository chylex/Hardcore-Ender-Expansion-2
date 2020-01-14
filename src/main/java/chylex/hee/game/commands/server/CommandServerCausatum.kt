package chylex.hee.game.commands.server
import chylex.hee.game.commands.ICommand
import chylex.hee.game.commands.util.EnumArgument.Companion.enum
import chylex.hee.game.commands.util.exception
import chylex.hee.game.commands.util.executes
import chylex.hee.game.commands.util.getEnum
import chylex.hee.game.commands.util.message
import chylex.hee.game.commands.util.returning
import chylex.hee.game.mechanics.causatum.CausatumStage
import chylex.hee.game.mechanics.causatum.EnderCausatum
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands.argument
import net.minecraft.command.Commands.literal

object CommandServerCausatum : ICommand{
	override val name = "causatum"
	
	override fun register(builder: ArgumentBuilder<CommandSource, *>){
		builder.executes(this::executeCheck) // TODO add a way to check someone else's stage
		
		builder.then(
			argument("new_stage", enum<CausatumStage>()).executes(this::executeTrigger, false).then(
				literal("force").executes(this::executeTrigger, true)
			)
		)
	}
	
	private val TRIGGER_FAIL = exception("trigger_fail")
	
	private fun executeCheck(ctx: CommandContext<CommandSource>): Int{
		with(ctx.source){
			val stage = EnderCausatum.getStage(asPlayer())
			
			sendFeedback(message("check", stage.key), false)
			return stage.ordinal
		}
	}
	
	private fun executeTrigger(ctx: CommandContext<CommandSource>, force: Boolean) = returning(1){
		val newStage = ctx.getEnum<CausatumStage>("new_stage")
		
		with(ctx.source){
			if (!EnderCausatum.triggerStage(asPlayer(), newStage, force)){
				throw TRIGGER_FAIL.create()
			}
			
			sendFeedback(message("trigger_success"), true)
		}
	}
}
