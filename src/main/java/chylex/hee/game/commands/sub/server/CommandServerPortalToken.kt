package chylex.hee.game.commands.sub.server
import chylex.hee.game.commands.sub.ISubCommand
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.init.ModItems
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import java.util.Locale

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
}
