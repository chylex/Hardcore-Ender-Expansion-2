package chylex.hee.proxy
import net.minecraft.entity.player.EntityPlayer

open class ModCommonProxy{
	open fun getClientSidePlayer(): EntityPlayer? = null
	
	open fun onPreInit(){}
	open fun onInit(){}
}
