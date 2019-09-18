package chylex.hee.game.world.feature.obsidiantower
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerLevel_Top
import chylex.hee.game.world.feature.obsidiantower.piece.ObsidianTowerRoom_General

class ObsidianTowerRoomArrangement(
	val levels: Array<Pair<Array<out ObsidianTowerRoom_General>, ObsidianTowerSpawnerLevel>>,
	val topPiece: ObsidianTowerLevel_Top
){
	val floors
		get() = levels.size
}
