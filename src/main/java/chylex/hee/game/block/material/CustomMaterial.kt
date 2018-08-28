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
	
	fun destroyWhenPushed() = setNoPushMobility()
	fun blockWhenPushed() = setImmovableMobility()
	
	fun makeTransparent(){
		solid = false
		replaceable = true
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
	
	override fun isSolid(): Boolean = solid
	override fun isLiquid(): Boolean = liquid
	override fun isOpaque(): Boolean = blocksMovement && !translucent
	override fun isReplaceable(): Boolean = replaceable
	override fun getCanBurn(): Boolean = flammable
	
	override fun isToolNotRequired(): Boolean = !requiresTool
	override fun blocksLight(): Boolean = blocksLight
	override fun blocksMovement(): Boolean = blocksMovement
}
