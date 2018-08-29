package chylex.hee.game.commands.sub.server
import chylex.hee.game.commands.HeeServerCommand.availableAdminCommands
import chylex.hee.game.commands.HeeServerCommand.availableDebugCommands
import chylex.hee.game.commands.sub.ISubCommand
import chylex.hee.system.util.ceilToInt
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.TextFormatting.GRAY
import net.minecraft.util.text.TextFormatting.GREEN
import net.minecraft.util.text.event.ClickEvent
import net.minecraft.util.text.event.ClickEvent.Action.RUN_COMMAND
import net.minecraft.util.text.event.ClickEvent.Action.SUGGEST_COMMAND

internal object CommandServerHelp : ISubCommand{
	override val name = "1"
	override val usage = "commands.hee.usage"
	override val info = "commands.hee.info"
	
	private const val COMMANDS_PER_PAGE = 7
	
	override fun executeCommand(server: MinecraftServer?, sender: ICommandSender, args: Array<out String>){
		val displayPage = if (args.isEmpty()) 1 else args[0].toIntOrNull() ?: throw WrongUsageException(usage)
		
		var totalPages = (availableAdminCommands.size.toFloat() / COMMANDS_PER_PAGE).ceilToInt()
		var debugPage = -1
		
		if (availableDebugCommands.isNotEmpty()){
			totalPages++
			debugPage = totalPages
		}
		
		val actualPage: Int
		
		if (sender is EntityPlayer){
			actualPage = displayPage - 1
			totalPages++
		}
		else{
			actualPage = displayPage
		}
		
		if (displayPage < 1 || displayPage > totalPages){
			throw CommandException("commands.hee.help.failed", totalPages)
		}
		
		val responseHeaderKey: String
		val responseCommands: Iterable<ISubCommand>
		
		if (actualPage == debugPage){
			responseHeaderKey = "commands.hee.help.header.debug"
			responseCommands = availableDebugCommands.asIterable()
		}
		else{
			responseHeaderKey = "commands.hee.help.header.admin"
			responseCommands = availableAdminCommands.drop((actualPage - 1) * COMMANDS_PER_PAGE).take(COMMANDS_PER_PAGE)
		}
		
		sendCommandListPage(sender, responseCommands, responseHeaderKey, displayPage, totalPages)
	}
	
	fun sendCommandListPage(sender: ICommandSender, commands: Iterable<ISubCommand>, headerKey: String, currentPage: Int, totalPages: Int?){
		val emptyLine = TextComponentString("")
		
		sender.sendMessage(emptyLine)
		sender.sendMessage(TextComponentTranslation(headerKey, currentPage, totalPages).also {
			it.style.color = GREEN // required to set a custom color on tokens
		})
		sender.sendMessage(emptyLine)
		
		for(command in commands){
			with(command){
				sender.sendMessage(TextComponentTranslation(usage).also {
					it.style.clickEvent = ClickEvent(SUGGEST_COMMAND, "/hee $name")
				})
				
				sender.sendMessage(chainTextComponents(
					TextComponentString("  "),
					TextComponentTranslation(info).also {
						it.style.color = GRAY
					}
				))
			}
		}
		
		if (sender is EntityPlayer){
			sender.sendMessage(emptyLine)
			sendInteractiveNavigation(sender, currentPage, totalPages)
			sender.sendMessage(emptyLine)
		}
	}
	
	private fun sendInteractiveNavigation(sender: ICommandSender, currentPage: Int, totalPages: Int?){
		val components = mutableListOf<ITextComponent>()
		
		if (totalPages == null){
			components.add(TextComponentTranslation("commands.hee.help.footer.admin").also {
				it.style.clickEvent = ClickEvent(RUN_COMMAND, "/hee ${currentPage + 1}")
			})
		}
		else{
			val showPrev = currentPage > 1
			val showNext = currentPage < totalPages
			
			if (showPrev){
				components.add(TextComponentTranslation("commands.hee.help.footer.prev").also {
					it.style.clickEvent = ClickEvent(RUN_COMMAND, "/hee ${currentPage - 1}")
				})
			}
			
			if (showPrev && showNext){
				components.add(TextComponentTranslation("commands.hee.help.footer.middle"))
			}
			
			if (showNext){
				components.add(TextComponentTranslation("commands.hee.help.footer.next").also {
					it.style.clickEvent = ClickEvent(RUN_COMMAND, "/hee ${currentPage + 1}")
				})
			}
		}
		
		sender.sendMessage(chainTextComponents(
			TextComponentTranslation("commands.hee.help.footer.start"),
			*components.toTypedArray(),
			TextComponentTranslation("commands.hee.help.footer.end")
		))
	}
	
	private fun chainTextComponents(vararg components: ITextComponent): ITextComponent{
		return components.reduce { component, next -> component.appendSibling(next) }
	}
}
