package chylex.hee.game.block.info
import net.minecraft.block.material.Material
import net.minecraft.block.material.MaterialColor
import net.minecraft.block.material.PushReaction

class CustomMaterial{
	var solid = true
	private var liquid = false
	
	var translucent = false
	var replaceable = false
	var flammable = false
	
	var requiresTool = false
	var blocksMovement = true
	var blocksLight = true // UPDATE not used anywhere
	
	private var pushReaction = PushReaction.NORMAL
	
	fun destroyWhenPushed(){
		pushReaction = PushReaction.DESTROY
	}
	
	fun blockWhenPushed(){
		pushReaction = PushReaction.BLOCK
	}
	
	fun makeTransparent(){
		solid = false
		blocksMovement = false
		blocksLight = false
	}
	
	fun makeLiquid(){
		solid = false
		liquid = true
		replaceable = true
		blocksMovement = false
		destroyWhenPushed()
	}
	
	// Building
	
	fun build(): Material{
		return Material(MaterialColor.AIR, liquid, solid, blocksMovement, blocksMovement && !translucent, !requiresTool, flammable, replaceable, pushReaction)
	}
	
	inline fun build(block: CustomMaterial.() -> Unit): Material{
		block()
		return build()
	}
}
