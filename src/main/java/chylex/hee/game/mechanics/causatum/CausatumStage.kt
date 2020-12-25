package chylex.hee.game.mechanics.causatum

enum class CausatumStage(val key: String) {
	S0_INITIAL("initial"),
	S1_KILLED_ENDERMAN("enderman"),
	S2_ENTERED_END("enter_end"),
	S3_FINISHED_CURSED_LIBRARY("cursed_library"),
	S4_FINISHED_DRAGON_LAIR("dragon_lair"),
	S5_FINISHED_DEMON_PASS("demon_pass"),
	S6_ULTIMUS("ultimus");
	
	companion object {
		fun fromKey(key: String): CausatumStage? {
			return values().firstOrNull { it.key == key }
		}
	}
}
