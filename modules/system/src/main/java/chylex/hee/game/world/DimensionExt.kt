package chylex.hee.game.world

import chylex.hee.HEE
import net.minecraft.entity.Entity
import net.minecraft.world.World

val World.isOverworldDimension
	get() = this.dimensionKey === World.OVERWORLD

val World.isEndDimension
	get() = this.dimensionKey === HEE.dim

val Entity.isInOverworldDimension
	get() = this.world.isOverworldDimension

val Entity.isInEndDimension
	get() = this.world.isEndDimension
