package chylex.hee.game.command.server

import chylex.hee.game.command.ICommand
import chylex.hee.game.command.util.CommandExecutionFunctionCtx
import chylex.hee.game.command.util.executes
import chylex.hee.game.command.util.getInt
import chylex.hee.game.command.util.simpleCommand
import chylex.hee.init.ModCommands
import chylex.hee.util.math.ceilToInt
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandException
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands.argument
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.text.IFormattableTextComponent
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting.DARK_GREEN
import net.minecraft.util.text.TextFormatting.GRAY
import net.minecraft.util.text.TextFormatting.GREEN
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.util.text.event.ClickEvent
import net.minecraft.util.text.event.ClickEvent.Action.RUN_COMMAND
import net.minecraft.util.text.event.ClickEvent.Action.SUGGEST_COMMAND

object CommandServerHelp : ICommand, CommandExecutionFunctionCtx<Boolean> {
	private const val COMMANDS_PER_PAGE = 7
	
	override val name = "help"
	override val description = "shows command list"
	
	override val localization
		get() = mapOf(
			"failed" to "Page must be between 1 and %s",
			
			"header.client" to "§2--- Showing §aclient§2 commands, page %s§2 ---",
			"header.admin"  to "§2--- Showing §aadmin§2 commands, page %s §2of %s§2 ---",
			"header.debug"  to "§2--- Showing §adebug§2 commands, page %s §2of %s§2 ---",
			
			"footer.start"  to "§2--- Navigate to: §r",
			"footer.admin"  to "§a§nadmin commands",
			"footer.prev"   to "previous page",
			"footer.middle" to "§2 / §r",
			"footer.next"   to "next page",
			"footer.end"    to "§2 ---",
		)
	
	override fun register(builder: ArgumentBuilder<CommandSource, *>) {
		builder.executes(this, false)
		
		builder.then(
			argument("page", integer(1)).executes(this, true)
		)
	}
	
	override fun invoke(ctx: CommandContext<CommandSource>, hasDisplayPage: Boolean) = simpleCommand {
		val source = ctx.source
		val displayPage = if (hasDisplayPage) ctx.getInt("page") else 1
		
		var totalPages = (ModCommands.admin.size.toFloat() / COMMANDS_PER_PAGE).ceilToInt()
		var debugPage = -1
		
		if (ModCommands.debug.isNotEmpty()) {
			totalPages++
			debugPage = totalPages
		}
		
		val actualPage: Int
		
		if (source.entity is PlayerEntity) {
			actualPage = displayPage - 1
			totalPages++
		}
		else {
			actualPage = displayPage
		}
		
		if (displayPage < 1 || displayPage > totalPages) {
			throw CommandException(TranslationTextComponent("commands.hee.help.failed", totalPages))
		}
		
		val responseHeaderKey: String
		val responseCommands: Iterable<ICommand>
		
		if (actualPage == debugPage) {
			responseHeaderKey = "commands.hee.help.header.debug"
			responseCommands = ModCommands.debug.asIterable()
		}
		else {
			responseHeaderKey = "commands.hee.help.header.admin"
			responseCommands = ModCommands.admin.drop((actualPage - 1) * COMMANDS_PER_PAGE).take(COMMANDS_PER_PAGE)
		}
		
		val commandNames = responseCommands.map { it.name }
		val commandUsages = source.server.commandManager.dispatcher.let { it.getSmartUsage(it.root.getChild(ModCommands.ROOT), source) }.mapKeys { it.value }
		
		sendCommandListPage(source, commandNames, commandUsages, responseHeaderKey, displayPage, totalPages)
	}
	
	fun sendCommandListPage(source: CommandSource, commandNames: Iterable<String>, commandUsages: Map<String, String>, headerKey: String, currentPage: Int, totalPages: Int?) {
		val emptyLine = StringTextComponent("")
		
		send(source, emptyLine)
		send(source, TranslationTextComponent(headerKey, currentPage, totalPages).also {
			it.mergeStyle(GREEN) // required to set a custom color on tokens
		})
		send(source, emptyLine)
		
		for (name in commandNames) {
			val entry = commandUsages.entries.find { it.key == name }
			val usage = entry?.value?.replaceFirst("[$name]", name) ?: name
			
			send(source, StringTextComponent(usage).also {
				it.style = it.style.setClickEvent(ClickEvent(SUGGEST_COMMAND, "/hee $name"))
			})
			
			send(source, chainTextComponents(
				StringTextComponent("  "),
				TranslationTextComponent("commands.hee.${name}.info").also {
					it.mergeStyle(GRAY)
				}
			))
		}
		
		if (source.entity is PlayerEntity) {
			send(source, emptyLine)
			sendInteractiveNavigation(source, currentPage, totalPages)
			send(source, emptyLine)
		}
	}
	
	private fun sendInteractiveNavigation(source: CommandSource, currentPage: Int, totalPages: Int?) {
		val components = mutableListOf<IFormattableTextComponent>()
		
		if (totalPages == null) {
			components.add(TranslationTextComponent("commands.hee.help.footer.admin").also {
				it.style = it.style.setClickEvent(ClickEvent(RUN_COMMAND, "/hee help ${currentPage + 1}"))
			})
		}
		else {
			val showPrev = currentPage > 1
			val showNext = currentPage < totalPages
			
			components.add(TranslationTextComponent("commands.hee.help.footer.prev").also {
				getNavigationStyle(it, if (showPrev) currentPage - 1 else null)
			})
			
			components.add(TranslationTextComponent("commands.hee.help.footer.middle"))
			
			components.add(TranslationTextComponent("commands.hee.help.footer.next").also {
				getNavigationStyle(it, if (showNext) currentPage + 1 else null)
			})
		}
		
		send(source, chainTextComponents(
			TranslationTextComponent("commands.hee.help.footer.start"),
			*components.toTypedArray(),
			TranslationTextComponent("commands.hee.help.footer.end")
		))
	}
	
	private fun send(source: CommandSource, text: ITextComponent) {
		source.sendFeedback(text, false)
	}
	
	private fun getNavigationStyle(text: IFormattableTextComponent, page: Int?) {
		if (page == null) {
			text.mergeStyle(DARK_GREEN)
		}
		else {
			text.style = text.style
				.setClickEvent(ClickEvent(RUN_COMMAND, "/${ModCommands.ROOT} help $page"))
				.setFormatting(GREEN)
				.setUnderlined(true)
		}
	}
	
	private fun chainTextComponents(vararg components: IFormattableTextComponent): IFormattableTextComponent {
		return components.reduce(IFormattableTextComponent::appendSibling)
	}
}
