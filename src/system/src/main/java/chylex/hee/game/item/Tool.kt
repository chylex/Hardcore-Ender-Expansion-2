package chylex.hee.game.item

import net.minecraftforge.common.ToolType

object Tool {
	object Type {
		val PICKAXE: ToolType = ToolType.PICKAXE
		val SHOVEL: ToolType  = ToolType.SHOVEL
		val AXE: ToolType     = ToolType.AXE
	}
	
	object Level {
		const val WOOD    = 0
		const val GOLD    = 0
		const val STONE   = 1
		const val IRON    = 2
		const val DIAMOND = 3
	}
}
