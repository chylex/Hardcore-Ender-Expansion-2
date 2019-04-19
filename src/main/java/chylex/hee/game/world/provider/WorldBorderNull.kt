package chylex.hee.game.world.provider
import net.minecraft.world.border.WorldBorder

class WorldBorderNull : WorldBorder(){
	override fun setSize(size: Int){}
	override fun setCenter(x: Double, z: Double){}
	
	override fun setTransition(newSize: Double){}
	override fun setTransition(oldSize: Double, newSize: Double, time: Long){}
	
	override fun setDamageAmount(newAmount: Double){}
	override fun setDamageBuffer(bufferSize: Double){}
	
	override fun setWarningTime(warningTime: Int){}
	override fun setWarningDistance(warningDistance: Int){}
}
