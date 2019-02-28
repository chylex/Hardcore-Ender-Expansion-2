package chylex.hee.game.world.feature.stronghold

enum class StrongholdPieceType(val weightConnectionExponent: Float, val weightMultiplier: Int){
	CORRIDOR(weightConnectionExponent = 2.5F, weightMultiplier = 2),
	ROOM    (weightConnectionExponent = 1.8F, weightMultiplier = 4),
	OTHER   (weightConnectionExponent = 1F,   weightMultiplier = 0);
	
	fun shouldIncreaseDistanceToPortal(connectedWith: StrongholdPieceType): Boolean{
		return (this == ROOM && connectedWith != ROOM) || (this != ROOM && connectedWith == ROOM)
	}
}
