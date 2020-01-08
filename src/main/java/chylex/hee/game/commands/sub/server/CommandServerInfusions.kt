package chylex.hee.game.commands.sub.server

/*
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
}*/
