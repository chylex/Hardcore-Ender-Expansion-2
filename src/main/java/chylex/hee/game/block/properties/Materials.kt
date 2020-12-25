package chylex.hee.game.block.properties

object Materials {
	val SOLID_NO_TOOL = CustomMaterial().build {
		requiresTool = false
	}
	
	val SOLID_WITH_TOOL = CustomMaterial().build {
		requiresTool = true
	}
	
	val INFUSED_GLASS = CustomMaterial().build {
		requiresTool = true
		translucent = true
	}
	
	val ANCIENT_COBWEB = CustomMaterial().build {
		blocksMovement = false
		destroyWhenPushed()
	}
	
	val ENERGY_CLUSTER = CustomMaterial().build {
		makeTransparent()
		destroyWhenPushed()
	}
	
	val CORRUPTED_ENERGY = CustomMaterial().build {
		replaceable = true
		makeTransparent()
		destroyWhenPushed()
	}
	
	val ENDER_GOO = CustomMaterial().build {
		makeLiquid()
	}
	
	val PURIFIED_ENDER_GOO = CustomMaterial().build {
		makeLiquid()
	}
	
	val IGNEOUS_ROCK_PLATE = CustomMaterial().build {
		destroyWhenPushed()
	}
	
	val JAR_O_DUST = CustomMaterial().build {
		requiresTool = false
		destroyWhenPushed()
	}
	
	val SCAFFOLDING = CustomMaterial().build {
		translucent = true
		blockWhenPushed()
	}
}
