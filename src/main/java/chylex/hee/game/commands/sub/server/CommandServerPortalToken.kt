package chylex.hee.game.commands.sub.server

/*
object CommandServerPortalToken : ISubCommand{
	override val name = "token"
	override val usage = "commands.hee.token.usage"
	override val info = "commands.hee.token.info"
	
	override fun executeCommand(server: MinecraftServer?, sender: ICommandSender, args: Array<out String>){
		if (sender is EntityPlayer){
			val type = args.getOrElse(1){ TokenType.NORMAL.name }.toUpperCase(Locale.ENGLISH).let(TokenType::valueOf)
			val territory = args.getOrNull(0)?.let(TerritoryType.Companion::fromTitle) ?: return
			
			sender.addItemStackToInventory(ModItems.PORTAL_TOKEN.forTerritory(type, territory))
		}
		
		// UPDATE
	}
}*/
