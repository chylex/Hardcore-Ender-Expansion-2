package chylex.hee.game.commands.sub.server
import chylex.hee.game.commands.sub.ISubCommand
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.system.migration.Hand.MAIN_HAND
import chylex.hee.system.util.isNotEmpty
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer

object CommandServerInfusions : ISubCommand{
	override val name = "infusions"
	override val usage = "commands.hee.infusions.usage"
	override val info = "commands.hee.infusions.info"
	
	override fun executeCommand(server: MinecraftServer?, sender: ICommandSender, args: Array<out String>){
		if (sender is EntityPlayer){
			val heldItem = sender.getHeldItem(MAIN_HAND)
			
			if (heldItem.isNotEmpty){
				if (args.size == 1 && args[0] == "reset"){
					InfusionTag.setList(heldItem, InfusionList.EMPTY)
				}
				else if (args.size == 2 && args[0] == "add"){
					Infusion.values().firstOrNull {
						it.name.equals(args[1], true)
					}?.let {
						it.tryInfuse(heldItem)
					}?.let {
						sender.setHeldItem(MAIN_HAND, it)
					}
				}
			}
		}
		
		// UPDATE
	}
}
