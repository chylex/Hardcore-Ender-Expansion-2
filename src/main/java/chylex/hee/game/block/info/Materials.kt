package chylex.hee.game.block.info

object Materials{
	val SOLID_NO_TOOL = CustomMaterial().apply {
		requiresTool = false
	}
	
	val SOLID_WITH_TOOL = CustomMaterial().apply {
		requiresTool = true
	}
	
	val ANCIENT_COBWEB = CustomMaterial().apply {
		blocksMovement = false
		destroyWhenPushed()
	}
	
	val ENERGY_CLUSTER = CustomMaterial().apply {
		makeTransparent()
		destroyWhenPushed()
	}
	
	val CORRUPTED_ENERGY = CustomMaterial().apply {
		replaceable = true
		makeTransparent()
		destroyWhenPushed()
	}
	
	val ENDER_GOO = CustomMaterial().apply {
		makeLiquid()
	}
	
	val PURIFIED_ENDER_GOO = CustomMaterial().apply {
		makeLiquid()
	}
	
	val SCAFFOLDING = CustomMaterial().apply {
		blocksLight = false
		translucent = true
		blockWhenPushed()
	}
}
