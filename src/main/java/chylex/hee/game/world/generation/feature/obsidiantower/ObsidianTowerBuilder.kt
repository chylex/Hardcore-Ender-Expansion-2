package chylex.hee.game.world.generation.feature.obsidiantower

import chylex.hee.game.world.generation.feature.obsidiantower.ObsidianTowerSpawnerLevel.LEVEL_1
import chylex.hee.game.world.generation.structure.piece.IStructureBuild
import chylex.hee.game.world.generation.structure.piece.IStructureBuilder
import chylex.hee.game.world.generation.structure.piece.StructureBuild
import chylex.hee.game.world.generation.structure.piece.StructureBuild.AddMode.MERGE
import chylex.hee.game.world.generation.structure.piece.StructurePiece
import chylex.hee.game.world.util.Transform
import chylex.hee.util.math.Size.Alignment.CENTER
import chylex.hee.util.math.Size.Alignment.MIN
import chylex.hee.util.random.nextItem
import net.minecraft.util.Rotation
import java.util.Random

class ObsidianTowerBuilder(private val arrangement: ObsidianTowerRoomArrangement, private val rotation: Rotation? = null) : IStructureBuilder<IStructureBuild> {
	override fun build(rand: Random): IStructureBuild {
		val transform1 = Transform(rotation = rotation ?: rand.nextItem(), mirror = false)
		val transform2 = transform1.copy(rotation = transform1.rotation.add(Rotation.CLOCKWISE_180))
		
		val floors = arrangement.floors
		
		val size = ObsidianTowerPieces.calculateStructureSize(floors)
		val build = StructureBuild<StructurePiece<*>.MutableInstance>(size)
		
		val bottomPos = size.getPos(CENTER, MIN, CENTER)
		var y = 0
		
		val roomListMapping = arrangement.levels.map { it.first }.associateWith { it.toMutableList() }
		
		for (floor in 0..floors) {
			val piece = when (floor) {
				floors -> arrangement.topPiece
				0      -> ObsidianTowerPieces.PIECE_LEVEL_BOTTOM
				else   -> ObsidianTowerPieces.PIECE_LEVEL_MIDDLE
			}
			
			val transform = if (floor % 2 == 0) transform1 else transform2
			val floorPos = bottomPos.up(y)
			
			build.addPiece(piece.MutableInstance(transform), floorPos.subtract(piece.size.getPos(CENTER, MIN, CENTER)))
			
			if (floor < floors) {
				val (originalRoomList, spawnerLevel) = arrangement.levels[floor]
				val roomList = roomListMapping.getValue(originalRoomList)
				
				val data = ObsidianTowerRoomData(spawnerLevel, rand)
				val room = rand.nextItem(if (spawnerLevel == LEVEL_1) roomList.filter { it.guaranteesSpawnersOnLevel1 }.ifEmpty { roomList } else roomList)
				
				roomList.remove(room)
				build.addPiece(room.MutableInstance(data, transform), floorPos.subtract(room.size.getPos(CENTER, MIN, CENTER)), MERGE)
			}
			
			y += piece.size.y
		}
		
		return build.freeze()
	}
}
