package chylex.hee.game.commands
import chylex.hee.HardcoreEnderExpansion
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.client.IClientCommand
import net.minecraftforge.event.CommandEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
@EventBusSubscriber(Side.CLIENT, modid = HardcoreEnderExpansion.ID)
object HeeClientCommand : CommandBase(), IClientCommand{
	private val subCommands = setOf("1", "version")
	
	override fun getName(): String = HeeServerCommand.name
	
	override fun getUsage(sender: ICommandSender): String = HeeServerCommand.getUsage(sender)
	
	override fun getRequiredPermissionLevel(): Int = 0
	
	override fun allowUsageWithoutPrefix(sender: ICommandSender, message: String): Boolean = false
	
	override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>){
		if (args.isEmpty() || args[0] == "1"){
			sender.sendMessage(TextComponentString("client help"))
			// TODO show client-only commands, and a message to use /hee [page=1] for more pages
		}
		else{
			sender.sendMessage(TextComponentString("client command"))
			// TODO
		}
	}
	
	@JvmStatic
	@SubscribeEvent
	fun onClientCommand(e: CommandEvent){
		if (e.command === HeeClientCommand){
			val args = e.parameters
			
			if (!args.isEmpty() && !subCommands.contains(args[0])){
				e.isCanceled = true // send the command to server
			}
		}
	}
}
