package chylex.hee.game.block.properties

import chylex.hee.init.ModItems
import net.minecraft.block.SkullBlock.ISkullType
import net.minecraft.util.IItemProvider

object CustomSkull {
	interface ICustomSkull : ISkullType, IItemProvider
	
	object Enderman : ICustomSkull {
		override fun asItem() = ModItems.ENDERMAN_HEAD
	}
}
