package chylex.hee.game.commands.server
import chylex.hee.game.commands.ICommand
import chylex.hee.game.commands.util.EnumArgument.Companion.enum
import chylex.hee.game.commands.util.exception
import chylex.hee.game.commands.util.getEnum
import chylex.hee.game.commands.util.message
import chylex.hee.game.commands.util.returning
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.system.migration.Hand.MAIN_HAND
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands.argument
import net.minecraft.command.Commands.literal
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TranslationTextComponent

object CommandServerInfusions : ICommand{
	override val name = "infusions"
	
	override fun register(builder: ArgumentBuilder<CommandSource, *>){
		builder.then(
			literal("reset").executes(this::executeReset)
		)
		
		builder.then(
			literal("add").then(
				argument("infusion", enum<Infusion>()).executes(this::executeAdd)
			)
		)
		
		builder.then(
			literal("remove").then(
				argument("infusion", enum<Infusion>()).executes(this::executeRemove)
			)
		)
	}
	
	private val NO_HELD_ITEM = exception("no_held_item")
	private val NOTHING_TO_REMOVE = exception("nothing_to_remove")
	private val NOT_APPLICABLE = exception("not_applicable")
	private val ALREADY_PRESENT = exception("already_present")
	private val NOT_PRESENT = exception("not_present")
	
	private inline fun updateHeldItem(ctx: CommandContext<CommandSource>, modify: (ItemStack, InfusionList) -> ItemStack){
		val player = ctx.source.asPlayer()
		val heldItem = player.getHeldItem(MAIN_HAND)
		
		if (heldItem.isEmpty){
			throw NO_HELD_ITEM.create()
		}
		
		player.setHeldItem(MAIN_HAND, modify(heldItem, InfusionTag.getList(heldItem)))
	}
	
	private fun executeReset(ctx: CommandContext<CommandSource>): Int{
		var removedInfusions = 0
		
		updateHeldItem(ctx) { stack, list ->
			removedInfusions = list.size
			
			if (removedInfusions == 0) {
				throw NOTHING_TO_REMOVE.create()
			}
			
			stack.also { InfusionTag.setList(it, InfusionList.EMPTY) }
		}
		
		ctx.source.sendFeedback(message("reset_success", removedInfusions), true)
		return removedInfusions
	}
	
	private fun executeAdd(ctx: CommandContext<CommandSource>) = returning(1){
		val infusion = ctx.getEnum<Infusion>("infusion")
		
		updateHeldItem(ctx) { stack, list ->
			if (list.has(infusion)) {
				throw ALREADY_PRESENT.create()
			}
			
			infusion.tryInfuse(stack) ?: throw NOT_APPLICABLE.create()
		}
		
		ctx.source.sendFeedback(message("add_success", TranslationTextComponent(infusion.translationKey)), true)
	}
	
	private fun executeRemove(ctx: CommandContext<CommandSource>) = returning(1){
		val infusion = ctx.getEnum<Infusion>("infusion")
		
		updateHeldItem(ctx) { stack, list ->
			if (!list.has(infusion)) {
				throw NOT_PRESENT.create()
			}
			
			stack.also { InfusionTag.setList(it, list.except(infusion)) }
		}
		
		ctx.source.sendFeedback(message("remove_success", TranslationTextComponent(infusion.translationKey)), true)
	}
}
