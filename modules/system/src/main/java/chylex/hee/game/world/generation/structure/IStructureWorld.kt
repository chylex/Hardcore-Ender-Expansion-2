package chylex.hee.game.world.generation.structure

import chylex.hee.game.world.generation.IBlockPicker
import chylex.hee.game.world.util.max
import chylex.hee.game.world.util.min
import chylex.hee.util.math.MutablePos
import chylex.hee.util.math.component1
import chylex.hee.util.math.component2
import chylex.hee.util.math.component3
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.material.Material
import net.minecraft.util.math.BlockPos
import java.util.Random

interface IStructureWorld {
	val rand: Random
	
	fun getState(pos: BlockPos): BlockState
	fun setState(pos: BlockPos, state: BlockState)
	
	fun addTrigger(pos: BlockPos, trigger: IStructureTrigger)
	
	fun finalize()
	
	// Utilities
	
	fun getBlock(pos: BlockPos): Block {
		return getState(pos).block
	}
	
	fun setBlock(pos: BlockPos, block: Block) {
		setState(pos, block.defaultState)
	}
	
	fun setAir(pos: BlockPos) {
		setState(pos, Blocks.AIR.defaultState)
	}
	
	fun isAir(pos: BlockPos): Boolean {
		return getState(pos).material === Material.AIR
	}
	
	fun placeBlock(pos: BlockPos, picker: IBlockPicker) {
		setState(pos, picker.pick(rand))
	}
	
	fun placeCube(pos1: BlockPos, pos2: BlockPos, picker: IBlockPicker) {
		val (x1, y1, z1) = pos1.min(pos2)
		val (x2, y2, z2) = pos1.max(pos2)
		
		val mut = MutablePos()
		
		for (x in x1..x2) for (y in y1..y2) for (z in z1..z2) {
			setState(mut.setPos(x, y, z), picker.pick(rand))
		}
	}
	
	fun placeCubeHollow(pos1: BlockPos, pos2: BlockPos, picker: IBlockPicker) {
		val (x1, y1, z1) = pos1.min(pos2)
		val (x2, y2, z2) = pos1.max(pos2)
		
		val mut1 = MutablePos()
		val mut2 = MutablePos()
		
		placeCube(mut1.setPos(x1, y1, z1), mut2.setPos(x2, y1, z2), picker)
		placeCube(mut1.setPos(x1, y2, z1), mut2.setPos(x2, y2, z2), picker)
		
		placeCube(mut1.setPos(x1, y1 + 1, z1), mut2.setPos(x2, y2 - 1, z1), picker)
		placeCube(mut1.setPos(x1, y1 + 1, z2), mut2.setPos(x2, y2 - 1, z2), picker)
		
		placeCube(mut1.setPos(x1, y1 + 1, z1 + 1), mut2.setPos(x1, y2 - 1, z2 - 1), picker)
		placeCube(mut1.setPos(x2, y1 + 1, z1 + 1), mut2.setPos(x2, y2 - 1, z2 - 1), picker)
	}
	
	fun placeWalls(pos1: BlockPos, pos2: BlockPos, picker: IBlockPicker) {
		val (x1, y1, z1) = pos1.min(pos2)
		val (x2, y2, z2) = pos1.max(pos2)
		
		val mut1 = MutablePos()
		val mut2 = MutablePos()
		
		placeCube(mut1.setPos(x1, y1, z1), mut2.setPos(x2, y2, z1), picker)
		placeCube(mut1.setPos(x1, y1, z2), mut2.setPos(x2, y2, z2), picker)
		
		if (z1 != z2) {
			placeCube(mut1.setPos(x1, y1, z1 + 1), mut2.setPos(x1, y2, z2 - 1), picker)
			placeCube(mut1.setPos(x2, y1, z1 + 1), mut2.setPos(x2, y2, z2 - 1), picker)
		}
	}
}
