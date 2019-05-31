package chylex.hee.game.block.material
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material

class CustomMaterial : Material(MapColor.AIR){
	var solid = true
	private var liquid = false
	
	var translucent = false
	var replaceable = false
	var flammable = false
	
	var requiresTool = false
	var blocksMovement = true
	var blocksLight = true
	
	fun destroyWhenPushed(){
		setNoPushMobility()
	}
	
	fun blockWhenPushed(){
		setImmovableMobility()
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
	
	// Overrides
	
	override fun isSolid() = solid
	override fun isLiquid() = liquid
	override fun isOpaque() = blocksMovement && !translucent
	override fun isReplaceable() = replaceable
	override fun getCanBurn() = flammable
	
	override fun isToolNotRequired() = !requiresTool
	override fun blocksLight() = blocksLight
	override fun blocksMovement() = blocksMovement
}
