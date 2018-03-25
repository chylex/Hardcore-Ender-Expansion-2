package chylex.hee.game.commands.sub.client
import chylex.hee.HardcoreEnderExpansion
import chylex.hee.game.commands.sub.ISubCommand
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.TextFormatting.DARK_GREEN

internal object CommandClientVersion : ISubCommand{
	override val name = "version"
	override val usage = "commands.hee.version.usage"
	override val info = "commands.hee.version.info"
	
	override fun executeCommand(server: MinecraftServer?, sender: ICommandSender, args: Array<out String>){
		val emptyLine = TextComponentString("")
		
		sender.sendMessage(emptyLine)
		sender.sendMessage(TextComponentString("--- Hardcore Ender Expansion 2 ---").also {
			it.style.color = DARK_GREEN
		})
		sender.sendMessage(emptyLine)
		
		sender.sendMessage(TextComponentTranslation("commands.hee.version.data.version", HardcoreEnderExpansion.version))
		sender.sendMessage(emptyLine)
		// TODO update information
	}
}
