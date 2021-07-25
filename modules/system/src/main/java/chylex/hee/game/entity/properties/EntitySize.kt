package chylex.hee.game.entity.properties

data class EntitySize(val width: Float, val height: Float) {
	constructor(size: Float) : this(size, size)
}
