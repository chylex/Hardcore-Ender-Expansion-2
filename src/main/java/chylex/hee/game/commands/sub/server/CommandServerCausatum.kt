package chylex.hee.game.commands.sub.server

/*
object CommandServerCausatum : ISubCommand{
	override val name = "causatum"
	override val usage = "commands.hee.causatum.usage"
	override val info = "commands.hee.causatum.info"
	
	override fun executeCommand(server: MinecraftServer?, sender: ICommandSender, args: Array<out String>){
		if (sender is EntityPlayer){
			if (args.isEmpty()){
				sender.sendMessage(TextComponentString("Current: " + EnderCausatum.getStage(sender).key))
			}
			else{
				val stage = CausatumStage.fromKey(args[0])
				
				if (stage != null){
					sender.sendMessage(TextComponentString("Success: " + EnderCausatum.triggerStage(sender, stage, args.size == 2 && args[1] == "force")))
				}
			}
		}
		
		// UPDATE
	}
}*/
