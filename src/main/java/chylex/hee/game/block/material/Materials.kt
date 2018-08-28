package chylex.hee.game.block.material

object Materials{
	val SOLID_NO_TOOL = CustomMaterial().apply {
		requiresTool = false
	}
	
	val SOLID_WITH_TOOL = CustomMaterial().apply {
		requiresTool = true
	}
}
