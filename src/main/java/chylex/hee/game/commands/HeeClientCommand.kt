package chylex.hee.game.commands
import chylex.hee.HEE
import chylex.hee.game.commands.sub.ISubCommand.Companion.subCommandMapOf
import chylex.hee.game.commands.sub.client.CommandClientHelp
import chylex.hee.game.commands.sub.client.CommandClientScaffolding
import chylex.hee.game.commands.sub.client.CommandClientVersion
import chylex.hee.game.commands.sub.client.CommandDebugToggles
import chylex.hee.system.Debug
import net.minecraft.command.ICommandSender
import net.minecraftforge.client.IClientCommand
import net.minecraftforge.event.CommandEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
@EventBusSubscriber(Side.CLIENT, modid = HEE.ID)
internal object HeeClientCommand : HeeBaseCommand(), IClientCommand{
	public override val allSubCommands = subCommandMapOf(*
		arrayOf(
			CommandClientHelp,
			CommandClientVersion,
			CommandClientScaffolding
		)
		+ if (Debug.enabled) arrayOf(
			CommandDebugToggles
		) else emptyArray()
	)
	
	override val defaultSubCommand = CommandClientHelp
	
	override fun getRequiredPermissionLevel() = 0
	
	override fun allowUsageWithoutPrefix(sender: ICommandSender, message: String) = false
	
	@JvmStatic
	@SubscribeEvent
	fun onClientCommand(e: CommandEvent){
		if (e.command === HeeClientCommand){
			val args = e.parameters
			
			if (args.isNotEmpty() && !allSubCommands.containsKey(args[0])){
				e.isCanceled = true // send the command to server
			}
		}
	}
}
