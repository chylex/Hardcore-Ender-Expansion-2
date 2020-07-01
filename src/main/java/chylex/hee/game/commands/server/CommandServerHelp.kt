package chylex.hee.game.commands.server
import chylex.hee.game.commands.ICommand
import chylex.hee.game.commands.util.CommandExecutionFunctionCtx
import chylex.hee.game.commands.util.executes
import chylex.hee.game.commands.util.getInt
import chylex.hee.game.commands.util.returning
import chylex.hee.init.ModCommands
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.TextComponentString
import chylex.hee.system.migration.vanilla.TextComponentTranslation
import chylex.hee.system.util.ceilToInt
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandException
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands.argument
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextFormatting.DARK_GREEN
import net.minecraft.util.text.TextFormatting.GRAY
import net.minecraft.util.text.TextFormatting.GREEN
import net.minecraft.util.text.event.ClickEvent
import net.minecraft.util.text.event.ClickEvent.Action.RUN_COMMAND
import net.minecraft.util.text.event.ClickEvent.Action.SUGGEST_COMMAND

object CommandServerHelp : ICommand, CommandExecutionFunctionCtx<Boolean>{
	private const val COMMANDS_PER_PAGE = 7
	
	override val name = "help"
	
	override fun register(builder: ArgumentBuilder<CommandSource, *>){
		builder.executes(this, false)
		
		builder.then(
			argument("page", integer(1)).executes(this, true)
		)
	}
	
	override fun invoke(ctx: CommandContext<CommandSource>, hasDisplayPage: Boolean) = returning(1){
		val source = ctx.source
		val displayPage = if (hasDisplayPage) ctx.getInt("page") else 1
		
		var totalPages = (ModCommands.admin.size.toFloat() / COMMANDS_PER_PAGE).ceilToInt()
		var debugPage = -1
		
		if (ModCommands.debug.isNotEmpty()){
			totalPages++
			debugPage = totalPages
		}
		
		val actualPage: Int
		
		if (source.entity is EntityPlayer){
			actualPage = displayPage - 1
			totalPages++
		}
		else{
			actualPage = displayPage
		}
		
		if (displayPage < 1 || displayPage > totalPages){
			throw CommandException(TextComponentTranslation("commands.hee.help.failed", totalPages))
		}
		
		val responseHeaderKey: String
		val responseCommands: Iterable<ICommand>
		
		if (actualPage == debugPage){
			responseHeaderKey = "commands.hee.help.header.debug"
			responseCommands = ModCommands.debug.asIterable()
		}
		else{
			responseHeaderKey = "commands.hee.help.header.admin"
			responseCommands = ModCommands.admin.drop((actualPage - 1) * COMMANDS_PER_PAGE).take(COMMANDS_PER_PAGE)
		}
		
		val commandNames = responseCommands.map { it.name }
		val commandUsages = source.server.commandManager.dispatcher.let { it.getSmartUsage(it.root.getChild(ModCommands.ROOT), source) }.mapKeys { it.value }
		
		sendCommandListPage(source, commandNames, commandUsages, responseHeaderKey, displayPage, totalPages)
	}
	
	fun sendCommandListPage(source: CommandSource, commandNames: Iterable<String>, commandUsages: Map<String, String>, headerKey: String, currentPage: Int, totalPages: Int?){
		val emptyLine = TextComponentString("")
		
		send(source, emptyLine)
		send(source, TextComponentTranslation(headerKey, currentPage, totalPages).also {
			it.style.color = GREEN // required to set a custom color on tokens
		})
		send(source, emptyLine)
		
		for(name in commandNames){
			val entry = commandUsages.entries.find { it.key == name }
			val usage = entry?.value?.replaceFirst("[$name]", name) ?: name
			
			send(source, TextComponentString(usage).also {
				it.style.clickEvent = ClickEvent(SUGGEST_COMMAND, "/hee $name")
			})
			
			send(source, chainTextComponents(
				TextComponentString("  "),
				TextComponentTranslation("commands.hee.${name}.info").also {
					it.style.color = GRAY
				}
			))
		}
		
		if (source.entity is EntityPlayer){
			send(source, emptyLine)
			sendInteractiveNavigation(source, currentPage, totalPages)
			send(source, emptyLine)
		}
	}
	
	private fun sendInteractiveNavigation(source: CommandSource, currentPage: Int, totalPages: Int?){
		val components = mutableListOf<ITextComponent>()
		
		if (totalPages == null){
			components.add(TextComponentTranslation("commands.hee.help.footer.admin").also {
				it.style.clickEvent = ClickEvent(RUN_COMMAND, "/hee help ${currentPage + 1}")
			})
		}
		else{
			val showPrev = currentPage > 1
			val showNext = currentPage < totalPages
			
			components.add(TextComponentTranslation("commands.hee.help.footer.prev").also {
				setupNavigation(it, if (showPrev) currentPage - 1 else null)
			})
			
			components.add(TextComponentTranslation("commands.hee.help.footer.middle"))
			
			components.add(TextComponentTranslation("commands.hee.help.footer.next").also {
				setupNavigation(it, if (showNext) currentPage + 1 else null)
			})
		}
		
		send(source, chainTextComponents(
			TextComponentTranslation("commands.hee.help.footer.start"),
			*components.toTypedArray(),
			TextComponentTranslation("commands.hee.help.footer.end")
		))
	}
	
	private fun send(source: CommandSource, text: ITextComponent){
		source.sendFeedback(text, false)
	}
	
	private fun setupNavigation(text: ITextComponent, page: Int?){
		val style = text.style
		
		if (page != null){
			style.clickEvent = ClickEvent(RUN_COMMAND, "/${ModCommands.ROOT} help $page")
			style.color = GREEN
			style.underlined = true
		}
		else{
			style.color = DARK_GREEN
		}
	}
	
	private fun chainTextComponents(vararg components: ITextComponent): ITextComponent{
		return components.reduce { component, next -> component.appendSibling(next) }
	}
}
