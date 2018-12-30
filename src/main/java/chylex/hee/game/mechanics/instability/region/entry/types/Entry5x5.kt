package chylex.hee.game.mechanics.instability.region.entry.types
import chylex.hee.game.mechanics.instability.region.entry.IRegionEntry
import chylex.hee.game.mechanics.instability.region.entry.IRegionEntryConstructor
import chylex.hee.system.util.shlong
import net.minecraft.util.math.BlockPos

inline class Entry5x5(override val compacted: Long) : IRegionEntry{
	private companion object{
		private const val MASK_X = 0x00000_FFFFFL
		private const val MASK_Z = 0xFFFFF_00000L
		private const val MASK_XZ = MASK_X or MASK_Z
		
		private const val BIT_OFFSET_X = 0
		private const val BIT_OFFSET_Z = 20
		private const val BIT_OFFSET_POINTS = 40
		
		private const val REGION_CHUNKS = 5
		private const val REGION_BLOCKS = REGION_CHUNKS * 16
		private const val REGION_Z_OFFSET_BLOCKS = REGION_BLOCKS / 2
		
		// methods must be public, otherwise some weird interaction between them and inline classes generates invalid bytecode
		
		fun fromRegion(regionX: Int, regionZ: Int): Entry5x5{
			val bitsX = (regionX shlong BIT_OFFSET_X) and MASK_X
			val bitsZ = (regionZ shlong BIT_OFFSET_Z) and MASK_Z
			
			return Entry5x5(bitsX or bitsZ)
		}
		
		fun fixNegativeCoord(coord: Int): Int{
			return if (coord > 0x7FFFF)
				-(0xFFFFF - coord)
			else
				coord
		}
	}
	
	object Constructor : IRegionEntryConstructor<Entry5x5>{
		override fun fromCompacted(compacted: Long): Entry5x5{
			return Entry5x5(compacted)
		}
		
		override fun fromPos(pos: BlockPos): Entry5x5{
			val regionX = getRegionCoord(pos.x)
			
			val offsetZ = if (regionX % 2 == 0) 0 else REGION_Z_OFFSET_BLOCKS
			val regionZ = getRegionCoord(pos.z + offsetZ)
			
			return fromRegion(regionX, regionZ)
		}
		
		private inline fun getRegionCoord(coord: Int): Int{
			return if (coord < 0)
				((coord + 1) / REGION_BLOCKS) - 1
			else
				coord / REGION_BLOCKS
		}
	}
	
	// Instance
	
	override val key
		get() = compacted and MASK_XZ
	
	override val x
		get() = ((compacted and MASK_X) ushr BIT_OFFSET_X).toInt()
	
	override val z
		get() = ((compacted and MASK_Z) ushr BIT_OFFSET_Z).toInt()
	
	override val points
		get() = (compacted shr BIT_OFFSET_POINTS).toInt()
	
	override val adjacent: Sequence<Entry5x5>
		get(){
			val x = this.x
			val z = this.z
			
			val offsetZ = x and 1
			
			return sequenceOf(
				fromRegion(x + 0, z - 1),
				fromRegion(x + 0, z + 1),
				fromRegion(x - 1, z + 0 - offsetZ),
				fromRegion(x + 1, z + 0 - offsetZ),
				fromRegion(x - 1, z + 1 - offsetZ),
				fromRegion(x + 1, z + 1 - offsetZ)
			)
		}
	
	override fun withPoints(points: Int): Entry5x5{
		return Entry5x5((compacted and MASK_XZ) or (points shlong BIT_OFFSET_POINTS))
	}
	
	override fun toString(): String{
		return "Entry5x5 (x = ${fixNegativeCoord(x)}, z = ${fixNegativeCoord(z)}, points = $points, key = $key, compacted = $compacted)"
	}
	
	/* why write tests when you can plop this into the code and see what it does with your eyes
	
	Pos(0, 16, 0).allInCenteredBoxMutable(200, 0, 200).forEach { pos ->
		val e = Entry5x5.Constructor.fromPos(pos)
		pos.setState(world, Blocks.WOOL.defaultState.withProperty(COLOR, EnumDyeColor.values()[(e.x + 4 * e.z) % 16]))
		
		if (Entry5x5.Constructor.fromPos(Pos(0, 0, 0)).adjacent.any { it.key == e.key }){
			pos.up().setBlock(world, Blocks.ACACIA_FENCE)
		}
		else{
			pos.up().setAir(world)
		}
	}*/
}
