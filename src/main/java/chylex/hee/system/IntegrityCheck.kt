package chylex.hee.system
import chylex.hee.HardcoreEnderExpansion

object IntegrityCheck{
	
	fun verify(){
	}
	
	// Utilities
	
	private fun warnIfFalse(value: Boolean, message: String){
		if (!value){
			failIntegrityCheck(message, Debug.enabled)
		}
	}
	
	private fun crashIfFalse(value: Boolean, message: String){
		if (!value){
			failIntegrityCheck(message, true)
		}
	}
	
	private fun failIntegrityCheck(message: String, crash: Boolean){
		HardcoreEnderExpansion.log.error("[IntegrityCheck] $message")
		
		if (crash){
			throw IllegalStateException("Integrity check failed: $message")
		}
	}
}
