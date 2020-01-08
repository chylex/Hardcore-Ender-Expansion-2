package chylex.hee.game.commands

/*
@Sided(Side.CLIENT)
@SubscribeAllEvents(Side.CLIENT, modid = HEE.ID)
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
	
	@SubscribeEvent
	fun onClientCommand(e: CommandEvent){
		if (e.command === HeeClientCommand){
			val args = e.parameters
			
			if (args.isNotEmpty() && !allSubCommands.containsKey(args[0])){
				e.isCanceled = true // send the command to server
			}
		}
	}
}*/
