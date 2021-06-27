package chylex.hee.game.block.properties

@Suppress("ConvertLambdaToReference")
object Materials {
	val SOLID = CustomMaterial().build()
	
	val INFUSED_GLASS = CustomMaterial().build {
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
		destroyWhenPushed()
	}
	
	val SCAFFOLDING = CustomMaterial().build {
		translucent = true
		blockWhenPushed()
	}
}
