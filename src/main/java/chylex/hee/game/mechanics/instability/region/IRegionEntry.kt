package chylex.hee.game.mechanics.instability.region

interface IRegionEntry {
	val compacted: Long
	val key: Long
	val x: Int
	val z: Int
	val points: Int
	val adjacent: Sequence<IRegionEntry>
	
	fun withPoints(points: Int): IRegionEntry
}
